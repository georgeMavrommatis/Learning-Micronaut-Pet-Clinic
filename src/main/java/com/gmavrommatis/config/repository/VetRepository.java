package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Vet;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for {@link Vet} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@Repository
public interface VetRepository extends JpaRepository<Vet, Long> {

  /**
   * Deletes all veterinarians matching the given first and last name.
   *
   * <p>Useful for removing a vet when their full name is known.
   *
   * @param firstName the first name of the vet(s) to delete
   * @param lastName the last name of the vet(s) to delete
   * @return the number of rows (veterinarians) deleted
   */
  long deleteByFirstNameAndLastName(String firstName, String lastName);

  /**
   * Finds a veterinarian by their first and last name.
   *
   * @param firstName the first name of the vet
   * @param lastName the last name of the vet
   * @return the matching {@code Vet}
   */
  Optional<Vet> findByFirstNameAndLastName(String firstName, String lastName);
}
