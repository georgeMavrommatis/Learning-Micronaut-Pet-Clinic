package com.gmavrommatis.controller;

import static java.lang.Math.clamp;

import com.gmavrommatis.mapper.VetReviewToVetReviewResponseMapper;
import com.gmavrommatis.model.request.CreateVetReviewRequest;
import com.gmavrommatis.model.response.VetReviewDetails;
import com.gmavrommatis.model.response.VetReviewResponse;
import com.gmavrommatis.model.response.VetReviewScore;
import com.gmavrommatis.service.VetReviewService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.http.sse.Event;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing veterinarian reviews in a reactive, non-blocking fashion.
 *
 * <p>Exposes endpoints to page through reviews, retrieve review scores by reviewer name, and create
 * new reviews with reactive types.
 *
 * @author Your Name
 */
@Controller("/vet-review")
@Slf4j
public class VetReviewController {

  private final VetReviewService vetReviewService;
  private final VetReviewToVetReviewResponseMapper mapper;

  public VetReviewController(
      VetReviewService vetReviewService, VetReviewToVetReviewResponseMapper mapper) {
    this.vetReviewService = vetReviewService;
    this.mapper = mapper;
  }

  /**
   * Streams a batch of vet reviews as a JSON stream with built-in back-pressure support.
   *
   * <p>This endpoint returns up to {@code limit} reviews, starting from {@code offset}, emitting
   * one element per second. It also sets response headers to convey pagination and authorization
   * information.
   *
   * @param offset the zero-based index of the first review to include in the stream (default 0)
   * @param limit the maximum number of reviews to return in this batch (default 10)
   * @return a {@code Mono} emitting an HTTP 200 OK response containing a {@code Flux} of {@link
   *     VetReviewResponse} objects, or a 500 error with an empty stream if an exception occurs
   *     during response creation
   * @header Authorization Bearer test
   * @header Expected-Stream-Size the total number of reviews available (retrieved via {@code
   *     vetReviewService.count()}, defaults to 0 on count errors)
   * @header Offset echo of the {@code offset} query parameter
   * @header Limit echo of the {@code limit} query parameter
   * @implSpec
   *     <ul>
   *       <li>Logs at INFO level each review as it is processed.
   *       <li>Delays each element by 1 second to simulate back-pressure or pacing.
   *       <li>Defaults the total count header to 0 and logs an ERROR if counting fails.
   *       <li>Returns a server-error response with an empty stream on response construction
   *           failures.
   *     </ul>
   */
  @Get(
      uri = "/reviewer-details/json-stream/back-pressure/batch",
      produces = MediaType.APPLICATION_JSON_STREAM)
  public Mono<MutableHttpResponse<Flux<VetReviewResponse>>> findBatchJsonStream(
      @QueryValue(defaultValue = "0") int offset, @QueryValue(defaultValue = "10") int limit) {

    Mono<Long> countMono =
        vetReviewService
            .count()
            .onErrorResume(
                err -> {
                  // log and default to zero if the count fails
                  log.error("Failed to count reviews, defaulting to 0", err);
                  return Mono.just(0L);
                });

    Flux<VetReviewResponse> stream =
        vetReviewService
            .findBatch(offset, limit)
            .delayElements(Duration.ofSeconds(1))
            .map(mapper::toVetReviewResponse)
            .doOnNext(vetReviewResponse -> log.info("Working on review: {}", vetReviewResponse));

    return countMono
        .map(
            count ->
                HttpResponse.ok(stream)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer test")
                    .header("Expected-Stream-Size", String.valueOf(clamp(count - offset, 0, limit)))
                    .header("Total-Count", String.valueOf(count))
                    .header("Offset", String.valueOf(offset))
                    .header("Limit", String.valueOf(limit)))
        .onErrorResume(
            err -> {
              // return a 500 with an empty stream if response creation fails
              log.error("Error building streaming response", err);
              return Mono.just(
                  HttpResponse.<Flux<VetReviewResponse>>serverError().body(Flux.empty()));
            });
  }

  /**
   * Streams a batch of vet reviews as Server-Sent Events (SSE) with built-in back-pressure support.
   *
   * <p>This endpoint returns up to {@code limit} reviews, starting from {@code offset}, emitting
   * one event per second. Each review is wrapped in an {@link io.micronaut.http.sse.Event} object.
   * Response headers convey authorization, custom metadata, and the total stream size.
   *
   * @param offset the zero-based index of the first review to include in the stream (default 0)
   * @param limit the maximum number of reviews to return in this batch (default 10)
   * @return a {@code Mono} emitting an HTTP 200 OK response containing a {@code Flux} of {@link
   *     io.micronaut.http.sse.Event}{@code <}{@link VetReviewResponse}{@code >} objects, or a 500
   *     error with a single error event if an exception occurs during response creation
   * @header Authorization Bearer \<test\>
   * @header Expected-Stream-Size the total number of reviews available (retrieved via {@code
   *     vetReviewService.count()}, defaults to 0 on count errors)
   * @implSpec
   *     <ul>
   *       <li>Logs at INFO level each review as it is processed.
   *       <li>Delays each element by 1 second to simulate pacing/back-pressure.
   *       <li>Maps each domain object to {@link VetReviewResponse}, then wraps it in an SSE {@link
   *           io.micronaut.http.sse.Event}.
   *       <li>Defaults the total count header to 0 and logs an ERROR if counting fails.
   *       <li>On response-building failure, returns HTTP 500 with a single SSE event containing an
   *           error message comment.
   *     </ul>
   */
  @Get(
      uri = "/reviewer-details/sse-stream/back-pressure/batch",
      produces = MediaType.TEXT_EVENT_STREAM)
  public Mono<MutableHttpResponse<Flux<Event<VetReviewResponse>>>> findBatchSSE(
      @QueryValue(defaultValue = "0") int offset, @QueryValue(defaultValue = "10") int limit) {

    Mono<Long> countMono =
        vetReviewService
            .count()
            .onErrorResume(
                err -> {
                  log.error("Failed to fetch total count – defaulting to 0", err);
                  return Mono.just(0L);
                });

    Flux<Event<VetReviewResponse>> eventFlux =
        vetReviewService
            .findBatch(offset, limit)
            .delayElements(Duration.ofSeconds(1))
            .map(mapper::toVetReviewResponse)
            .doOnNext(vetReviewResponse -> log.info("Working on review: {}", vetReviewResponse))
            .map(Event::of);

    return countMono
        .map(
            count ->
                HttpResponse.ok(eventFlux)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer <test>")
                    .header("Total-Count", String.valueOf(count))
                    .header("Expected-Stream-Size", String.valueOf(clamp(count - offset, 0, limit)))
                    .header("Offset", String.valueOf(offset))
                    .header("Limit", String.valueOf(limit)))
        .onErrorResume(
            err -> {
              log.error("Failed to build SSE response", err);
              Event<VetReviewResponse> errorEvent =
                  Event.<VetReviewResponse>of(null).comment("Stream error: " + err.getMessage());
              return Mono.just(
                  HttpResponse.<Flux<Event<VetReviewResponse>>>serverError()
                      .body(Flux.just(errorEvent)));
            });
  }

  /**
   * Retrieves a paginated list of veterinarian reviews.
   *
   * <p>Uses query parameters {@code page} (zero-based index) and {@code size} (items per page) to
   * control pagination. Returns a {@link VetReviewDetails} DTO wrapped in a 200 OK response.
   *
   * @param page zero-based page index (default 0)
   * @param size number of review items per page (default 10)
   * @return a {@code Mono<HttpResponse<VetReviewDetails>>} emitting 200 OK with paged review
   *     details
   */
  @Get("/reviewer-details")
  public Mono<HttpResponse<VetReviewDetails>> findVetReviews(
      @QueryValue(defaultValue = "0") int page, @QueryValue(defaultValue = "10") int size) {
    Pageable pageable = Pageable.from(page, size);
    return vetReviewService
        // assume you’ve exposed a reactive service method:
        .findAll(pageable) // Mono<Page<VetReview>>
        .map(
            vetReviewPage -> {
              var dtoList = mapper.toVetReviewResponseList(vetReviewPage.getContent());
              var details =
                  VetReviewDetails.builder()
                      .vetReviews(dtoList)
                      .page(vetReviewPage.getPageable().getNumber())
                      .size(vetReviewPage.getPageable().getSize())
                      .totalPages(vetReviewPage.getTotalPages())
                      .totalElements(vetReviewPage.getTotalSize())
                      .build();
              return HttpResponse.ok(details);
            });
  }

  /**
   * Streams all reviews as a JSON‐encoded sequence.
   *
   * <p>The HTTP response is sent with chunked transfer encoding, and each {@code VetReviewResponse}
   * is emitted once every second. The {@code Expected-Stream-Size} header reports the total number
   * of reviews available at the time the request is received.
   *
   * <ul>
   *   <li>If the count operation fails, it defaults the header value to 0.
   *   <li>If building the HTTP response fails, a 500 status with an empty stream is returned.
   * </ul>
   *
   * @return a non-blocking HTTP response whose body is a {@code Flux<VetReviewResponse>} that emits
   *     one review per second
   */
  @Get(uri = "/reviewer-details/json-stream", produces = MediaType.APPLICATION_JSON_STREAM)
  public Mono<MutableHttpResponse<Flux<VetReviewResponse>>> jsonStreamReviews() {
    Mono<Long> countMono =
        vetReviewService
            .count()
            .onErrorResume(
                err -> {
                  // log and default to zero if the count fails
                  log.error("Failed to count reviews, defaulting to 0", err);
                  return Mono.just(0L);
                });

    Flux<VetReviewResponse> stream =
        vetReviewService
            .findAll()
            .delayElements(Duration.ofSeconds(1))
            .map(mapper::toVetReviewResponse)
            .doOnNext(vetReviewResponse -> log.info("Working on review: {}", vetReviewResponse))
            .onErrorContinue(
                (throwable, obj) -> log.warn("Skipping one review due to error", throwable));

    return countMono
        .map(
            count ->
                HttpResponse.ok(stream)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer test")
                    .header("Total-Count", String.valueOf(count))
                    .header("Expected-Stream-Size", String.valueOf(count)))
        .onErrorResume(
            err -> {
              // return a 500 with an empty stream if response creation fails
              log.error("Error building streaming response", err);
              return Mono.just(
                  HttpResponse.<Flux<VetReviewResponse>>serverError().body(Flux.empty()));
            });
  }

  /**
   * Streams all reviews as Server‐Sent Events.
   *
   * <p>Each review is sent as an SSE {@link Event} once every second. The {@code
   * Expected-Stream-Size} header reports the total number of reviews at the time of subscription.
   *
   * <ul>
   *   <li>If the total‐count lookup fails, it is logged and defaults to 0.
   *   <li>If the HTTP response cannot be constructed, a 500 status with a single SSE comment
   *       describing the error is returned.
   * </ul>
   *
   * @return a non-blocking HTTP response whose body is a {@code Flux<Event<VetReviewResponse>>}
   *     representing a live stream of review events
   */
  @Get(uri = "/reviewer-details/text-event-stream", produces = MediaType.TEXT_EVENT_STREAM)
  public Mono<MutableHttpResponse<Flux<Event<VetReviewResponse>>>> eventStreamReviews() {
    Mono<Long> countMono =
        vetReviewService
            .count()
            .onErrorResume(
                err -> {
                  log.error("Failed to fetch total count – defaulting to 0", err);
                  return Mono.just(0L);
                });

    Flux<Event<VetReviewResponse>> eventFlux =
        vetReviewService
            .findAll()
            .delayElements(Duration.ofSeconds(1))
            .map(mapper::toVetReviewResponse)
            .doOnNext(vetReviewResponse -> log.info("Working on review: {}", vetReviewResponse))
            .map(Event::of)
            .onErrorContinue(
                (throwable, obj) ->
                    log.warn("Skipping one VetReview due to error: {}", obj, throwable));

    return countMono
        .map(
            count ->
                HttpResponse.ok(eventFlux)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer <test>")
                    .header("Total-Count", String.valueOf(count))
                    .header("Expected-Stream-Size", String.valueOf(count)))
        .onErrorResume(
            err -> {
              log.error("Failed to build SSE response", err);
              Event<VetReviewResponse> errorEvent =
                  Event.<VetReviewResponse>of(null).comment("Stream error: " + err.getMessage());
              return Mono.just(
                  HttpResponse.<Flux<Event<VetReviewResponse>>>serverError()
                      .body(Flux.just(errorEvent)));
            });
  }

  /**
   * Retrieves the review score for a specific veterinarian reviewer.
   *
   * <p>Returns 200 OK with the {@link VetReviewScore} if found, or 404 Not Found if the reactive
   * stream completes empty.
   *
   * @param firstName the reviewer’s first name (non-blank)
   * @param lastName the reviewer’s last name (non-blank)
   * @return a {@code Mono<MutableHttpResponse<VetReviewScore>>} emitting 200 OK or 404 Not Found
   */
  @Get("/{firstName}/{lastName}")
  public Mono<MutableHttpResponse<VetReviewScore>> findVetRating(
      @PathVariable String firstName, @PathVariable String lastName) {

    return vetReviewService
        .findVetRatingReactive(firstName, lastName)
        // wrap the result in a 200 OK
        .map(HttpResponse::ok)
        // if the Mono completes empty, return 404
        .defaultIfEmpty(HttpResponse.notFound());
  }

  /**
   * Finds review scores for multiple reviewers by zipping parallel lists of first and last names,
   * issuing all lookups concurrently, and collecting the results into a single HTTP response.
   *
   * <p>Example request:
   *
   * <pre>
   * GET /vet-review/findVetsScore
   *     ?firstName=Nicholas
   *     &lastName=Robinson
   *     &firstName=David
   *     &lastName=Lewis
   * </pre>
   *
   * <p>This will pair:
   *
   * <ul>
   *   <li>(Nicholas, Robinson)
   *   <li>(David, Lewis)
   * </ul>
   *
   * Then each pair is looked up in parallel via the reactive service.
   *
   * @param firstName the list of first names, in the same order as lastName
   * @param lastName the list of last names, in the same order as firstName
   * @return a Mono emitting:
   *     <ul>
   *       <li>HTTP 200 with a List&lt;VetReviewScore&gt; if any scores are found
   *       <li>HTTP 404 if the resulting list is empty
   *     </ul>
   */
  @Get("/findVetsScore{?firstName*,lastName*}")
  public Mono<MutableHttpResponse<List<VetReviewScore>>> findVetsRating(
      @QueryValue List<String> firstName, @QueryValue List<String> lastName) {

    /*Protocol multiplexing*/

    return Flux.zip(Flux.fromIterable(firstName), Flux.fromIterable(lastName))
        // multiplex all lookups (unbounded concurrency by default, or supply a number)
        .flatMap(
            tuple2 -> {
              String fn = tuple2.getT1();
              String ln = tuple2.getT2();
              return vetReviewService
                  .findVetRatingReactive(fn, ln)
                  .doOnNext(s -> log.info("subscribe {} {}", s.getFirstName(), s.getLastName()))
                  .doFinally(v -> log.info("done {} {}", fn, ln));
            })
        // collect all the scores into a List
        .collectList()
        // wrap into a 200 OK (or 404 if empty)
        .map(
            list ->
                list.isEmpty()
                    ? HttpResponse.<List<VetReviewScore>>notFound()
                    : HttpResponse.ok(list));
  }

  /**
   * Creates a new veterinarian review.
   *
   * <p>Accepts a {@link CreateVetReviewRequest} in the body, delegates to the service to save
   * reactively, and returns 201 Created on success. Errors are mapped to a 500 Server Error
   * response.
   *
   * @param request the {@link CreateVetReviewRequest} containing review details
   * @return a {@code Mono<MutableHttpResponse<Object>>} emitting 201 Created or 500 Server Error
   */
  @Post
  public Mono<MutableHttpResponse<Object>> create(@Body @Valid CreateVetReviewRequest request) {
    return vetReviewService
        .saveReviewReactive(request)
        // on success return 201 CREATED
        .thenReturn(HttpResponse.status(HttpStatus.CREATED))
        // (optional) map any exception to a 500 or custom error
        .onErrorResume(
            e ->
                Mono.just(
                    HttpResponse.serverError("Failed to save Vet-Review - " + e.getMessage())));
  }
}
