package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Specialty;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import reactor.core.publisher.Mono;

/**
 * Repository interface for {@link Specialty} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@SuppressWarnings("unused")
@Repository
public interface SpecialtyRepository extends ReactorCrudRepository<Specialty, Long> {

  /**
   * Finds a {@link Specialty} by its unique name.
   *
   * <p>Executes a reactive lookup; the resulting {@code Mono} will complete with the found entity
   * or complete empty if no match is found.
   *
   * @param name the unique name of the specialty to look up
   * @return a {@code Mono<Specialty>} that emits the matching specialty, or completes empty if none
   *     exists
   */
  Mono<Specialty> findByName(String name);

  /**
   * Deletes all specialties matching the given name.
   *
   * <p>Performs a reactive delete operation; the resulting {@code Mono} emits the number of rows
   * that were deleted.
   *
   * @param name the name of the specialty (or specialties) to delete
   * @return a {@code Mono<Long>} emitting the count of deleted records
   */
  Mono<Long> deleteByName(String name);
}
