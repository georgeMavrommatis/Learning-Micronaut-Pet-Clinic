package com.gmavrommatis.model.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Introspected
@Serdeable
@Data
@Builder
public class VetResponse {
  private String firstName;
  private String lastName;
  private List<SpecialtyResponse> specialties;
  private Integer page;
  private Integer size;
  private Integer totalPages;
  private Long totalElements;
}
