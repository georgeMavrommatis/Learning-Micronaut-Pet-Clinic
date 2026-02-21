package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Specialty;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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
   * Finds all specialties whose names are in the given collection.
   *
   * @param names collection of specialty names to fetch
   * @return list of matching Specialty entities
   */
  Set<Specialty> findByNameIn(Collection<String> names);

  /**
   * Deletes all Specialties matching the given name.
   *
   * @return the number of rows deleted
   */
  Long deleteByName(String name);
}
