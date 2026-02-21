package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Vet;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for {@link Vet} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface VetRepository extends ReactiveStreamsCrudRepository<Vet, Long> {

  /**
   * Retrieves a page of vets using manual SQL pagination.
   *
   * @param limit the maximum number of records to return
   * @param offset the number of records to skip before starting to collect the result set
   * @return a {@link Flux} emitting up to {@code limit} {@code Vet} entities, starting from the
   *     specified {@code offset}
   */
  @Query("SELECT * FROM petclinic.vets LIMIT :limit OFFSET :offset")
  Flux<Vet> findAllPaged(long limit, long offset);

  /**
   * Counts the total number of vets in the database.
   *
   * @return a {@link Mono} emitting the total count of {@code Vet} records
   */
  @Query("SELECT COUNT(*) FROM petclinic.vets")
  Mono<Long> countAll();

  /**
   * Finds all veterinarians whose last name exactly matches the given lastName.
   *
   * @param lastName the exact last name to match
   * @return a {@link Flux} emitting each {@code Vet} whose last name matches
   */
  Flux<Vet> findByLastName(String lastName);

  /**
   * Finds a single veterinarian by first and last name.
   *
   * @param firstName the first name to match
   * @param lastName the last name to match
   * @return a {@link Mono} emitting the matching {@code Vet}, or empty if none found
   */
  Mono<Vet> findByFirstNameAndLastName(String firstName, String lastName);

  /**
   * Deletes all veterinarians matching the given first and last name.
   *
   * @param firstName the first name of the vet(s) to delete
   * @param lastName the last name of the vet(s) to delete
   * @return a {@link Mono} emitting the number of records deleted
   */
  Mono<Long> deleteByFirstNameAndLastName(String firstName, String lastName);
}
