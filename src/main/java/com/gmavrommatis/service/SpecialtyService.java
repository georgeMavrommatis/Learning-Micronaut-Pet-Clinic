package com.gmavrommatis.service;

import com.gmavrommatis.config.domain.l1.Specialty;
import com.gmavrommatis.config.repository.SpecialtyRepositoryL2;
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

  private final SpecialtyRepositoryL2 specialtyRepository;

  public SpecialtyService(SpecialtyRepositoryL2 specialtyRepository) {
    this.specialtyRepository = specialtyRepository;
  }

  /**
   * Retrieves all specialties from the database.
   *
   * @return a {@link List} of all {@link Specialty} entities
   */
  @Transactional(value = "postgresql2", readOnly = true)
  public List<Specialty> findAll() {
    return specialtyRepository.findAll();
  }

  /**
   * Creates a new specialty with the specified name.
   *
   * @param name the name of the new specialty
   * @return the created {@link Specialty} entity
   */
  @Transactional(value = "postgresql2")
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
  @Transactional(value = "postgresql2")
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
  @Transactional(value = "postgresql2")
  public void deleteByName(String name) {
    specialtyRepository.deleteByName(name);
  }
}
