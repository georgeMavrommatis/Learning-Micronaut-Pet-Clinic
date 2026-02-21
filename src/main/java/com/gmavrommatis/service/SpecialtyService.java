package com.gmavrommatis.service;

import com.gmavrommatis.config.domain.Specialty;
import com.gmavrommatis.config.repository.SpecialtyRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service layer for managing {@link Specialty} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@Singleton
@Slf4j
public class SpecialtyService {

  private final SpecialtyRepository specialtyRepository;

  public SpecialtyService(SpecialtyRepository specialtyRepository) {
    this.specialtyRepository = specialtyRepository;
  }

  /**
   * Retrieves all specialties.
   *
   * <p>Streams all {@link Specialty} entities from the repository.
   *
   * @return a {@link Flux} emitting each {@code Specialty} entity
   */
  @Transactional(readOnly = true)
  public Flux<Specialty> findAll() {
    log.debug("Request to get all Specialties");
    return specialtyRepository.findAll();
  }

  /**
   * Creates a new specialty with the specified name.
   *
   * <p>Persists a new {@link Specialty} entity. If a specialty with the same name already exists,
   * the repository may emit an error.
   *
   * @param name the name of the new specialty
   * @return a {@link Mono} emitting the saved {@code Specialty} entity
   * @throws IllegalArgumentException if the repository enforces uniqueness
   */
  @Transactional
  public Mono<Specialty> create(String name) {
    Specialty s = new Specialty();
    s.setName(name);
    return specialtyRepository.save(s);
  }

  /**
   * Updates the name of an existing specialty.
   *
   * <p>Finds the {@link Specialty} by its current name, errors if not found, then updates its name
   * and persists the change.
   *
   * @param existingName the current unique name of the specialty
   * @param newName the new name to assign
   * @return a {@link Mono} emitting the updated {@code Specialty}
   * @throws NoSuchElementException if no specialty with {@code existingName} exists
   * @throws IllegalArgumentException if the new name violates constraints
   */
  @Transactional
  public Mono<Specialty> update(String existingName, String newName) {
    return specialtyRepository
        .findByName(existingName)
        .switchIfEmpty(
            Mono.error(new NoSuchElementException("Specialty not found: " + existingName)))
        // flatMap unwraps the inner Mono<Specialty> into the outer stream, the map would
        // encapsulate further into another Mono<Mono<Specialty>>
        .flatMap(
            specialty -> {
              specialty.setName(newName);
              return specialtyRepository.update(specialty);
            });
  }

  /**
   * Deletes all specialties matching the given name.
   *
   * <p>Performs a reactive delete operation; the resulting {@code Mono} emits the count of deleted
   * records.
   *
   * @param name the unique name of the specialty to delete
   * @return a {@link Mono} emitting the number of deleted specialties
   */
  @Transactional
  public Mono<Long> deleteByName(String name) {
    return specialtyRepository.deleteByName(name);
  }
}
