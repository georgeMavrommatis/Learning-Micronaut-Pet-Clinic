package com.gmavrommatis.controller;

import com.gmavrommatis.api.VetApi;
import com.gmavrommatis.mapper.VetToVetResponseMapper;
import com.gmavrommatis.model.CreateVetRequest;
import com.gmavrommatis.model.UpdateVetRequest;
import com.gmavrommatis.model.VetResponse;
import com.gmavrommatis.service.VetService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import reactor.core.publisher.Mono;

@Controller
public class VetControllerOpenaAPI implements VetApi {

  private final VetService vetService;
  private final VetToVetResponseMapper vetToVetResponseMapper;

  public VetControllerOpenaAPI(
      VetService vetService, VetToVetResponseMapper vetToVetResponseMapper) {
    this.vetService = vetService;
    this.vetToVetResponseMapper = vetToVetResponseMapper;
  }

  @Override
  public Mono<@NotNull List<@Valid VetResponse>> vetDetails(Integer page, Integer size) {
    return vetService
        .findAllPageable(Pageable.from(page, size))
        .map(vetToVetResponseMapper::toVetResponseOpenApi)
        .collectList();
  }

  @Override
  public Mono<@Valid VetResponse> createVet(CreateVetRequest createVetRequest) {
    // todo refactor
    return null;
  }

  @Override
  public Mono<HttpResponse<Void>> deleteByName(String firstName, String lastName) {
    // todo refactor
    return null;
  }

  @Override
  public Mono<@NotNull List<@Valid VetResponse>> findByLastNameAndSpecialties(
      String lastName, List<@NotNull String> specialtyNames) {
    // todo refactor
    return null;
  }

  @Override
  public Mono<@Valid VetResponse> updateVet(
      String firstName,
      String lastName,
      UpdateVetRequest updateVetRequest,
      Integer page,
      Integer size) {
    // todo refactor
    return null;
  }
}
