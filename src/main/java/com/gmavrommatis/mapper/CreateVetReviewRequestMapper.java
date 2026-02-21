package com.gmavrommatis.mapper;

import com.gmavrommatis.config.mongo.document.VetReview;
import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import com.gmavrommatis.model.request.CreateVetReviewRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for CreateVetReviewRequest.
 *
 * @author Gewrgios.Mavrommatis
 */
@Mapper(componentModel = "jsr330")
public interface CreateVetReviewRequestMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "vetId", ignore = true)
  VetReview toVetReview(CreateVetReviewRequest request);

  @Mapping(target = "vetFirstName", source = "firstName")
  @Mapping(target = "vetLastName", source = "lastName")
  @Mapping(target = "reviewContent", source = "content")
  VetReviewNotificationEvent toVetReviewNotificationEvent(CreateVetReviewRequest request);
}
