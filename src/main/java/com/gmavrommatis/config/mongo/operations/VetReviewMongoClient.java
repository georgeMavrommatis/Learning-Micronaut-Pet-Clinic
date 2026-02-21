package com.gmavrommatis.config.mongo.operations;

import com.gmavrommatis.config.mongo.document.VetReview;
import com.mongodb.reactivestreams.client.MongoClient;
import jakarta.inject.Singleton;
import java.util.Objects;
import reactor.core.publisher.Flux;

/**
 * A MongoDB client for reactive retrieval of veterinarian review documents.
 *
 * <p>This singleton wraps a {@link com.mongodb.reactivestreams.client.MongoClient} and provides a
 * reactive method to fetch batched {@link org.springframework.data.mongodb.core.mapping.Document
 * VetReview} entities from the "vetReviews" collection within the "petclinic" database.
 *
 * <p>Example usage:
 *
 * <pre>
 *   @Inject
 *   VetReviewMongoClient client;
 *   client.findBatch(10, 5)
 *         .subscribe(review -> System.out.println(review.getComments()));
 * </pre>
 *
 * @author Gewrgios Mavrommatis
 */
@Singleton
public class VetReviewMongoClient {

  /** The reactive MongoDB client instance. */
  private final MongoClient mongoClient;

  /**
   * Constructs a new {@code VetReviewMongoClient} using the provided reactive Mongo client.
   *
   * @param mongoClient the reactive {@link com.mongodb.reactivestreams.client.MongoClient} to use
   * @throws NullPointerException if {@code mongoClient} is null
   */
  public VetReviewMongoClient(MongoClient mongoClient) {
    this.mongoClient = Objects.requireNonNull(mongoClient, "mongoClient must not be null");
  }

  /**
   * Fetches a batch of {@link VetReview} documents from the MongoDB collection.
   *
   * <p>This method performs a skip and limit query on the "vetReviews" collection within the
   * "petclinic" database. The returned {@link reactor.core.publisher.Flux} will emit up to {@code
   * limit} items, starting after skipping the first {@code offset} items.
   *
   * @param offset the number of documents to skip (zero-based)
   * @param limit the maximum number of documents to retrieve
   * @return a {@link reactor.core.publisher.Flux} that emits the matching {@link VetReview}
   *     instances
   * @throws IllegalArgumentException if {@code offset} or {@code limit} is negative
   */
  public Flux<VetReview> findBatch(int offset, int limit) {
    if (offset < 0 || limit < 0) {
      throw new IllegalArgumentException(
          "Offset and limit must be non-negative: offset=" + offset + ", limit=" + limit);
    }
    return Flux.from(
        mongoClient
            .getDatabase("petclinic")
            .getCollection("vetReviews", VetReview.class)
            .find()
            .skip(offset)
            .limit(limit));
  }
}
