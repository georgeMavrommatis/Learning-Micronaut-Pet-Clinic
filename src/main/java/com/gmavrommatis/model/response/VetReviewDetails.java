package com.gmavrommatis.model.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.*;

@Introspected
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VetReviewDetails {

  private List<VetReviewResponse> vetReviews;

  private int page;
  private int size;
  private int totalPages;
  private long totalElements;
}
