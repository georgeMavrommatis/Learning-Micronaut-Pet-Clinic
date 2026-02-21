package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Vet;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for {@link Vet} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@Repository
public interface VetRepository extends ReactorCrudRepository<Vet, Long> {

  /**
   * Retrieves a paginated list of all {@link Vet} entities.
   *
   * <p>Micronaut Data automatically implements this method, executing an SQL query equivalent to:
   *
   * <pre>
   *   SELECT * FROM vets
   *   LIMIT :#{#pageable.pageSize}
   *   OFFSET :#{#pageable.pageNumber} * :#{#pageable.pageSize}
   * </pre>
   *
   * @param pageable pagination parameters including zero-based page index and page size
   * @return a {@link Mono} emitting a {@link Page} of {@code Vet} entities for the requested page
   */
  Mono<Page<Vet>> findAll(Pageable pageable);

  /**
   * Deletes all veterinarians matching the given first and last name.
   *
   * <p>Performs a reactive delete operation; the resulting {@code Mono} emits the count of rows
   * deleted.
   *
   * @param firstName the first name of the vet(s) to delete
   * @param lastName the last name of the vet(s) to delete
   * @return a {@link Mono} emitting the number of vets deleted
   */
  Mono<Long> deleteByFirstNameAndLastName(String firstName, String lastName);

  /**
   * Finds all veterinarians matching the given first and last name.
   *
   * <p>Returns a reactive stream of matching vets; completes empty if none are found.
   *
   * @param firstName the first name to match
   * @param lastName the last name to match
   * @return a {@link Flux} emitting matching {@code Vet} entities
   */
  Flux<Vet> findByFirstNameAndLastName(String firstName, String lastName);

  /**
   * Finds all veterinarians whose last name equals the given value and who have at least one of the
   * specified specialties.
   *
   * <p>Executes the provided JPQL query with a {@code LEFT JOIN FETCH} on {@code v.specialties} to
   * initialize the collection and filters by specialty membership. Results are distinct and ordered
   * by last name.
   *
   * @param lastName the exact last name to match
   * @param specialtyNames a list of specialty names; only vets possessing at least one of these
   *     specialties are returned
   * @return a {@link Flux} emitting distinct {@code Vet} entities with their specialties
   *     initialized
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
  Flux<Vet> findByLastNameAndSpecialties(String lastName, List<String> specialtyNames);
}
