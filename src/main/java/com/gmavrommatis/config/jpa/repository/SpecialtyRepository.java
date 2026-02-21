package com.gmavrommatis.config.jpa.repository;

import com.gmavrommatis.config.jpa.domain.Specialty;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for {@link Specialty} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@SuppressWarnings("unused")
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

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
