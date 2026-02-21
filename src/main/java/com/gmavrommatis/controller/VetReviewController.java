package com.gmavrommatis.controller;

import com.gmavrommatis.mapper.VetReviewToVetReviewResponseMapper;
import com.gmavrommatis.model.request.CreateVetReviewRequest;
import com.gmavrommatis.model.response.VetReviewDetails;
import com.gmavrommatis.model.response.VetReviewScore;
import com.gmavrommatis.service.VetReviewService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
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
 * @version 1.0
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
  @Get("/details")
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
  public Mono<MutableHttpResponse<Object>> create(@Body CreateVetReviewRequest request) {
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
