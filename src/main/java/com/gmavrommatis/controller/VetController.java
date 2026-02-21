package com.gmavrommatis.controller;

import com.gmavrommatis.mapper.VetToVetResponseMapper;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import com.gmavrommatis.model.response.VetResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing veterinarian records.
 *
 * @author GewrgiosMmavrommatis
 * @version 1.0
 */
@Controller("/vet")
public class VetController {

  private final VetService vetService;
  private final VetToVetResponseMapper vetToVetResponseMapper;

  public VetController(VetService vetService, VetToVetResponseMapper vetToVetResponseMapper) {
    this.vetService = vetService;
    this.vetToVetResponseMapper = vetToVetResponseMapper;
  }

  @Get
  public Mono<HttpResponse<List<VetResponse>>> vetDetails() throws Exception {
    return vetService
        .findAll()
        .map(vetToVetResponseMapper::toVetResponseEager)
        .collectList()
        .map(HttpResponse::ok);
  }

  /**
   * Searches for veterinarians whose last name starts with the given prefix and who have at least
   * one of the specified specialties.
   *
   * @param lastNamePrefix the prefix to match against each vet’s last name (exact prefix match)
   * @param specialtyNames the list of specialty names; only vets with at least one matching
   *     specialty are returned
   * @return a {@code Mono<HttpResponse<List<VetResponse>>>} emitting:
   *     <ul>
   *       <li>200 OK with the list of matching vet DTOs when successful
   *     </ul>
   */
  @Get("/{lastNamePrefix}/{specialtyNames}")
  public Mono<HttpResponse<List<VetResponse>>> findByLastNameAndSpecialties(
      @PathVariable String lastNamePrefix,
      @PathVariable List<String> specialtyNames // Micronaut will split “1,2,5” → [1,2,5]
      ) {
    /*With Query*/
    return vetService
        .findByLastNameAndSpecialtiesByQuery(lastNamePrefix, specialtyNames)
        .map(vetToVetResponseMapper::toVetResponseEager)
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
  public Mono<HttpResponse<VetResponse>> create(@Body CreateVetRequest request) {
    return vetService
        .createVet(request)
        .map(vetToVetResponseMapper::toVetResponseEager)
        .map(HttpResponse::ok);
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
        .deleteByName(firstName, lastName)
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
  public Flux<HttpResponse<VetResponse>> update(
      @QueryValue(defaultValue = "0") int page,
      @QueryValue(defaultValue = "10") int size,
      @PathVariable String firstName,
      @PathVariable String lastName,
      @Body UpdateVetRequest request) {

    return vetService
        .updateVetByName(firstName, lastName, request)
        // map the domain Vet -> DTO
        .map(vetToVetResponseMapper::toVetResponseEager)
        // wrap in 200 OK
        .map(HttpResponse::ok);
  }
}
