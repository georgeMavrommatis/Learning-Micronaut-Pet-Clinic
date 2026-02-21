package com.gmavrommatis.controller;

import com.gmavrommatis.config.domain.l1.Vet;
import com.gmavrommatis.mapper.VetToVetResponseMapper;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import com.gmavrommatis.model.response.VetResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

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
   * Retrieves all veterinarians.
   *
   * @return a list of {@link VetResponse}
   */
  @Get
  public HttpResponse<List<VetResponse>> vetDetails() {
    return HttpResponse.ok(vetToVetResponseMapper.toVetResponseLazyList(vetService.findAll()));
  }

  /**
   * Retrieves veterinarians whose last names start with the given prefix and who have at least one
   * of the specified specialties.
   *
   * @param lastNamePrefix the prefix to match against each vet’s last name (case-sensitive)
   * @param specialtyNames a list of specialty names (parsed from a comma-separated path variable);
   *     only vets possessing at least one of these specialties are returned
   * @return an {@code HttpResponse} containing a list of fully populated {@link VetResponse} DTOs
   *     for vets matching the criteria
   */
  @Get("/{lastNamePrefix}/{specialtyNames}")
  public HttpResponse<List<VetResponse>> findByLastNameAndSpecialties(
      @PathVariable String lastNamePrefix,
      @PathVariable List<String> specialtyNames // Micronaut will split “1,2,5” → [1,2,5]
      ) {
    /*With Criteria*/
    List<Vet> vets =
        vetService.findByLastNameAndSpecialties(
            lastNamePrefix, specialtyNames // “false” = match *any* specialty
            );
    return HttpResponse.ok(vetToVetResponseMapper.toVetResponseEagerList(vets));
  }

  /**
   * Creates a new veterinarian record.
   *
   * @param request the {@link CreateVetRequest} containing vet details
   * @return an {@code HttpResponse} with status 201 Created and the created {@link VetResponse}
   */
  @Post
  public HttpResponse<VetResponse> create(@Body CreateVetRequest request) {
    Vet created = vetService.createVet(request);
    return HttpResponse.created(vetToVetResponseMapper.toVetResponseEager(created));
  }

  /**
   * Deletes a veterinarian by first and last name.
   *
   * <p>On success, returns a 200 OK response containing the number of deleted records. If no
   * matching veterinarian is found, returns a 400 Bad Request response with an error message.
   *
   * @param firstName the veterinarian's first name
   * @param lastName the veterinarian's last name
   * @return an {@code HttpResponse<String>} with:
   *     <ul>
   *       <li>200 OK and the count of deleted veterinarians as the response body
   *       <li>400 Bad Request and an error message if no matching veterinarian is found
   *     </ul>
   */
  @Delete("/{firstName}/{lastName}")
  public HttpResponse<String> deleteByName(
      @PathVariable String firstName, @PathVariable String lastName) {
    try {
      return HttpResponse.ok(String.valueOf(vetService.deleteByName(firstName, lastName)));
    } catch (NoSuchElementException e) {
      return HttpResponse.badRequest("Vet not found: " + firstName + " " + lastName);
    }
  }

  /**
   * Updates an existing veterinarian's details.
   *
   * @param firstName the current first name of the vet to update
   * @param lastName the current last name of the vet to update
   * @param request the {@link UpdateVetRequest} containing updated fields
   * @return an {@code HttpResponse} containing the updated {@link VetResponse}, or 400 badRequest
   *     if no matching vet exists
   */
  @Put("/{firstName}/{lastName}")
  @Transactional
  public HttpResponse<?> update(
      @PathVariable String firstName,
      @PathVariable String lastName,
      @Body UpdateVetRequest request) {

    try {
      Vet updated = vetService.updateVetByName(firstName, lastName, request);
      return HttpResponse.ok(vetToVetResponseMapper.toVetResponseEager(updated));
    } catch (NoSuchElementException e) {
      return HttpResponse.badRequest(e.getMessage());
    }
  }
}
