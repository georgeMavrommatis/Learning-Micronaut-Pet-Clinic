package com.gmavrommatis.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Introspected
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@Schema(
    name = "PetClinicResponse",
    description = "Paginated response containing vets and pagination metadata")
public class PetClinicResponse {

  @Schema(
      description = "List of vets with their specialties",
      implementation = VetResponse.class) // ensure VetResponse is also annotated
  private List<VetResponse> vets;

  @Schema(description = "Zero-based page index", example = "0")
  private int page;

  @Schema(description = "Number of items per page", example = "10")
  private int size;

  @Schema(description = "Total number of pages available", example = "5")
  private int totalPages;

  @Schema(description = "Total number of elements across all pages", example = "47")
  private long totalElements;
}
