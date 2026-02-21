package com.gmavrommatis.mapper;

import com.gmavrommatis.config.mongo.document.VetReview;
import com.gmavrommatis.model.response.WebSocketVetReviewResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "jsr330")
public interface VetReviewToWebSocketVetReviewResponseMapper {

  @Mapping(target = "webSocketMetadataResponse", ignore = true)
  WebSocketVetReviewResponse toWebSocketVetReviewResponse(VetReview vetReview);

  List<WebSocketVetReviewResponse> toWebSocketVetReviewResponseList(List<VetReview> vetReviews);
}
