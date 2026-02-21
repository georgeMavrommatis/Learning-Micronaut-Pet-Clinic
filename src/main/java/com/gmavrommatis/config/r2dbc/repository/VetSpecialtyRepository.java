package com.gmavrommatis.config.r2dbc.repository;

import com.gmavrommatis.config.r2dbc.domain.VetSpecialty;
import com.gmavrommatis.config.r2dbc.domain.VetSpecialtyId;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for the {@link VetSpecialty} join entity, backed by R2DBC.
 *
 * <p>Provides methods to fetch and delete associations between veterinarians and specialties in a
 * non-blocking manner using PostgreSQL as the target database.
 *
 * @author Your Name
 * @version 1.0
 */
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface VetSpecialtyRepository
    extends ReactiveStreamsCrudRepository<VetSpecialty, VetSpecialtyId> {

  /**
   * Retrieves all {@link VetSpecialty} associations for the given veterinarian ID.
   *
   * <p>Emits a stream of join records linking the specified vet to its specialties.
   *
   * @param vetId the unique identifier of the veterinarian
   * @return a {@link Flux} emitting each {@code VetSpecialty} for the specified vet
   */
  Flux<VetSpecialty> findByVetId(Long vetId);

  /**
   * Deletes all {@code VetSpecialty} records for the specified veterinarian ID.
   *
   * <p>Executes a SQL DELETE statement against the <code>petclinic.vet_specialties</code> table.
   * Emits the number of rows that were removed.
   *
   * @param vetId the unique identifier of the veterinarian whose associations should be removed
   * @return a {@link Mono} emitting the count of deleted rows
   */
  @Query("DELETE FROM petclinic.vet_specialties WHERE vet_id = :vetId")
  Mono<Long> deleteByVetId(Long vetId);
}
