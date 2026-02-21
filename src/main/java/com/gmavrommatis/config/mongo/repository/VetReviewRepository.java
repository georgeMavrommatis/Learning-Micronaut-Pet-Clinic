package com.gmavrommatis.config.mongo.repository;

import com.gmavrommatis.config.mongo.document.VetReview;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.reactive.ReactorPageableRepository;
import reactor.core.publisher.Flux;

/**
 * Reactive MongoDB repository for {@link VetReview} documents, supporting pagination.
 *
 * <p>Extends {@link ReactorPageableRepository} to provide CRUD operations and paginated queries in
 * a non-blocking, reactive fashion.
 *
 * @author Your Name
 * @version 1.0
 */
@MongoRepository
public interface VetReviewRepository extends ReactorPageableRepository<VetReview, String> {

  /**
   * Retrieves all reviews for the specified veterinarian.
   *
   * <p>Derives a query to find all {@code VetReview} documents where the {@code vetId} field
   * matches the given value. Returns a reactive stream of matching reviews.
   *
   * @param vetId the unique identifier of the veterinarian whose reviews are to be fetched
   * @return a {@link Flux} emitting each matching {@code VetReview}, or completing empty if none
   *     are found
   */
  Flux<VetReview> findAllByVetId(Long vetId);
}
