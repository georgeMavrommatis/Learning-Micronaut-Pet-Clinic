package com.gmavrommatis.controller;

import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import jakarta.annotation.security.RolesAllowed;
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
@RolesAllowed({"ADMIN"})
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
  public Mono<HttpResponse<PetClinicResponse>> petClinicDetails(
      @QueryValue(defaultValue = "0") int page, @QueryValue(defaultValue = "10") int size) {
    log.info("PetClinicController petClinicDetails");
    return vetService
        .getVetsWithSpecialties(Pageable.from(page, size))
        .map(
            petClinicResponse ->
                HttpResponse.ok(petClinicResponse)
                    .header("page", String.valueOf(page))
                    .header("size", String.valueOf(size)));
  }
}
