package com.gmavrommatis.config.mongo.repository;

import com.gmavrommatis.config.mongo.document.VetReview;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.PageableRepository;
import java.util.List;

/**
 * Repository interface for {@link VetReview} documents in MongoDB.
 *
 * <p>Extends {@link PageableRepository} to provide paging support for review queries. Uses
 * Micronaut Data to derive queries from method names.
 *
 * @author Your Name
 */
@MongoRepository
public interface VetReviewRepository extends PageableRepository<VetReview, String> {

  /**
   * Retrieves all reviews for the specified veterinarian.
   *
   * <p>Derives a MongoDB query to find all {@link VetReview} documents where the {@code vetId}
   * field matches the given value.
   *
   * @param vetId the identifier of the veterinarian whose reviews to fetch
   * @return a {@link List} of {@code VetReview} objects for the given vet, or an empty list if none
   *     are found
   */
  List<VetReview> findAllByVetId(Long vetId);

  /**
   * Retrieves all {@link VetReview} documents for a given veterinarian whose {@code rating} is
   * strictly greater than the specified minimum.
   *
   * <p>This method uses a custom MongoDB filter via {@code @MongoFindQuery}, matching on the {@code
   * vetId} field and {@code rating > minRating}, then sorts the results in descending order by
   * rating.
   *
   * @param vetId the identifier of the veterinarian whose reviews to fetch
   * @param minRating the exclusive lower bound for the review rating
   * @return a {@link List} of {@code VetReview} objects matching the criteria, ordered by highest
   *     rating first
   */
  @MongoFindQuery(
      filter = "{ vetId: :vetId, rating: { $gt: :minRating } }",
      sort = "{ rating: -1 }" // optional
      )
  List<VetReview> findReviewsAboveRating(Long vetId, short minRating);
}
