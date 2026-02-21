package com.gmavrommatis.controller;

import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * REST controller for fetching Pet Clinic details.
 *
 * @author GewrgiosMmavrommatis
 * @version 1.0
 */
@Slf4j
@Controller("/pet-clinic")
@Tag(name = "Pet Clinic", description = "Operations related to pet clinic and vets")
public class PetClinicController {

  private final VetService vetService;

  public PetClinicController(VetService vetService) {
    this.vetService = vetService;
  }

  /**
   * Retrieves paginated Pet Clinic details in a non-blocking, reactive manner.
   *
   * <p>Supports pagination via the {@code page} (zero-based index) and {@code size} (items per
   * page) query parameters. Delegates to the service layer to fetch a {@link PetClinicResponse}
   * wrapped in a {@code Mono}, then maps it to an {@code HttpResponse} with status 200 OK.
   *
   * @param page zero-based page index (defaults to 0 if not specified)
   * @param size the maximum number of items per page (defaults to 10 if not specified)
   * @return a {@code Mono<HttpResponse<PetClinicResponse>>} that, when subscribed to, emits an HTTP
   *     200 OK response containing the paginated clinic data
   */
  @Get("/details")
  @Operation(
      summary = "Get pet clinic details (paginated)",
      description =
          "Returns paginated pet clinic data (vets with specialties). Supports `page` (0-based) and `size`.",
      tags = {"Pet Clinic"})
  @ApiResponse(
      responseCode = "200",
      description = "Successful response with paginated PetClinicResponse",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = PetClinicResponse.class))) // ,
  @ApiResponse(responseCode = "400", description = "Invalid pagination parameters") // ,
  @ApiResponse(responseCode = "500", description = "Internal server error")
  public Mono<HttpResponse<PetClinicResponse>> petClinicDetails(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "page",
              description = "Zero-based page index",
              example = "0",
              schema = @Schema(type = "integer", defaultValue = "0"))
          @QueryValue(defaultValue = "0")
          int page,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "size",
              description = "Number of items per page",
              example = "10",
              schema = @Schema(type = "integer", defaultValue = "10"))
          @QueryValue(defaultValue = "10")
          int size) {
    String threadName = Thread.currentThread().getName();
    String pool = threadName.contains("nioEventLoopGroup") ? "EVENT-LOOP" : "WORKER";
    log.info("→ executed on {}", pool);
    return vetService
        .getVetsWithSpecialties(Pageable.from(page, size))
        .map(
            petClinicResponse ->
                HttpResponse.ok(petClinicResponse)
                    .header("page", String.valueOf(page))
                    .header("size", String.valueOf(size)));
  }
}
