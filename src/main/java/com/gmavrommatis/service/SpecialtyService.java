package com.gmavrommatis.service;

import com.gmavrommatis.config.domain.Specialty;
import com.gmavrommatis.config.repository.SpecialtyRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;

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
   * Retrieves all specialties from the database.
   *
   * @return a {@link List} of all {@link Specialty} entities
   */
  @Transactional(readOnly = true)
  public List<Specialty> findAll() {
    return specialtyRepository.findAll();
  }

  /**
   * Finds a specialty by its ID.
   *
   * @param id the ID of the specialty
   * @return the {@link Specialty} entity, or {@code null} if not found
   */
  @Transactional(readOnly = true)
  public Specialty findById(Long id) {
    return specialtyRepository.findById(id).orElse(null);
  }

  /**
   * Finds a specialty by its name.
   *
   * @param name the name of the specialty
   * @return the {@link Specialty} entity, or {@code null} if not found
   */
  @Transactional(readOnly = true)
  public Specialty findByName(String name) {
    return specialtyRepository.findByName(name).orElse(null);
  }

  /**
   * Creates a new specialty with the given name.
   *
   * @param name the name of the new specialty
   * @return the created {@link Specialty} entity
   */
  @Transactional
  public Specialty create(String name) {
    Specialty s = new Specialty();
    s.setName(name);
    return specialtyRepository.save(s);
  }

  /**
   * Updates an existing specialty's name.
   *
   * @param existingName the current name of the specialty
   * @param newName the new name to assign
   * @return the updated {@link Specialty} entity
   * @throws NoSuchElementException if no specialty exists with {@code existingName}
   */
  @Transactional
  public Specialty update(String existingName, String newName) {
    Specialty s =
        specialtyRepository
            .findByName(existingName)
            .orElseThrow(() -> new NoSuchElementException("Specialty not found: " + existingName));

    s.setName(newName);
    return specialtyRepository.update(s);
  }
}
