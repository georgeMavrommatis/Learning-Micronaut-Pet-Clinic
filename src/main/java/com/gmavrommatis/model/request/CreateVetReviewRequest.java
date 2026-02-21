package com.gmavrommatis.model.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Introspected
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateVetReviewRequest {

  private String firstName;

  private String lastName;

  private String reviewer;

  private String content;

  private short rating;
}
