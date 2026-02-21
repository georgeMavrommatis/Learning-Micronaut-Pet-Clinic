package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Specialty;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for {@link Specialty} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface SpecialtyRepository extends CrudRepository<Specialty, Long> {

  /**
   * Repository for {@link Specialty} entities.
   *
   * <p>This interface uses Micronaut Data's {@code @JdbcRepository} with a Postgres dialect.
   * Implementations are generated at compile time by Micronaut Data. Methods should be treated as
   * thin data-access operations — map results to DTOs or services for business logic.
   *
   * <p>Repository implementations typically throw runtime data-access exceptions (for example
   * {@code DataAccessException}) for low-level failures; callers may want to translate or handle
   * those exceptions at service boundaries.
   */

  /**
   * Finds a specialty by its exact name.
   *
   * <p>Search is exact and case-sensitive depending on the database collation; if case-insensitive
   * matching is required use an appropriate SQL function or a custom query.
   *
   * @param name the exact name of the specialty (must not be {@code null})
   * @return an {@link Optional} containing the {@link Specialty} if found, otherwise an empty
   *     {@code Optional}
   */
  Optional<Specialty> findByName(String name);

  Set<Specialty> findByNameIn(Set<String> names);

  /**
   * Finds all specialties associated with a given veterinarian.
   *
   * <p>This method uses a custom SQL query against the {@code petclinic.specialties} table and the
   * join table {@code petclinic.vet_specialties}. It returns all specialties that are linked to the
   * provided vet id.
   *
   * <p>Notes:
   *
   * <ul>
   *   <li>The query is executed directly against the database (no entity graph / fetch plan).
   *   <li>If the vet has no specialties the method returns an empty {@link List} (never {@code
   *       null}).
   *   <li>Use pagination if the result set can be large.
   * </ul>
   *
   * @param vetId the id of the veterinarian whose specialties should be returned (must not be
   *     {@code null})
   * @return a {@link List} of {@link Specialty} entities assigned to the veterinarian; empty list
   *     when none found
   */
  @Query(
      """
                    SELECT s.*
                    FROM petclinic.specialties s
                    JOIN petclinic.vet_specialties vs ON s.id = vs.specialty_id
                    WHERE vs.vet_id = :vetId
                    """)
  List<Specialty> findByVetId(Long vetId);
}
