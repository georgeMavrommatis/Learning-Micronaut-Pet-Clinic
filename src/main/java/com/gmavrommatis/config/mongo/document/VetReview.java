package com.gmavrommatis.config.mongo.document;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a review submitted for a veterinarian.
 *
 * @author Your Name
 */
@Introspected
@Serdeable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedEntity(value = "vetReviews")
public class VetReview {

  /**
   * The unique identifier for this review.
   *
   * <p>Generated automatically when the entity is saved.
   */
  @Id @GeneratedValue private String id;

  /**
   * The identifier of the veterinarian being reviewed.
   *
   * <p>References the primary key of the {@link com.gmavrommatis.config.domain.Vet Vet} entity.
   */
  private Long vetId;

  /** The name of the person who submitted the review. */
  private String reviewer;

  /** The textual content of the review. */
  private String content;

  /**
   * The rating given by the reviewer.
   *
   * <p>Expected to be on a scale (e.g., 1–5).
   */
  private short rating;

  /**
   * The date the review was created.
   *
   * <p>Automatically populated when the entity is inserted.
   */
  @DateCreated private LocalDate date;
}
