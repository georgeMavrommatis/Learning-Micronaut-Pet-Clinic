package com.gmavrommatis.controller;

import com.gmavrommatis.config.mongo.document.VetReview;
import com.gmavrommatis.mapper.VetReviewToVetReviewResponseMapper;
import com.gmavrommatis.model.request.CreateVetReviewRequest;
import com.gmavrommatis.model.response.VetReviewDetails;
import com.gmavrommatis.model.response.VetReviewResponse;
import com.gmavrommatis.model.response.VetReviewScore;
import com.gmavrommatis.service.VetReviewService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import java.util.List;

/**
 * REST controller for managing veterinarian reviews.
 *
 * <p>Provides endpoints to paginate through all reviews, compute reviewer scores, and create new
 * reviews for veterinarians.
 *
 * @author Your Name
 */
@Controller("/vet-review")
public class VetReviewController {

  private final VetReviewService vetReviewService;
  private final VetReviewToVetReviewResponseMapper mapper;

  public VetReviewController(
      VetReviewService vetReviewService, VetReviewToVetReviewResponseMapper mapper) {
    this.vetReviewService = vetReviewService;
    this.mapper = mapper;
  }

  /**
   * Retrieves a page of veterinarian reviews.
   *
   * <p>Supports pagination via {@code page} (zero-based index) and {@code size} (items per page)
   * query parameters. Returns review details along with pagination metadata.
   *
   * @param page zero-based page index (defaults to 0 if not provided)
   * @param size the maximum number of reviews per page (defaults to 10 if not provided)
   * @return an {@code HttpResponse} containing a {@link VetReviewDetails} DTO with the list of
   *     reviews and pagination information
   */
  @Get("/details")
  public HttpResponse<VetReviewDetails> findVetReviews(
      @QueryValue(defaultValue = "0") int page, @QueryValue(defaultValue = "10") int size) {
    Page<VetReview> vetReviewPage = vetReviewService.findAll(Pageable.from(page, size));

    List<VetReviewResponse> vetReviewResponses =
        mapper.toVetReviewResponseList(vetReviewPage.getContent());
    return HttpResponse.ok(
        VetReviewDetails.builder()
            .vetReviews(vetReviewResponses)
            .page(vetReviewPage.getPageable().getNumber())
            .size(vetReviewPage.getPageable().getSize())
            .totalPages(vetReviewPage.getTotalPages())
            .totalElements(vetReviewPage.getTotalSize())
            .build());
  }

  @Get("above-min-rating/{firstName}/{lastName}")
  public HttpResponse<List<VetReviewResponse>> findVetReviewsAboveRating(
      @PathVariable String firstName, @PathVariable String lastName, @QueryValue Short minRating) {
    List<VetReview> vetReviews =
        vetReviewService.findReviewsAboveRating(firstName, lastName, minRating);
    return HttpResponse.ok(mapper.toVetReviewResponseList(vetReviews));
  }

  /**
   * Computes and returns the review score for a specific veterinarian, identified by the reviewer's
   * first and last name.
   *
   * <p>This endpoint aggregates review data to produce a score summary.
   *
   * @param firstName the reviewer’s first name (non-blank)
   * @param lastName the reviewer’s last name (non-blank)
   * @return an {@code HttpResponse} containing a {@link VetReviewScore} DTO with aggregated rating
   *     information
   */
  @Get("/{firstName}/{lastName}")
  public HttpResponse<VetReviewScore> findVetRating(
      @PathVariable String firstName, @PathVariable String lastName) {
    VetReviewScore vetReviewScore = vetReviewService.findVetRating(firstName, lastName);
    return HttpResponse.ok(vetReviewScore);
  }

  /**
   * Creates a new veterinarian review.
   *
   * <p>Persists a new review based on the provided {@link CreateVetReviewRequest}. Returns HTTP 201
   * Created on success.
   *
   * @param request the {@link CreateVetReviewRequest} containing review details
   * @return an {@code HttpResponse<Void>} with status 201 Created
   */
  @Post
  public HttpResponse<Void> create(@Body CreateVetReviewRequest request) {
    vetReviewService.saveReview(request);
    return HttpResponse.status(HttpStatus.CREATED);
  }
}
