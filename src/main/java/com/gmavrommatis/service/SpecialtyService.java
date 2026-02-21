package com.gmavrommatis.service;

import com.gmavrommatis.config.r2dbc.domain.Specialty;
import com.gmavrommatis.config.r2dbc.repository.SpecialtyRepository;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import io.r2dbc.spi.Connection;
import jakarta.inject.Singleton;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service layer for managing {@link Specialty} entities.
 *
 * @author GewrgiosMmavrommatis
 */
@Singleton
@Slf4j
public class SpecialtyService {

  private final SpecialtyRepository specialtyRepository;
  private final ReactiveTransactionOperations<Connection> r2dbcTx;

  public SpecialtyService(
      SpecialtyRepository specialtyRepository, ReactiveTransactionOperations<Connection> r2dbcTx) {
    this.specialtyRepository = specialtyRepository;
    this.r2dbcTx = r2dbcTx;
  }

  /**
   * Retrieves all specialties.
   *
   * @return a {@link Flux} emitting each {@code Specialty} entity
   */
  public Flux<Specialty> findAll() {
    log.info("SpecialtyService findAll");
    return Flux.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            r2dbcStatus -> // R2DBC Read only transaction
            Flux.from(specialtyRepository.findAll())));
  }

  /**
   * Retrieves a specialty by its unique identifier within a read-only R2DBC transaction.
   *
   * @param id the unique identifier of the specialty to retrieve
   * @return a {@link Flux<Specialty>} emitting the matching specialty if found, or completing empty
   *     if none exists
   */
  public Flux<Specialty> findById(Long id) {
    return Flux.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            r2dbcStatus -> // R2DBC Read only transaction
            Flux.from(specialtyRepository.findById(id))));
  }

  /**
   * Retrieves a specialty by its unique name within a read-only R2DBC transaction.
   *
   * @param name the unique name of the specialty to retrieve
   * @return a {@link Mono<Specialty>} emitting the matching specialty if found, or completing empty
   *     if none exists
   */
  public Mono<Specialty> findByName(String name) {
    return Mono.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            r2dbcStatus -> // R2DBC Read only transaction
            Mono.from(specialtyRepository.findByName(name))));
  }

  /**
   * Creates and persists a new {@link Specialty} with the specified name.
   *
   * @param name the unique name of the specialty to create
   * @return the persisted {@link Specialty} entity with its generated ID
   */
  public Specialty create(String name) {
    Specialty s = new Specialty();
    s.setName(name);
    return Mono.from(
            r2dbcTx.withTransaction(
                TransactionDefinition.DEFAULT,
                r2dbcStatus -> // R2DBC DEFAULT transaction
                Mono.from(specialtyRepository.save(s))))
        .block();
  }

  /**
   * Updates the name of an existing {@link Specialty}.
   *
   * @param existingName the current unique name of the specialty to update
   * @param newName the new name to assign to the specialty
   * @return the updated {@link Specialty} entity with its new name
   * @throws NoSuchElementException if no specialty with {@code existingName} exists
   */
  public Specialty update(String existingName, String newName) {
    /*check if specialty exists*/
    Specialty s =
        Optional.ofNullable(
                Mono.from(
                        r2dbcTx.withTransaction(
                            TransactionDefinition.DEFAULT,
                            r2dbcStatus -> // R2DBC DEFAULT transaction
                            Mono.from(specialtyRepository.findByName(existingName))))
                    .block())
            .orElseThrow(() -> new NoSuchElementException("Specialty not found: " + existingName));

    s.setName(newName);
    return Mono.from(specialtyRepository.update(s)).block();
  }
}
