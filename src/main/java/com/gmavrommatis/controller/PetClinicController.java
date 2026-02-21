package com.gmavrommatis.controller;

import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.PetClinicService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import reactor.core.publisher.Mono;

/**
 * REST controller for fetching Pet Clinic details.
 *
 * @author GewrgiosMmavrommatis
 */
@Controller("/pet-clinic")
public class PetClinicController {

  private final PetClinicService petClinicService;

  public PetClinicController(PetClinicService petClinicService) {
    this.petClinicService = petClinicService;
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
    return petClinicService.getPetClinicDetails(Pageable.from(page, size)).map(HttpResponse::ok);
  }
}
