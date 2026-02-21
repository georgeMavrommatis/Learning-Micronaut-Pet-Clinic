package com.gmavrommatis.controller;

import com.gmavrommatis.mapper.VetToVetResponseMapper;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import com.gmavrommatis.model.response.VetResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing veterinarian records.
 *
 * @author GewrgiosMmavrommatis
 */
@Controller("/vetCustom")
public class VetController {

  private final VetService vetService;
  private final VetToVetResponseMapper vetToVetResponseMapper;

  public VetController(VetService vetService, VetToVetResponseMapper vetToVetResponseMapper) {
    this.vetService = vetService;
    this.vetToVetResponseMapper = vetToVetResponseMapper;
  }

  /**
   * Retrieves a paginated list of veterinarians in a reactive, non-blocking fashion.
   *
   * @param page zero-based page index (defaults to 0 if not specified)
   * @param size the maximum number of items per page (defaults to 10 if not specified)
   * @return a {@link Mono} emitting an {@link HttpResponse} containing a {@code List<VetResponse>}
   *     and HTTP status 200 OK
   */
  @Get
  public Mono<HttpResponse<List<VetResponse>>> vetDetails(
      @QueryValue(defaultValue = "0") int page, @QueryValue(defaultValue = "10") int size) {
    return vetService
        .findAllPageable(Pageable.from(page, size))
        .map(vetToVetResponseMapper::toVetResponse)
        .collectList()
        .map(HttpResponse::ok);
  }

  /**
   * Searches for veterinarians whose last name equals given lastName and who have at least one of
   * the specified specialties.
   *
   * @param lastName the lastName to match against each vet’s last name.
   * @param specialtyNames the list of specialty names; only vets with at least one matching
   *     specialty are returned
   * @return a {@code Mono<HttpResponse<List<VetResponse>>>} emitting:
   *     <ul>
   *       <li>200 OK with the list of matching vet DTOs when successful
   *     </ul>
   */
  @Get("/{lastName}/{specialtyNames}")
  public Mono<HttpResponse<List<VetResponse>>> findByLastNameAndSpecialties(
      @PathVariable String lastName,
      @PathVariable List<String> specialtyNames // Micronaut will split “1,2,5” → [1,2,5]
      ) {

    return vetService
        .findByLastName(lastName, specialtyNames)
        .collectList() // Mono<List<VetResponse>>
        .map(HttpResponse::ok);
  }

  /**
   * Creates a new veterinarian record.
   *
   * @param request the {@link CreateVetRequest} containing the vet’s details and specialty names
   * @return a {@code Mono<HttpResponse<VetResponse>>} emitting:
   *     <ul>
   *       <li>200 OK with the created vet DTO when successful
   *     </ul>
   */
  @Post
  public Mono<HttpResponse<VetResponse>> create(@Body @Valid CreateVetRequest request) {
    return vetService.createVetWithSpecialties(request).map(HttpResponse::ok);
  }

  /**
   * Deletes a veterinarian by first and last name in a reactive, non-blocking manner.
   *
   * @param firstName the vet’s first name
   * @param lastName the vet’s last name
   * @return a {@code Mono<MutableHttpResponse<Object>>} emitting:
   *     <ul>
   *       <li>204 No Content when a record was deleted
   *     </ul>
   */
  @Delete("/{firstName}/{lastName}")
  public Mono<MutableHttpResponse<Object>> deleteByName(
      @PathVariable String firstName, @PathVariable String lastName) {
    return vetService
        .deleteByNameWithCascade(firstName, lastName)
        // On success, emit a 204 No Content
        .thenReturn(HttpResponse.noContent())
        // If the Vet wasn’t found, emit a 404 with a message
        .onErrorMap(
            Exception.class, // we can specify subclass exceptions instead
            e -> e);
  }

  /**
   * Updates an existing veterinarian’s details in a reactive, non-blocking manner.
   *
   * @param page zero-based page index (default 0, not used in this method)
   * @param size page size (default 10, not used in this method)
   * @param firstName the current first name of the vet to update
   * @param lastName the current last name of the vet to update
   * @param request the {@link UpdateVetRequest} containing fields to update
   * @return a {@code Flux<HttpResponse<VetResponse>>>} emitting:
   *     <ul>
   *       <li>200 OK with the updated vet DTO when successful
   *     </ul>
   */
  @Put("/{firstName}/{lastName}")
  public Mono<HttpResponse<VetResponse>> update(
      @QueryValue(defaultValue = "0") int page,
      @QueryValue(defaultValue = "10") int size,
      @PathVariable String firstName,
      @PathVariable String lastName,
      @Body @Valid UpdateVetRequest request) {

    return vetService
        .updateVetByName(firstName, lastName, request)
        // wrap in 200 OK
        .map(HttpResponse::ok);
  }
}
