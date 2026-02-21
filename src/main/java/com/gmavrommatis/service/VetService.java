package com.gmavrommatis.service;

import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.config.repository.SpecialtyRepository;
import com.gmavrommatis.config.repository.VetRepository;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service layer for managing {@link Vet} entities and their specialties.
 *
 * @author GewrgiosMmavrommatis
 */
@Singleton
@Slf4j
public class VetService {

  private final VetRepository vetRepository;
  private final SpecialtyRepository specialtyRepository;

  public VetService(VetRepository vetRepository, SpecialtyRepository specialtyRepository) {
    this.vetRepository = vetRepository;
    this.specialtyRepository = specialtyRepository;
  }

  @Transactional(readOnly = true)
  public Mono<Page<Vet>> findAllWithSpecialties(Pageable from) {
    log.info("Request to get all Vets with Specialties");
    return vetRepository
        .findAll(from)
        .map(
            vetsPage -> {
              vetsPage.getContent().stream().peek(vet -> vet.getSpecialties().size()).toList();
              return vetsPage;
            });
  }

  /**
   * Retrieves all veterinarians without initializing their specialties collection.
   *
   * @return a {@link Flux} emitting each {@link Vet} entity
   */
  @Transactional(readOnly = true)
  public Flux<Vet> findAll() throws Exception {
    log.info("Request to get all Vets");
    return vetRepository.findAll();
  }

  /**
   * Creates a new {@link Vet} and associates it with existing specialties.
   *
   * <p>Loads each specialty by name in parallel; if any specialty is not found, the flow errors.
   *
   * @param createVetRequest the request containing vet names and specialty names
   * @return a {@link Mono} emitting the persisted {@code Vet} with ID and specialties initialized
   */
  @Transactional // default readOnly = false
  public Mono<Vet> createVet(CreateVetRequest createVetRequest) {
    // 1) Lookup all specialties in parallel
    return Flux.fromIterable(createVetRequest.getSpecialties())
        .concatMap( // concatMap enforces one DB call at a time,  The reactive session remains
            // consistent, Transaction boundaries are respected
            name ->
                specialtyRepository
                    .findByName(name)
                    .switchIfEmpty(
                        Mono.error(
                            // todo throw custom error catch with handler
                            new Exception("Specialty not found: " + name))))
        .collectList() // Mono<List<Specialty>>
        // 2) Build and save the Vet once all specialties are loaded
        .flatMap(
            specList -> {
              Vet vet = new Vet();
              vet.setFirstName(createVetRequest.getFirstName());
              vet.setLastName(createVetRequest.getLastName());
              vet.setSpecialties(new HashSet<>(specList));
              return vetRepository.save(vet); // returns Mono<Vet>
            });
  }

  /**
   * Deletes veterinarians matching the given first and last name.
   *
   * <p>Emits completion if at least one record was deleted; otherwise errors.
   *
   * @param firstName the vet’s first name
   * @param lastName the vet’s last name
   * @return a {@link Mono} that completes on success or errors if no vet was deleted
   */
  @Transactional
  public Mono<Void> deleteByName(String firstName, String lastName) {
    return vetRepository
        // assume this now returns Mono<Long> count of deleted rows
        .deleteByFirstNameAndLastName(firstName, lastName)
        .flatMap(
            deletedCount -> {
              if (deletedCount > 0) {
                // complete normally (HTTP 204 No Content, for example)
                return Mono.empty();
              } else {
                // signal an error downstream
                return Mono.error(
                    new Exception("No vet found with name: " + firstName + " " + lastName));
              }
            });
  }

  /**
   * Updates a veterinarian’s personal details and/or specialties by their name.
   *
   * <p>Applies only non-null fields from the request. If specialties are provided, loads them by
   * name and replaces the vet’s specialties set.
   *
   * @param firstName the current first name of the vet
   * @param lastName the current last name of the vet
   * @param req the request containing updated fields
   * @return a {@link Flux} emitting the updated {@link Vet}
   */
  @Transactional
  public Flux<Vet> updateVetByName(String firstName, String lastName, UpdateVetRequest req) {

    return vetRepository
        // 1) Find the existing vet
        .findByFirstNameAndLastName(firstName, lastName)
        .switchIfEmpty(Mono.error(new Exception("Vet not found: " + firstName + " " + lastName)))
        // 2) Apply simple field updates
        .flatMap(
            vet -> {
              if (req.getFirstName() != null) {
                vet.setFirstName(req.getFirstName());
              }
              if (req.getLastName() != null) {
                vet.setLastName(req.getLastName());
              }

              // 3) If specialties need updating, look them up; otherwise just update the vet
              if (req.getSpecialtyNames() != null) {
                return Flux.fromIterable(req.getSpecialtyNames())
                    .flatMap(
                        specName ->
                            specialtyRepository
                                .findByName(specName)
                                .switchIfEmpty(
                                    Mono.error(new Exception("Specialty not found: " + specName))))
                    .collectList()
                    .flatMap(
                        specList -> {
                          vet.setSpecialties(new HashSet<>(specList));
                          return vetRepository.update(vet);
                        });
              } else {
                return vetRepository.update(vet);
              }
            });
  }

  /**
   * Finds veterinarians whose last name matches exactly and who have any of the given specialties,
   * using a predefined repository query.
   *
   * @param lastNamePrefix the exact last name to match
   * @param specialtyNames the list of specialty names to filter by
   * @return a {@link Flux} emitting each matching {@code Vet}
   */
  @Transactional
  public Flux<Vet> findByLastNameAndSpecialtiesByQuery(
      String lastNamePrefix, List<String> specialtyNames) {

    return vetRepository.findByLastNameAndSpecialties(lastNamePrefix, specialtyNames);
  }
}
