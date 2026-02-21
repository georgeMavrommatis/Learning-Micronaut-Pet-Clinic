package com.gmavrommatis.controller;

import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.PetClinicService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

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
   * Retrieves paginated Pet Clinic details.
   *
   * <p>Supports paging via {@code page} (zero-based index) and {@code size} (items per page).
   * Delegates to the service to fetch a {@link PetClinicResponse} containing the requested page of
   * vet data along with pagination metadata.
   *
   * @param page zero-based page index (defaults to 0 if not specified)
   * @param size the number of items per page (defaults to 10 if not specified)
   * @return an {@code HttpResponse} containing the {@link PetClinicResponse} for the requested page
   */
  @Get("/details")
  public HttpResponse<PetClinicResponse> petClinicDetails(
      @QueryValue(defaultValue = "0") int page, @QueryValue(defaultValue = "10") int size) {
    return HttpResponse.ok(petClinicService.getPetClinicDetails(Pageable.from(page, size)));
  }
}
