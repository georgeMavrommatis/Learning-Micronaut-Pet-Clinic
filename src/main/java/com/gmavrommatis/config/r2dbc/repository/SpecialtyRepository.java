package com.gmavrommatis.config.r2dbc.repository;

import com.gmavrommatis.config.r2dbc.domain.Specialty;
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
 * @version 1.0
 */
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface SpecialtyRepository extends ReactiveStreamsCrudRepository<Specialty, Long> {

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
