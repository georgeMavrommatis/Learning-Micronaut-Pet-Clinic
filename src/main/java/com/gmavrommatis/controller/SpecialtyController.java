package com.gmavrommatis.controller;

import com.gmavrommatis.config.r2dbc.domain.Specialty;
import com.gmavrommatis.model.request.SpecialtyRequest;
import com.gmavrommatis.model.response.SpecialtyResponse;
import com.gmavrommatis.service.SpecialtyService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller that handles CRUD operations for {@code Specialty}.
 *
 * @author GewrgiosMmavrommatis
 * @version 1.0
 */
@Controller("/specialty")
@Slf4j
public class SpecialtyController {

  private final SpecialtyService specialtyService;

  public SpecialtyController(SpecialtyService specialtyService) {
    this.specialtyService = specialtyService;
  }

  /**
   * Retrieves all specialties in a synchronous, blocking manner.
   *
   * @return an {@link HttpResponse} with status 201 Created and a list of specialties
   */
  @Get
  public HttpResponse<List<SpecialtyResponse>> findAll() {
    log.info("SpecialtyController findAll");
    List<Specialty> specialties =
        Optional.ofNullable(specialtyService.findAll().collectList().block()).orElse(List.of());

    if (specialties.isEmpty()) {
      throw new RuntimeException("No specialties found");
    }

    return HttpResponse.created(
        specialties.stream()
            .map(specialty -> SpecialtyResponse.builder().name(specialty.getName()).build())
            .toList());
  }

  /**
   * Creates a new specialty with the given name.
   *
   * @param request the {@link SpecialtyRequest} containing the name of the specialty to create
   * @return an {@link HttpResponse} with status 201 Created and the created {@code
   *     SpecialtyResponse}
   */
  @Post
  public HttpResponse<SpecialtyResponse> create(@Body SpecialtyRequest request) {
    Specialty created = specialtyService.create(request.getName());
    return HttpResponse.created(SpecialtyResponse.builder().name(created.getName()).build());
  }

  /**
   * Renames an existing specialty.
   *
   * @param name the current name of the specialty to rename
   * @param request the {@link SpecialtyRequest} containing the new name
   * @return an {@link HttpResponse} with status 200 OK and the updated {@code SpecialtyResponse}
   */
  @Put("/{name}")
  public HttpResponse<SpecialtyResponse> rename(
      @PathVariable String name, @Body SpecialtyRequest request) {

    try {
      Specialty updated = specialtyService.update(name, request.getName());
      return HttpResponse.ok(SpecialtyResponse.builder().name(updated.getName()).build());
    } catch (Exception e) {
      throw e;
    }
  }
}
