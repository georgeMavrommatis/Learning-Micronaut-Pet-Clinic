package com.gmavrommatis.controller;

import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for fetching Pet Clinic details.
 *
 * @author GewrgiosMmavrommatis
 */
@Slf4j
@Controller("/pet-clinic")
public class PetClinicController {

  private final VetService vetService;

  public PetClinicController(VetService vetService) {
    this.vetService = vetService;
  }

  /**
   * Returns a paged list of veterinarians with their specialties using the standard (non-related)
   * retrieval method provided by {@link
   * VetService#getVetsWithSpecialties(io.micronaut.data.model.Pageable)}.
   *
   * <p>Query parameters:
   *
   * <ul>
   *   <li>{@code page} — zero-based page index (default: {@code 0}).
   *   <li>{@code size} — page size (default: {@code 10}).
   * </ul>
   *
   * <p>Behavior:
   *
   * <ul>
   *   <li>Returns {@code HttpResponse.ok(...)} with a {@link PetClinicResponse} containing page
   *       data on success.
   *   <li>Validation or data-access errors will propagate as runtime exceptions — handle them via
   *       exception handlers if you need custom responses.
   * </ul>
   *
   * @param page zero-based page index; provided via {@code ?page=...} (default {@code 0})
   * @param size page size; provided via {@code ?size=...} (default {@code 10})
   * @return {@code HttpResponse.ok(PetClinicResponse)} containing the requested page of vets and
   *     their specialties
   */
  @Get("/details")
  public HttpResponse<PetClinicResponse> petClinicDetails(
      @QueryValue(defaultValue = "0") int page, @QueryValue(defaultValue = "10") int size) {
    return HttpResponse.ok(vetService.getVetsWithSpecialties(Pageable.from(page, size)));
  }

  /**
   * Returns a paged list of veterinarians using the "related" retrieval method ({@link
   * VetService#findAllRelatedPageable(io.micronaut.data.model.Pageable)}), which may differ in
   * fetching strategy (for example, fetch-join or different repository).
   *
   * <p>Query parameters and behavior are identical to {@link #petClinicDetails(int,int)}; the
   * difference is the service method invoked — choose the endpoint depending on the repository
   * strategy you want to exercise.
   *
   * @param page zero-based page index; provided via {@code ?page=...} (default {@code 0})
   * @param size page size; provided via {@code ?size=...} (default {@code 10})
   * @return {@code HttpResponse.ok(PetClinicResponse)} containing the requested page of vets and
   *     their specialties
   */
  @Get("/detailsRelated")
  public HttpResponse<PetClinicResponse> petClinicDetailsRelated(
      @QueryValue(defaultValue = "0") int page, @QueryValue(defaultValue = "10") int size) {
    return HttpResponse.ok(vetService.findAllRelatedPageable(Pageable.from(page, size)));
  }
}
