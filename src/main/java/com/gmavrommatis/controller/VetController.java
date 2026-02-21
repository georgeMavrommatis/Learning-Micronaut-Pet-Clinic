package com.gmavrommatis.controller;

import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.mapper.VetToVetResponseMapper;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import com.gmavrommatis.model.response.VetResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;

/**
 * REST controller for managing veterinarian records.
 *
 * @author GewrgiosMmavrommatis
 */
@Controller("/vet")
public class VetController {

  private final VetService vetService;
  private final VetToVetResponseMapper vetToVetResponseMapper;

  public VetController(VetService vetService, VetToVetResponseMapper vetToVetResponseMapper) {
    this.vetService = vetService;
    this.vetToVetResponseMapper = vetToVetResponseMapper;
  }

  /**
   * Retrieves all {@link Vet} records from the system and returns them as JSON-safe {@link
   * VetResponse} objects.
   *
   * <p>This endpoint fetches all stored veterinarians via the service layer, maps each entity to an
   * immutable DTO representation using {@link VetToVetResponseMapper}, and returns the resulting
   * list wrapped in an HTTP 200 response. No pagination or filtering is applied.
   *
   * @return an {@link HttpResponse} containing a list of {@link VetResponse} elements, never {@code
   *     null}. The list may be empty if no vets are registered.
   */
  @Get
  public HttpResponse<List<VetResponse>> vetDetails() {
    List<Vet> vets = vetService.findAll();

    List<VetResponse> responses = vets.stream().map(vetToVetResponseMapper::toVetResponse).toList();

    return HttpResponse.ok(responses);
  }

  /**
   * Creates a new veterinarian record along with specialties.
   *
   * @param request the {@link CreateVetRequest} containing the vet’s details and specialty names
   * @return 200 OK with the created vet DTO
   */
  @Post
  public HttpResponse<VetResponse> create(@Body CreateVetRequest request) {
    VetResponse response = vetService.createVetWithSpecialties(request);
    return HttpResponse.ok(response);
  }

  @Post("/related")
  public HttpResponse<VetResponse> createRelated(@Body CreateVetRequest request) {
    VetResponse response = vetService.createVet(request);
    return HttpResponse.ok(response);
  }

  /**
   * Deletes a veterinarian by first and last name.
   *
   * @param firstName the vet’s first name
   * @param lastName the vet’s last name
   * @return 204 No Content if deletion succeeds, 404 if no vet found
   */
  @Delete("/{firstName}/{lastName}")
  public MutableHttpResponse<Object> deleteByName(
      @PathVariable String firstName, @PathVariable String lastName) {
    vetService.deleteByNameWithCascade(firstName, lastName);
    return HttpResponse.noContent(); // 204 on success
  }

  /**
   * Updates an existing veterinarian’s details.
   *
   * @param page zero-based page index (not used in this method)
   * @param size page size (not used in this method)
   * @param firstName the current first name of the vet to update
   * @param lastName the current last name of the vet to update
   * @param request the {@link UpdateVetRequest} containing fields to update
   * @return 200 OK with the updated vet DTO, or 404 if vet not found
   */
  @Put("/{firstName}/{lastName}")
  public HttpResponse<VetResponse> update(
      @QueryValue(defaultValue = "0") int page,
      @QueryValue(defaultValue = "10") int size,
      @PathVariable String firstName,
      @PathVariable String lastName,
      @Body UpdateVetRequest request) {

    try {
      VetResponse response = vetService.updateVetByName(firstName, lastName, request);
      return HttpResponse.ok(response);
    } catch (RuntimeException e) {
      return HttpResponse.notFound();
    }
  }

  /**
   * Searches for veterinarians whose last name equals the given lastName and who have at least one
   * of the specified specialties.
   *
   * @param lastName the lastName to match
   * @param specialtyNames the list of specialty names; only vets with at least one matching
   *     specialty are returned
   * @return 200 OK with matching vets, or 404 if none found
   */
  @Get("/{lastName}/{specialtyNames}")
  public HttpResponse<List<VetResponse>> findByLastNameAndSpecialties(
      @PathVariable String lastName, @PathVariable List<String> specialtyNames) {

    List<VetResponse> response = vetService.findByLastName(lastName, specialtyNames);

    if (response == null || response.isEmpty()) {
      return HttpResponse.notFound();
    }

    return HttpResponse.ok(response);
  }
}
