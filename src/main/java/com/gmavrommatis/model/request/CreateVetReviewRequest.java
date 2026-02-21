package com.gmavrommatis.model.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.*;
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

  @NotBlank private String firstName;

  @NotBlank private String lastName;

  @NotBlank private String reviewer;

  @NotBlank private String content;

  @NotNull
  @Min(1)
  @Max(9)
  /*only check fraction for float double BigDecimal etc.
  because by the time Bean Validation runs, this  property will already be a Short/Integer etc., and
  any fractional part has been thrown away*/
  // (client needs to know what is expected)
  @Digits(integer = 2, fraction = 1)
  private Float rating;
}
