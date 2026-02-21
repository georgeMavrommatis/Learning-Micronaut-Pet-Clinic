package com.gmavrommatis.config.r2dbc.repository;

import com.gmavrommatis.config.r2dbc.domain.Vet;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface VetRepository extends ReactiveStreamsCrudRepository<Vet, Long> {

  /**
   * Custom pagination against vets
   *
   * @param limit limit
   * @param offset offset
   * @return {@code Flux<Vet>}
   */
  @Query("SELECT * FROM petclinic.vets LIMIT :limit OFFSET :offset")
  Flux<Vet> findAllPaged(long limit, long offset);

  @Query("SELECT COUNT(*) FROM petclinic.vets")
  Mono<Long> countAll();

  Flux<Vet> findByLastName(String lastNamePrefix);

  /**
   * Find all vets matching the given first & last name.
   *
   * @return an Optional containing the Vet if found
   */
  Mono<Vet> findByFirstNameAndLastName(String firstName, String lastName);

  /**
   * Deletes all vets matching the given first & last name.
   *
   * @return the number of rows deleted
   */
  Mono<Long> deleteByFirstNameAndLastName(String firstName, String lastName);
}
