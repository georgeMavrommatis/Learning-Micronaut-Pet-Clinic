package com.gmavrommatis.service;

import com.gmavrommatis.config.domain.Specialty;
import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.config.repository.SpecialtyRepository;
import com.gmavrommatis.config.repository.VetRepository;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
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

  @PersistenceContext private EntityManager em;

  public VetService(VetRepository vetRepository, SpecialtyRepository specialtyRepository) {
    this.vetRepository = vetRepository;
    this.specialtyRepository = specialtyRepository;
  }

  /**
   * Retrieves all veterinarians Paged, without forcing specialty initialization.
   *
   * @return a {@link Page} of all {@link Vet} entities
   */
  @Transactional(readOnly = true)
  public Page<Vet> findAllPageable(Pageable from) {
    return vetRepository.findAll(from);
  }

  /**
   * Retrieves all veterinarians without forcing specialty initialization.
   *
   * @return a {@link List} of all {@link Vet} entities
   */
  @Transactional(readOnly = true)
  public List<Vet> findAll() {
    log.info("Request to get all Vets");
    return vetRepository.findAll();
  }

  /**
   * Creates a new veterinarian and associates it with existing specialties.
   *
   * <p>Validates each specialty name in the request; throws {@link NoSuchElementException} if any
   * specialty is not found.
   *
   * @param request the {@link CreateVetRequest} containing vet details and specialty names
   * @return the persisted {@link Vet} entity with ID and specialties initialized
   */
  @Transactional // default readOnly = false
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
  @Transactional
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
  @Transactional
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

  /**
   * Finds veterinarians by exact last name and a list of specialty names using the JPA Criteria
   * API.
   *
   * <p>This method:
   *
   * <ul>
   *   <li>Creates a {@link CriteriaQuery} to fetch {@code Vet} entities, performing a LEFT JOIN
   *       FETCH on the {@code specialties} association to initialize the collection in one query.
   *   <li>Uses a subquery to filter only those vets whose {@code lastName} equals the given value
   *       and who have at least one specialty name contained in the provided list.
   *   <li>Returns distinct results ordered by last name.
   * </ul>
   *
   * @param lastName the exact last name to match
   * @param specialtyNames a list of specialty names; only vets possessing at least one of these
   *     specialties are returned
   * @return a {@link List} of {@code Vet} entities with their specialties initialized
   */
  @Transactional
  public List<Vet> findByLastNameAndSpecialties(String lastName, List<String> specialtyNames) {

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Vet> cq = cb.createQuery(Vet.class);
    Root<Vet> vet = cq.from(Vet.class);

    // Join Fetch to load all specialties
    vet.fetch("specialties", JoinType.LEFT);

    // Subquery to filter by specialties
    Subquery<Long> subquery = cq.subquery(Long.class);
    Root<Vet> subVet = subquery.from(Vet.class);
    Join<Vet, Specialty> subSpec = subVet.join("specialties");

    subquery
        .select(subVet.get("id"))
        .where(
            cb.and(
                cb.equal(subVet.get("lastName"), lastName),
                subSpec.get("name").in(specialtyNames)));

    // Main query filters only by vet IDs from subquery
    cq.select(vet)
        .distinct(true)
        .where(vet.get("id").in(subquery))
        .orderBy(cb.asc(vet.get("lastName")));

    return em.createQuery(cq).getResultList();
  }

  /**
   * Finds veterinarians by exact last name and specialty names using a predefined JPQL query.
   *
   * <p>Delegates to the {@link VetRepository#findByLastNameAndSpecialties(String, List)} method
   * annotated with {@code @Query}, which performs a JOIN FETCH on specialties and filters by last
   * name and specialty membership, returning distinct results ordered by last name.
   *
   * @param lastNamePrefix the exact last name to match (note: passed as-is to repository query)
   * @param specialtyNames a list of specialty names; only vets possessing at least one of these
   *     specialties are returned
   * @return a {@link List} of {@code Vet} entities with their specialties initialized
   */
  @Transactional
  public List<Vet> findByLastNameAndSpecialtiesByQuery(
      String lastNamePrefix, List<String> specialtyNames) {

    return vetRepository.findByLastNameAndSpecialties(lastNamePrefix, specialtyNames);
  }
}
