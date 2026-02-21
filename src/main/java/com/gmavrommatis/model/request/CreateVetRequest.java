package com.gmavrommatis.model.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Introspected
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateVetRequest {

  @NotBlank private String firstName;

  @NotBlank private String lastName;

  @Size(min = 1, max = 100)
  private Set<String> specialties = new HashSet<>();
}
