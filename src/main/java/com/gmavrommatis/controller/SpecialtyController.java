package com.gmavrommatis.controller;

import com.gmavrommatis.model.request.SpecialtyRequest;
import com.gmavrommatis.model.response.SpecialtyResponse;
import com.gmavrommatis.service.SpecialtyService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;
import reactor.core.publisher.Mono;

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
   * Retrieves all specialties in a reactive, non-blocking manner.
   *
   * <p>Streams all {@code Specialty} entities, maps each to a {@link SpecialtyResponse}, collects
   * them into a {@code List}, and wraps the result in a 200 OK {@code HttpResponse}.
   *
   * @return a {@link Mono} emitting an {@link HttpResponse} with a {@code List<SpecialtyResponse>}
   *     and HTTP status 200 OK
   */
  @Get
  public Mono<HttpResponse<List<SpecialtyResponse>>> findAll() throws Exception {
    return specialtyService
        .findAll() // Flux<Specialty>
        .map(s -> SpecialtyResponse.builder().name(s.getName()).build())
        .collectList() // Mono<List<SpecialtyResponse>>
        .map(HttpResponse::ok);
  }

  /**
   * Creates a new specialty.
   *
   * <p>Accepts a {@link SpecialtyRequest} in the request body, delegates to the service to create
   * the entity, maps the result to a {@link SpecialtyResponse}, and returns it with HTTP status 201
   * Created.
   *
   * @param request the {@link SpecialtyRequest} containing the name of the new specialty
   * @return a {@link Mono} emitting an {@link HttpResponse} with the created {@code
   *     SpecialtyResponse} and HTTP status 201 Created
   */
  @Post
  public Mono<HttpResponse<SpecialtyResponse>> create(@Body SpecialtyRequest request) {
    return specialtyService
        .create(request.getName())
        .map(s -> SpecialtyResponse.builder().name(s.getName()).build())
        .map(HttpResponse::created);
  }

  /**
   * Renames an existing specialty.
   *
   * <p>Takes the current specialty name as a path variable and a {@link SpecialtyRequest} with the
   * new name in the body. Delegates to the service to perform the update, then maps the result to a
   * 200 OK {@link SpecialtyResponse}. Any exception is propagated for handling via a custom
   * exception handler.
   *
   * @param name the current name of the specialty to rename
   * @param request the {@link SpecialtyRequest} containing the new name
   * @return a {@link Mono} emitting an {@link HttpResponse} with the updated {@code
   *     SpecialtyResponse} and HTTP status 200 OK
   */
  @Put("/{name}")
  public Mono<HttpResponse<SpecialtyResponse>> rename(
      @PathVariable String name, @Body SpecialtyRequest request) {

    return specialtyService
        .update(name, request.getName())
        // on success, wrap the DTO in a 200 OK
        .onErrorMap(
            Exception.class, // we can specify subclass exceptions instead
            e -> e)
        .map(
            updated ->
                HttpResponse.ok(SpecialtyResponse.builder().name(updated.getName()).build()));
  }

  /**
   * Deletes the specialty with the specified name in a reactive, non-blocking manner.
   *
   * @param name the unique name of the specialty to delete
   * @return a {@code Mono<HttpResponse<String>>} that emits:
   *     <ul>
   *       <li>204 No Content if the delete succeeded
   *       <li>400 Bad Request with “Specialty not found: {name}” if none matched
   *     </ul>
   */
  @Delete("/{name}")
  public Mono<MutableHttpResponse<String>> deleteByName(@PathVariable String name) {
    return specialtyService
        .deleteByName(name)
        .map(deletedCount -> HttpResponse.<String>noContent())
        .onErrorResume(
            NoSuchElementException.class,
            e -> Mono.just(HttpResponse.badRequest("Specialty not found: " + name)));
  }
}
