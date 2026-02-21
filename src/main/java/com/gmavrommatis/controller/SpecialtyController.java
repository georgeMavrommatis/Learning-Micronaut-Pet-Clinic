package com.gmavrommatis.controller;

import com.gmavrommatis.config.domain.Specialty;
import com.gmavrommatis.model.request.SpecialtyRequest;
import com.gmavrommatis.model.response.SpecialtyResponse;
import com.gmavrommatis.service.SpecialtyService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST controller that handles CRUD operations for {@code Specialty}.
 *
 * @author GewrgiosMmavrommatis
 */
@Controller("/specialty")
public class SpecialtyController {

  private final SpecialtyService specialtyService;

  public SpecialtyController(SpecialtyService specialtyService) {
    this.specialtyService = specialtyService;
  }

  /**
   * Retrieves all specialties and returns them as a list of {@link SpecialtyResponse} DTOs.
   *
   * <p>Note: this method currently returns {@link HttpResponse#created(Object)} (HTTP 201) with the
   * list in the response body. If you prefer conventional semantics, consider returning {@link
   * HttpResponse#ok(Object)} (HTTP 200) instead.
   *
   * @return an {@link HttpResponse} containing a {@link List} of {@link SpecialtyResponse} DTOs.
   *     The response body is never {@code null}; an empty list is returned when no specialties
   *     exist.
   */
  @Get
  public HttpResponse<List<SpecialtyResponse>> findAll() {
    List<Specialty> specialties = specialtyService.findAll();

    return HttpResponse.created(
        specialties.stream()
            .map(specialty -> SpecialtyResponse.builder().name(specialty.getName()).build())
            .toList());
  }

  /**
   * Creates a new specialty from the provided {@link SpecialtyRequest}.
   *
   * <p>On success, returns {@link HttpResponse#created(Object)} with the created specialty DTO.
   * Validation (non-empty name, uniqueness, etc.) should be enforced by {@code SpecialtyService} or
   * with validation annotations on {@link SpecialtyRequest}.
   *
   * @param request the request body containing the specialty name; expected to be non-null
   * @return an {@link HttpResponse} containing the created {@link SpecialtyResponse} DTO
   */
  @Post
  public HttpResponse<SpecialtyResponse> create(@Body SpecialtyRequest request) {
    Specialty created = specialtyService.create(request.getName());
    return HttpResponse.created(SpecialtyResponse.builder().name(created.getName()).build());
  }

  /**
   * Renames an existing specialty.
   *
   * <p>The {@code name} path variable identifies the specialty to rename; the new name is taken
   * from the {@link SpecialtyRequest} body. If the specialty does not exist, the controller returns
   * {@link HttpResponse#badRequest(Object)} with an explanatory message.
   *
   * @param name the current name of the specialty to rename (path variable)
   * @param request the request body containing the new name for the specialty
   * @return {@link HttpResponse#ok(Object)} with the updated {@link SpecialtyResponse} on success,
   *     or {@link HttpResponse#badRequest(Object)} if the specialty was not found
   */
  @Put("/{name}")
  public HttpResponse<?> rename(@PathVariable String name, @Body SpecialtyRequest request) {

    try {
      Specialty updated = specialtyService.update(name, request.getName());
      return HttpResponse.ok(SpecialtyResponse.builder().name(updated.getName()).build());
    } catch (NoSuchElementException e) {
      return HttpResponse.badRequest("Specialty not found: " + name);
    }
  }
}
