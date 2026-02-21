package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.l1.Vet;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import jakarta.inject.Named;
import java.util.List;
import java.util.Optional;

/**
 * Micronaut Data JPA repository for {@link Vet} entities.
 *
 * <p>Provides CRUD operations and custom queries for veterinarians. This repository is qualified
 * and named with "postgresql2" to distinguish it from other {@code VetRepository} beans in the
 * application context.
 *
 * <h3>Bean Configuration</h3>
 *
 * <ul>
 *   <li>{@code @Repository("postgresql2")} – registers this interface as a Bean with the name
 *       "postgresql2".
 *   <li>{@code @Named("postgresql2")} – allows injection by the qualifier "postgresql2".
 * </ul>
 *
 * @author GewrgiosMmavrommatis
 * @version 1.0
 */
@SuppressWarnings("unused")
@Named("postgresql1")
@Repository("postgresql1")
public interface VetRepositoryL1 extends JpaRepository<Vet, Long> {

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

  /**
   * Finds all veterinarians whose last name matches the given value and who have at least one of
   * the specified specialties.
   *
   * <p>Executes a JPQL query that:
   *
   * <ul>
   *   <li>Fetches each vet’s specialties in a single query to avoid N+1 select issues.
   *   <li>Filters vets by matching last name and at least one specialty name from the provided
   *       list.
   *   <li>Returns distinct results ordered by last name.
   * </ul>
   *
   * @param lastName the last name to match (exact match)
   * @param specialtyNames a list of specialty names; only vets with at least one matching specialty
   *     are returned
   * @return a list of {@link Vet} entities, each with their specialties initialized
   */
  @Query(
      """
    SELECT DISTINCT v
    FROM Vet v
    LEFT JOIN FETCH v.specialties s
    WHERE v IN (
        SELECT v2
        FROM Vet v2
        JOIN v2.specialties s2
        WHERE v2.lastName = :lastName
          AND s2.name IN :specialtyNames
    )
    ORDER BY v.lastName
""")
  List<Vet> findByLastNameAndSpecialties(String lastName, List<String> specialtyNames);
}
