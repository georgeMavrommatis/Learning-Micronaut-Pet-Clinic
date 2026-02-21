package com.gmavrommatis.model.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

@Introspected
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VetReviewScore {

  private String firstName;

  private String lastName;

  private Double averageRating;
}
