package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.l1.Specialty;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import jakarta.inject.Named;
import java.util.Optional;

/**
 * Micronaut Data JPA repository for {@link Specialty} entities.
 *
 * <p>Provides CRUD operations and custom queries for specialties, including lookup by unique name
 * and deletion by name. This repository is configured with the "postgresql2" qualifier and bean
 * name to distinguish it from other SpecialtyRepository implementations.
 *
 * <h3>Bean Configuration</h3>
 *
 * <ul>
 *   <li>{@code @Repository("postgresql2")} – registers this interface as a Bean with name
 *       "postgresql2".
 *   <li>{@code @Named("postgresql2")} – allows injection by qualifier "postgresql2".
 * </ul>
 *
 * @author GewrgiosMmavrommatis
 */
@SuppressWarnings("unused")
@Repository("postgresql1")
@Named("postgresql1")
public interface SpecialtyRepositoryL1 extends JpaRepository<Specialty, Long> {

  /**
   * Finds a {@link Specialty} by its unique name.
   *
   * @param name the unique name of the specialty
   * @return the found {@code Specialty}
   */
  Optional<Specialty> findByName(String name);

  /**
   * Deletes all specialties that match the given name.
   *
   * <p>Note: If multiple specialties share the same name (which should normally be unique), all
   * will be removed.
   *
   * @param name the name of the specialty to delete
   */
  void deleteByName(String name);
}
