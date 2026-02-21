package com.gmavrommatis.service;

import com.gmavrommatis.config.mongo.document.VetReview;
import com.gmavrommatis.config.mongo.repository.VetReviewRepository;
import com.gmavrommatis.mapper.CreateVetReviewRequestToVetReviewMapper;
import com.gmavrommatis.model.request.CreateVetReviewRequest;
import com.gmavrommatis.model.response.VetReviewScore;
import com.mongodb.reactivestreams.client.ClientSession;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service for managing veterinarian reviews in a reactive, non-blocking manner using MongoDB
 * transactions.
 *
 * <p>Provides methods to paginate review retrieval, calculate average review scores, and save new
 * reviews with transactional consistency.
 *
 * @author Your Name
 * @version 1.0
 */
@Singleton
@Slf4j
public class VetReviewService {

  private final ReactiveTransactionOperations<ClientSession> mongoTx;
  private final VetReviewRepository vetReviewRepository;
  private final VetService vetService;
  private final CreateVetReviewRequestToVetReviewMapper mapper;

  public VetReviewService(
      ReactiveTransactionOperations<ClientSession> mongoTx,
      VetReviewRepository vetReviewRepository,
      VetService vetService,
      CreateVetReviewRequestToVetReviewMapper mapper) {
    this.mongoTx = mongoTx;
    this.vetReviewRepository = vetReviewRepository;
    this.vetService = vetService;
    this.mapper = mapper;
  }

  /**
   * Retrieves a paginated list of {@link VetReview} documents.
   *
   * <p>Executes within a read-only MongoDB transaction and returns a {@code Mono<Page<VetReview>>}
   * containing the requested page.
   *
   * @param pageable pagination parameters (zero-based page index and page size)
   * @return a {@link Mono} emitting a {@link Page} of {@link VetReview} documents
   */
  public Mono<Page<VetReview>> findAll(Pageable pageable) {
    return Mono.from(
        mongoTx.withTransaction(
            TransactionDefinition.READ_ONLY, mongoStatus -> vetReviewRepository.findAll(pageable)));
  }

  /**
   * Computes the average review score for the veterinarian identified by the given first and last
   * name, in a read-only MongoDB transaction.
   *
   * <p>Fetches the vet, retrieves all their reviews, and calculates the average rating.
   *
   * @param firstName the first name of the reviewer whose scores to calculate
   * @param lastName the last name of the reviewer whose scores to calculate
   * @return a {@link Mono} emitting a {@link VetReviewScore} with the computed average, or empty if
   *     the vet has no reviews
   */
  public Mono<VetReviewScore> findVetRatingReactive(String firstName, String lastName) {
    return vetService
        .findByFirstAndLastName(firstName, lastName) // Mono<Vet>
        .flatMapMany(
            vet ->
                vetReviewRepository
                    .findAllByVetId(vet.getId()) // Flux<VetReview>
                    .map(VetReview::getRating) // Flux<Integer>
            )
        .collectList() // Mono<List<Integer>>
        .map(
            ratings -> {
              double avg = ratings.stream().mapToInt(Short::intValue).average().orElse(0d);
              return VetReviewScore.builder()
                  .firstName(firstName)
                  .lastName(lastName)
                  .averageRating(avg)
                  .build();
            });
  }

  /**
   * Saves a new {@link VetReview} document in a MongoDB transaction.
   *
   * <p>Ensures the vet exists, maps the request to an entity, and persists it. If the reviewer
   * equals "Mike berman", throws a runtime exception to simulate a rollback scenario.
   *
   * @param request the {@link CreateVetReviewRequest} containing review data
   * @return a {@link Mono} emitting the saved {@link VetReview}
   */
  public Mono<VetReview> saveReviewReactive(CreateVetReviewRequest request) {
    return Mono.from(
        mongoTx.withTransaction(
            TransactionDefinition.DEFAULT,
            mongoStatus -> // Mongo Default transaction
            vetService
                    .findByFirstAndLastName(request.getFirstName(), request.getLastName())
                    .flatMap(
                        vet -> {
                          VetReview r = mapper.toVetReview(request);
                          r.setVetId(vet.getId());
                          return Mono.just(r);
                        })
                    .flatMap(vetReviewRepository::save)
                    .flatMap(
                        saved -> {
                          if ("Mike berman".equals(saved.getReviewer())) {
                            // any exception here will trigger a rollback
                            return Mono.error(new RuntimeException("Intentional fail"));
                          } else {
                            return Mono.just(saved);
                          }
                        })));
  }
}
