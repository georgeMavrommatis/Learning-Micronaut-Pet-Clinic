package com.gmavrommatis.controller;

import com.gmavrommatis.mapper.VetReviewToWebSocketVetReviewResponseMapper;
import com.gmavrommatis.model.response.WebSocketMetadataResponse;
import com.gmavrommatis.model.response.WebSocketVetReviewResponse;
import com.gmavrommatis.service.VetReviewService;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.*;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

/**
 * Exposes a WebSocket endpoint for paginated streaming of veterinarian reviews.
 *
 * <p>The endpoint supports back-pressure by emitting a metadata frame followed by a sequence of
 * review frames, delaying each message by a fixed inter-message duration. Session-specific cursors
 * prevent duplicate delivery across multiple "NEXT n" requests.
 *
 * @author Gewrgios Mavrommatis
 */
@ServerWebSocket("/vet-review/reviewer-details/back-pressure")
@Slf4j
public class VetReviewWebSocket {

  private final VetReviewService service;
  private final VetReviewToWebSocketVetReviewResponseMapper mapper;
  private final WebSocketBroadcaster broadcaster;

  // Tracks the number of reviews sent per client session
  private final Map<String, AtomicInteger> offsets = new ConcurrentHashMap<>();

  // Guards against overlapping requests for the same session
  private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

  /**
   * Constructs the WebSocket controller with required dependencies.
   *
   * @param service provider of review data and total count operations
   * @param mapper converter from domain model to WebSocket response DTO
   * @param broadcaster reactive broadcaster for session-specific message delivery
   */
  @Inject
  public VetReviewWebSocket(
      VetReviewService service,
      VetReviewToWebSocketVetReviewResponseMapper mapper,
      WebSocketBroadcaster broadcaster) {
    this.service = service;
    this.mapper = mapper;
    this.broadcaster = broadcaster;
  }

  /**
   * Initializes per-session state when a new WebSocket connection is established.
   *
   * @param session the newly opened WebSocket session
   */
  @OnOpen
  public void onOpen(WebSocketSession session) {
    offsets.put(session.getId(), new AtomicInteger(0));
    log.info("Session {} opened, offset initialized to 0", session.getId());
  }

  /**
   * Handles incoming "NEXT n" commands to stream the next batch of reviews.
   *
   * <p>It emits a single metadata frame indicating the batch size, then streams the requested
   * reviews with a fixed delay. Upon completion, the session cursor is advanced by the number of
   * items sent.
   *
   * @param message raw command text of the form "NEXT <n>"
   * @param session the WebSocket session that sent the command
   * @return a reactive Publisher that emits frames to the client
   */
  @OnMessage
  public Publisher<WebSocketVetReviewResponse> stream(String message, WebSocketSession session) {
    String sessionId = session.getId();

    // Prevent concurrent streams for the same session
    if (!inFlight.add(sessionId)) {
      WebSocketVetReviewResponse err =
          WebSocketVetReviewResponse.builder()
              // .error("Previous request still in progress")
              .build();
      // Returns a Publisher that will send a single error frame
      return broadcaster.broadcast(err, s -> s.getId().equals(sessionId));
    }

    AtomicInteger offset = offsets.computeIfAbsent(sessionId, id -> new AtomicInteger(0));
    int start = offset.get();
    int count = parseCount(message);

    // Compose a Flux of one metadata frame followed by data frames
    Flux<WebSocketVetReviewResponse> flux =
        service
            .count()
            .flatMapMany(
                total -> {
                  long remaining = Math.max(0L, total - start);
                  int expectedSize = (int) Math.min(remaining, count);

                  // Metadata frame: informs client how many items to expect
                  WebSocketVetReviewResponse meta =
                      WebSocketVetReviewResponse.builder()
                          .webSocketMetadataResponse(
                              WebSocketMetadataResponse.builder()
                                  .expectedStreamSize(expectedSize)
                                  .build())
                          .build();

                  // Data flux: maps domain objects to DTOs
                  Flux<WebSocketVetReviewResponse> dataFlux =
                      service.findBatch(start, count).map(mapper::toWebSocketVetReviewResponse);

                  // Concatenate meta + data, apply delay, and update cursor on completion
                  return Flux.concat(Flux.just(meta), dataFlux)
                      .delayElements(Duration.ofSeconds(1))
                      .doOnComplete(
                          () -> {
                            int newOffset = offset.addAndGet(expectedSize);
                            log.info("Session {} cursor advanced to {}", sessionId, newOffset);
                          });
                })
            // Ensure in-flight flag is cleared when the stream terminates
            .doFinally(signal -> inFlight.remove(sessionId));

    // Hand off each element to the broadcaster for JSON serialization and delivery
    return flux.flatMap(resp -> broadcaster.broadcast(resp, s -> s.getId().equals(sessionId)));
  }

  /**
   * Cleans up session-specific state when the WebSocket connection closes.
   *
   * @param session the session that has closed
   */
  @OnClose
  public void onClose(WebSocketSession session) {
    String sessionId = session.getId();
    offsets.remove(sessionId);
    inFlight.remove(sessionId);
    log.info("Session {} closed, state cleared", sessionId);
  }

  /**
   * Logs unexpected errors during WebSocket processing.
   *
   * @param session the session in which the error occurred
   * @param t the thrown exception
   */
  @OnError
  public void onError(WebSocketSession session, Throwable t) {
    log.error("WebSocket error in session {}", session.getId(), t);
  }

  /**
   * Parses the client command to determine batch size.
   *
   * <p>The expected format is "NEXT <n>"; any parse failure defaults to 1.
   *
   * @param message raw command text
   * @return the requested batch size, or 1 on invalid input
   */
  private int parseCount(String message) {
    if (message != null && message.startsWith("NEXT ")) {
      try {
        return Integer.parseInt(message.substring(5).trim());
      } catch (NumberFormatException e) {
        log.warn("Invalid batch size '{}', defaulting to 1", message);
      }
    }
    return 1;
  }
}
