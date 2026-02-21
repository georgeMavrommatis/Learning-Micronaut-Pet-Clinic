package com.gmavrommatis.service;

import com.gmavrommatis.config.jpa.domain.Specialty;
import com.gmavrommatis.config.jpa.domain.Vet;
import com.gmavrommatis.config.jpa.repository.SpecialtyRepository;
import com.gmavrommatis.config.jpa.repository.VetRepository;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

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

  /**
   * Retrieves all veterinarians without forcing specialty initialization.
   *
   * @return a {@link List} of all {@link Vet} entities
   */
  @Transactional(readOnly = true, transactionManager = "jpaTx")
  public List<Vet> findAll() {
    return vetRepository.findAll();
  }

  /**
   * Retrieves a veterinarian by their first and last name.
   *
   * <p>Executes within a read-only JPA transaction using the "jpaTx" transaction manager. Looks up
   * the vet via the repository and throws an exception if not found.
   *
   * @param firstName the first name of the veterinarian to find
   * @param lastName the last name of the veterinarian to find
   * @return the matching {@link Vet} entity
   * @throws NoSuchElementException if no veterinarian is found with the given name
   */
  @Transactional(readOnly = true, transactionManager = "jpaTx")
  public Vet findByFirstAndLastName(String firstName, String lastName) {
    return vetRepository
        .findByFirstNameAndLastName(firstName, lastName)
        .orElseThrow(
            () -> new NoSuchElementException("Vet not found: " + firstName + " " + lastName));
  }

  /**
   * Creates and persists a new {@link Vet} along with its associated specialties.
   *
   * <p>Executes within a JPA transaction (using the "jpaTx" transaction manager). The method:
   *
   * <ol>
   *   <li>Builds a new, transient {@code Vet} instance from the request.
   *   <li>Looks up each specialty by name; throws {@link NoSuchElementException} if any specialty
   *       is not found.
   *   <li>Associates the loaded specialties with the vet and saves the vet. Hibernate will handle
   *       inserting into the join table.
   *   <li>After saving, if the vet’s first name equals "test_fail", a {@link RuntimeException} is
   *       thrown to simulate a rollback scenario.
   * </ol>
   *
   * @param request the {@link CreateVetRequest} containing the vet’s first name, last name, and
   *     list of specialty names to associate
   * @return the persisted {@link Vet} entity with its generated ID and specialties
   * @throws NoSuchElementException if any requested specialty name does not exist
   * @throws RuntimeException if the saved vet’s first name equals "test_fail"
   */
  @Transactional(transactionManager = "jpaTx") // default readOnly = false
  public Vet createVet(CreateVetRequest request) {
    // 1. Create a new Vet
    Vet vet =
        Vet.builder().firstName(request.getFirstName()).lastName(request.getLastName()).build();

    // 2. Load each Specialty by ID and add it to the Vet
    Set<Specialty> specs = new HashSet<>();
    for (String specialtyName : request.getSpecialties()) {
      Specialty s =
          specialtyRepository
              .findByName(specialtyName)
              .orElseThrow(
                  () -> new NoSuchElementException("Specialty not found: " + specialtyName));
      specs.add(s);
    }
    vet.setSpecialties(specs);

    // 3. Save the Vet. Hibernate will insert into vets,
    //    then into vet_specialties join-table for each Specialty
    Vet vetResponse = vetRepository.save(vet);
    // here we mimic an intentional exception to show how transactionality rollback commit to
    // database
    if (vetResponse.getFirstName().startsWith("fail")) {
      throw new RuntimeException("intentional exception");
    }
    return vetResponse;
  }

  /**
   * Deletes veterinarians matching the given first and last name.
   *
   * <p>Returns the number of deleted records. If no matching veterinarian is found, a {@link
   * NoSuchElementException} is thrown.
   *
   * @param firstName the vet’s first name
   * @param lastName the vet’s last name
   * @return the number of veterinarians deleted
   */
  @Transactional(transactionManager = "jpaTx")
  public Long deleteByName(String firstName, String lastName) {
    Optional<Vet> optionalVet = vetRepository.findByFirstNameAndLastName(firstName, lastName);
    if (optionalVet.isEmpty()) {
      throw new IllegalArgumentException("Vet " + firstName + " " + " not exists");
    }
    return vetRepository.deleteByFirstNameAndLastName(firstName, lastName);
  }

  /**
   * Updates an existing veterinarian’s personal details and specialties.
   *
   * <p>Only non-{@code null} fields in the {@link UpdateVetRequest} will be applied. Validates
   * specialty names before association.
   *
   * @param firstName the current first name of the vet to update
   * @param lastName the current last name of the vet to update
   * @param req the {@link UpdateVetRequest} containing updated fields
   * @return the updated {@link Vet} entity
   */
  @Transactional(transactionManager = "jpaTx")
  public Vet updateVetByName(String firstName, String lastName, UpdateVetRequest req) {

    Vet vet =
        vetRepository
            .findByFirstNameAndLastName(firstName, lastName)
            .orElseThrow(
                () -> new NoSuchElementException("Vet not found: " + firstName + " " + lastName));

    // same logic as before, e.g. req.getFirstName() != null → vet.setFirstName(...)
    if (req.getFirstName() != null) {
      vet.setFirstName(req.getFirstName());
    }
    if (req.getLastName() != null) {
      vet.setLastName(req.getLastName());
    }
    if (req.getSpecialtyNames() != null) {
      Set<Specialty> specs =
          req.getSpecialtyNames().stream()
              .map(
                  specialtyName ->
                      specialtyRepository
                          .findByName(specialtyName)
                          .orElseThrow(
                              () ->
                                  new NoSuchElementException(
                                      "Specialty not found: " + specialtyName)))
              .collect(Collectors.toSet());
      vet.setSpecialties(specs);
    }

    return vetRepository.update(vet);
  }
}
