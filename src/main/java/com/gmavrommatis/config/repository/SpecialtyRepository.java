package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Specialty;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for {@link Specialty} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface SpecialtyRepository extends ReactiveStreamsCrudRepository<Specialty, Long> {

  /**
   * Finds a {@link Specialty} by its name.
   *
   * <p>This method returns a {@link Mono} that will:
   *
   * <ul>
   *   <li>Emit the {@link Specialty} if a matching specialty is found.
   *   <li>Complete empty if no specialty with the given name exists.
   *   <li>Signal an error if there is a database access issue.
   * </ul>
   *
   * <p>The search is typically case-sensitive depending on the underlying database collation.
   *
   * @param name the name of the specialty to search for; must not be {@code null}
   * @return a {@link Mono} emitting the matching {@link Specialty}, or empty if none found
   */
  Mono<Specialty> findByName(String name);

  /**
   * Retrieves all {@link Specialty} entities associated with the specified veterinarian.
   *
   * <p>Executes a raw SQL query joining the <code>petclinic.specialties</code> table with the
   * <code>petclinic.vet_specialties</code> join table to fetch only those specialties linked to the
   * given vet ID.
   *
   * @param vetId the unique identifier of the veterinarian whose specialties are to be fetched
   * @return a {@link Flux} emitting each {@code Specialty} associated with the specified vet;
   *     completes empty if the vet has no specialties or does not exist
   */
  @Query(
      """
            SELECT s.*
            FROM petclinic.specialties s
            JOIN petclinic.vet_specialties vs ON s.id = vs.specialty_id
            WHERE vs.vet_id = :vetId
            """)
  Flux<Specialty> findByVetId(Long vetId);
}
