package com.gmavrommatis.mapper;

import com.gmavrommatis.config.mongo.document.VetReview;
import com.gmavrommatis.model.request.CreateVetReviewRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper that converts a {@link CreateVetReviewRequest} DTO into a {@link VetReview} entity.
 *
 * <p>This mapping ignores the {@code id} and {@code vetId} fields on the resulting entity, as those
 * are generated or assigned elsewhere in the application.
 *
 * <p>Implemented at compile time by MapStruct.
 *
 * @author Your Name
 */
@Mapper(componentModel = "jsr330")
public interface CreateVetReviewRequestToVetReviewMapper {

  /**
   * Maps the fields from the given {@link CreateVetReviewRequest} into a new {@link VetReview}
   * instance.
   *
   * <p>The target fields {@code id} and {@code vetId} are ignored and must be set by the caller.
   *
   * @param request the request DTO containing reviewer name, content, and rating
   * @return a {@code VetReview} entity populated with values from the request, except for {@code
   *     id} and {@code vetId}
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "vetId", ignore = true)
  VetReview toVetReview(CreateVetReviewRequest request);
}
