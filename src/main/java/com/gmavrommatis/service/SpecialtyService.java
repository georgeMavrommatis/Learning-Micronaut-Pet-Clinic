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
   * Creates a new specialty with the specified name.
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
   * Updates the name of an existing specialty.
   *
   * @param existingName the current name of the specialty to update
   * @param newName the new name to assign to the specialty
   * @return the updated {@link Specialty} entity
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

  /**
   * Deletes the specialty with the specified name.
   *
   * @param name the unique name of the specialty to delete
   */
  @Transactional
  public void deleteByName(String name) {
    specialtyRepository.deleteByName(name);
  }
}
