package com.gmavrommatis.controller;

import com.gmavrommatis.config.domain.l1.Specialty;
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
   * Retrieves all specialties.
   *
   * @return a list of {@link SpecialtyResponse}
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
   * Creates a new specialty with the given name.
   *
   * @param request the {@link SpecialtyRequest} containing the name of the new specialty
   * @return the created {@link SpecialtyResponse}
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
   * @return the updated {@link SpecialtyResponse}, or 400 BAD_REQUEST if no specialty with the
   *     given name exists
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

  /**
   * Deletes the specialty with the specified name.
   *
   * @param name the unique name of the specialty to delete
   * @return an {@code HttpResponse} with:
   *     <ul>
   *       <li>200 ok, if the deletion was successful
   *       <li>400 Bad Request, if no specialty with the given name exists
   *     </ul>
   */
  @Delete("/{name}")
  public HttpResponse<String> deleteByName(@PathVariable String name) {
    try {
      specialtyService.deleteByName(name);
      return HttpResponse.ok();
    } catch (NoSuchElementException e) {
      return HttpResponse.badRequest("Specialty not found: " + name);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
