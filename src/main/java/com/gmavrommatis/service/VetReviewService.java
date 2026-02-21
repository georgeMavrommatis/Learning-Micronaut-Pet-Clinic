package com.gmavrommatis.service;

import com.gmavrommatis.config.jpa.domain.Vet;
import com.gmavrommatis.config.mongo.document.VetReview;
import com.gmavrommatis.config.mongo.repository.VetReviewRepository;
import com.gmavrommatis.mapper.CreateVetReviewRequestToVetReviewMapper;
import com.gmavrommatis.model.request.CreateVetReviewRequest;
import com.gmavrommatis.model.response.VetReviewScore;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for managing {@link VetReview} documents.
 *
 * <p>Provides operations to fetch, score, and save reviews for veterinarians, using MongoDB
 * transactions where appropriate.
 *
 * @author Your Name
 */
@Singleton
@Slf4j
public class VetReviewService {

  private final VetReviewRepository vetReviewRepository;
  private final VetService vetService;
  private final CreateVetReviewRequestToVetReviewMapper mapper;

  public VetReviewService(
      VetReviewRepository vetReviewRepository,
      VetService vetService,
      CreateVetReviewRequestToVetReviewMapper mapper) {
    this.vetReviewRepository = vetReviewRepository;
    this.vetService = vetService;
    this.mapper = mapper;
  }

  /**
   * Retrieves a paginated list of all veterinarian reviews.
   *
   * <p>Uses the {@code mongoTx} transaction manager in read-only mode.
   *
   * @param pageable pagination parameters (zero-based page index and page size)
   * @return a {@link Page} of {@link VetReview} documents
   */
  @Transactional(value = "mongoTx", readOnly = true)
  public Page<VetReview> findAll(Pageable pageable) {
    return vetReviewRepository.findAll(pageable);
  }

  @Transactional(value = "mongoTx", readOnly = true)
  public List<VetReview> findReviewsAboveRating(String firstName, String lastName, short rating) {
    /*Check Vet exists*/
    Vet vet = vetService.findByFirstAndLastName(firstName, lastName);
    return vetReviewRepository.findReviewsAboveRating(vet.getId(), rating);
  }

  /**
   * Calculates the average review score for a specific veterinarian.
   *
   * <p>Ensures the vet exists, then fetches all reviews by their ID to compute the average rating.
   * Uses the {@code mongoTx} transaction manager in read-only mode.
   *
   * @param firstName the first name of the veterinarian
   * @param lastName the last name of the veterinarian
   * @return a {@link VetReviewScore} containing the computed average rating
   */
  @Transactional(value = "mongoTx", readOnly = true)
  public VetReviewScore findVetRating(String firstName, String lastName) {
    /*Check Vet exists*/
    Vet vet = vetService.findByFirstAndLastName(firstName, lastName);
    /*Locate all reviews for the Vet*/
    List<VetReview> vetReviews = vetReviewRepository.findAllByVetId(vet.getId());
    /*Calculate Average Rating*/
    Double averageRating = vetReviews.stream().mapToInt(VetReview::getRating).average().orElse(0d);
    return VetReviewScore.builder()
        .averageRating(averageRating)
        .firstName(firstName)
        .lastName(lastName)
        .build();
  }

  /**
   * Saves a new review for a veterinarian.
   *
   * <p>Ensures the vet exists, maps the request to a {@link VetReview} entity, populates the vetId,
   * and persists the review within a MongoDB transaction. Throws a runtime exception if the
   * reviewer equals “Mike berman” to simulate an error.
   *
   * @param request the {@link CreateVetReviewRequest} containing review details
   * @return the persisted {@link VetReview} document
   */
  @Transactional(value = "mongoTx")
  public VetReview saveReview(CreateVetReviewRequest request) {
    /*Check Vet exists*/
    Vet vet = vetService.findByFirstAndLastName(request.getFirstName(), request.getLastName());
    /*Map to VetReview*/
    VetReview vetReview = mapper.toVetReview(request);
    vetReview.setVetId(vet.getId());
    VetReview review = vetReviewRepository.save(vetReview);
    if (review.getReviewer().equals("Mike berman")) {
      throw new RuntimeException("booom");
    }
    return review;
  }
}
