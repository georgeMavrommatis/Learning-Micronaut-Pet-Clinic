package com.gmavrommatis.mapper;

import com.gmavrommatis.config.mongo.document.VetReview;
import com.gmavrommatis.model.response.VetReviewResponse;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting {@link VetReview} entities into {@link VetReviewResponse} DTOs.
 *
 * <p>Provides methods for mapping a single review as well as lists of review entities. Implemented
 * at compile time by MapStruct.
 *
 * @author Your Name
 */
@Mapper(componentModel = "jsr330")
public interface VetReviewToVetReviewResponseMapper {

  /**
   * Converts a single {@link VetReview} entity into a {@link VetReviewResponse} DTO.
   *
   * @param vetReview the review entity to map; may be {@code null}
   * @return the corresponding {@code VetReviewResponse} DTO, or {@code null} if the input was
   *     {@code null}
   */
  VetReviewResponse toVetReviewResponse(VetReview vetReview);

  /**
   * Converts a list of {@link VetReview} entities into a list of {@link VetReviewResponse} DTOs.
   *
   * @param vetReviews the list of review entities to map; may be {@code null}
   * @return a list of {@code VetReviewResponse} DTOs, or {@code null} if the input list was {@code
   *     null}
   */
  List<VetReviewResponse> toVetReviewResponseList(List<VetReview> vetReviews);
}
