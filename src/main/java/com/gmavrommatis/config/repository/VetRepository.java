package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Vet;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.annotation.EntityGraph;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Vet} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@Repository
public interface VetRepository extends JpaRepository<Vet, Long> {

  /**
   * Retrieves a paginated list of all {@link Vet} entities with their associated {@link Specialty
   * specialties} eagerly fetched.
   *
   * <p>This method overrides the default {@code findAll(Pageable)} implementation to eliminate the
   * typical N+1 select problem associated with lazily loaded collections. By applying a JPA {@link
   * EntityGraph} with {@code attributePaths = "specialties"}, Hibernate performs a single
   * fetch-join–like query to retrieve each {@code Vet} together with its specialties in one
   * round-trip to the database.
   *
   * <p>The result page still includes the default count query used for pagination, but the content
   * query will fetch the {@code specialties} association eagerly.
   *
   * @param pageable the pagination information, including page index and page size
   * @return a {@link Page} containing veterinarians and their eagerly loaded specialties
   */
  @Override
  @EntityGraph(attributePaths = "specialties")
  Page<Vet> findAll(Pageable pageable);

  /**
   * Explicit lazy variant that returns Vet entities without fetching specialties. The specialties
   * collection remains LAZY and won't be loaded until accessed.
   *
   * <p>NOTE: we use @Query to stop Spring Data from trying to parse the method name. Also provide
   * countQuery for pagination correctness.
   */
  @Query(value = "select v from Vet v", countQuery = "select count(v) from Vet v")
  Page<Vet> findAllLazy(Pageable pageable);

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
