package com.gmavrommatis.service;

import com.gmavrommatis.config.domain.*;
import com.gmavrommatis.config.repository.SpecialtyRepository;
import com.gmavrommatis.config.repository.VetRelatedRepository;
import com.gmavrommatis.config.repository.VetRepository;
import com.gmavrommatis.config.repository.VetSpecialtyRepository;
import com.gmavrommatis.mapper.SpecialtyMapper;
import com.gmavrommatis.mapper.SpecialtyToSpecialtyResponseMapper;
import com.gmavrommatis.mapper.VetRelatedToVetResponseMapper;
import com.gmavrommatis.mapper.VetToVetResponseMapper;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.model.response.SpecialtyResponse;
import com.gmavrommatis.model.response.VetResponse;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class VetService {

  private final VetRepository vetRepository;
  private final VetRelatedRepository vetRelatedRepository;
  private final SpecialtyRepository specialtyRepository;
  private final SpecialtyService specialtyService;
  private final VetSpecialtyRepository vetSpecialtyRepository;
  private final VetToVetResponseMapper vetToVetResponseMapper;
  private final VetRelatedToVetResponseMapper vetRelatedToVetResponseMapper;
  private final SpecialtyToSpecialtyResponseMapper specialtyToSpecialtyResponseMapper;
  private final SpecialtyMapper specialtyMapper;

  public VetService(
      VetRepository vetRepository,
      VetRelatedRepository vetRelatedRepository,
      SpecialtyRepository specialtyRepository,
      SpecialtyService specialtyService,
      VetSpecialtyRepository vetSpecialtyRepository,
      VetToVetResponseMapper vetToVetResponseMapper,
      VetRelatedToVetResponseMapper vetRelatedToVetResponseMapper,
      SpecialtyToSpecialtyResponseMapper specialtyToSpecialtyResponseMapper,
      SpecialtyMapper specialtyMapper) {
    this.vetRepository = vetRepository;
    this.vetRelatedRepository = vetRelatedRepository;
    this.specialtyRepository = specialtyRepository;
    this.specialtyService = specialtyService;
    this.vetSpecialtyRepository = vetSpecialtyRepository;
    this.vetToVetResponseMapper = vetToVetResponseMapper;
    this.vetRelatedToVetResponseMapper = vetRelatedToVetResponseMapper;
    this.specialtyToSpecialtyResponseMapper = specialtyToSpecialtyResponseMapper;
    this.specialtyMapper = specialtyMapper;
  }

  /**
   * Fetches all vets with pagination.
   *
   * @param pageable pagination information
   * @return a {@link Page} of {@link Vet} entities
   */
  @Transactional(readOnly = true)
  public PetClinicResponse findAllRelatedPageable(Pageable pageable) {
    Page<VetRelated> vetPage = vetRelatedRepository.findAll(pageable);

    return PetClinicResponse.builder()
        .vets(vetRelatedToVetResponseMapper.toVetResponseList(vetPage.getContent()))
        .page(pageable.getNumber())
        .size(pageable.getSize())
        .totalElements((long) vetPage.getNumberOfElements())
        .totalPages(vetPage.getTotalPages())
        .build();
  }

  /**
   * Retrieves all veterinarians without specialty.
   *
   * @return a {@link List} of all {@link Vet} entities
   */
  @Transactional(readOnly = true)
  public List<Vet> findAll() {
    log.info("Request to get all Vets");
    return vetRepository.findAll();
  }

  /**
   * Retrieves paginated vets with their specialties.
   *
   * @param pageable pagination information
   * @return {@link PetClinicResponse} containing vets with specialties
   */
  @Transactional(readOnly = true)
  public PetClinicResponse getVetsWithSpecialties(Pageable pageable) {
    Page<Vet> vetPage = vetRepository.findAllPaged(pageable);
    List<VetResponse> vetResponses = new ArrayList<>();

    for (Vet vet : vetPage.getContent()) {
      List<VetSpecialty> vetSpecialties = vetSpecialtyRepository.findByVetId(vet.getId());
      List<Specialty> specialties = new ArrayList<>();
      for (VetSpecialty vs : vetSpecialties) {
        Specialty spec = specialtyService.findById(vs.getId().getSpecialtyId());
        if (spec != null) specialties.add(spec);
      }
      VetResponse vr = vetToVetResponseMapper.toVetResponse(vet);
      vr.setSpecialties(specialtyToSpecialtyResponseMapper.toSpecialtyResponseList(specialties));
      vetResponses.add(vr);
    }

    return PetClinicResponse.builder()
        .vets(vetResponses)
        .page(pageable.getNumber())
        .size(pageable.getSize())
        .totalElements((long) vetPage.getNumberOfElements())
        .totalPages(vetPage.getTotalPages())
        .build();
  }

  @Transactional // default readOnly = false
  public VetResponse createVet(CreateVetRequest request) {
    // 1. Create a new Vet
    VetRelated vetRelated =
        VetRelated.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .build();

    // 2. Load each Specialty by ID and add it to the Vet
    Set<Specialty> specs = specialtyRepository.findByNameIn(request.getSpecialties());
    if (!request.getSpecialties().containsAll(specs)) {
      throw new NoSuchElementException("Specialties not found");
    }

    vetRelated.setSpecialties(specialtyMapper.toRelatedSet(specs));

    // 3. Save the Vet. Hibernate will insert into vets,
    //    then into vet_specialties join-table for each Specialty
    VetRelated vetResponse = vetRelatedRepository.save(vetRelated);
    // here we mimic an intentional exception to show how transactionality rollback commit to
    // database
    if (vetResponse.getFirstName().startsWith("fail")) {
      throw new RuntimeException("intentional exception");
    }
    return vetRelatedToVetResponseMapper.toVetResponse(vetResponse);
  }

  /**
   * Creates a vet with associated specialties.
   *
   * @param createVetRequest request containing vet info and specialties
   * @return created {@link VetResponse}
   */
  @Transactional
  public VetResponse createVetWithSpecialties(CreateVetRequest createVetRequest) {
    List<Specialty> specialties = new ArrayList<>();
    for (String name : createVetRequest.getSpecialties()) {
      Specialty spec = specialtyRepository.findByName(name).orElse(null);
      if (spec == null) throw new NoSuchElementException("Specialty not found: " + name);
      specialties.add(spec);
    }

    Vet vet = new Vet();
    vet.setFirstName(createVetRequest.getFirstName());
    vet.setLastName(createVetRequest.getLastName());
    Vet savedVet = vetRepository.save(vet);

    for (Specialty spec : specialties) {
      vetSpecialtyRepository.save(
          new VetSpecialty(new VetSpecialtyId(savedVet.getId(), spec.getId())));
    }

    VetResponse response = vetToVetResponseMapper.toVetResponse(savedVet);
    response.setSpecialties(
        specialtyToSpecialtyResponseMapper.toSpecialtyResponseList(specialties));
    return response;
  }

  /**
   * Finds vets by last name and filters by specialties.
   *
   * @param lastName the last name
   * @param specialtyNames list of specialty names
   * @return list of {@link VetResponse} matching criteria
   */
  @Transactional(readOnly = true)
  public List<VetResponse> findByLastName(String lastName, List<String> specialtyNames) {
    List<Vet> vets = vetRepository.findByLastName(lastName);
    if (vets == null || vets.isEmpty()) return List.of();

    List<VetResponse> responses = new ArrayList<>();
    for (Vet vet : vets) {
      List<Specialty> specialties = specialtyRepository.findByVetId(vet.getId());
      List<SpecialtyResponse> specialtyResponses =
          specialtyToSpecialtyResponseMapper.toSpecialtyResponseList(specialties);

      boolean hasMatching =
          specialtyResponses.stream()
              .map(SpecialtyResponse::getName)
              .anyMatch(specialtyNames::contains);

      if (hasMatching) {
        VetResponse vr = vetToVetResponseMapper.toVetResponse(vet);
        vr.setSpecialties(specialtyResponses);
        responses.add(vr);
      }
    }
    return responses;
  }

  /**
   * Deletes a vet by name (cascade).
   *
   * @param firstName first name
   * @param lastName last name
   * @throws NoSuchElementException if vet not found
   */
  @Transactional
  public void deleteByNameWithCascade(String firstName, String lastName) {
    Long deletedCount = vetRepository.deleteByFirstNameAndLastName(firstName, lastName);
    if (deletedCount == null || deletedCount == 0)
      throw new NoSuchElementException("No vet found with name: " + firstName + " " + lastName);
  }

  /**
   * Deletes a vet by name (no cascade; deletes associations manually first).
   *
   * @param firstName first name
   * @param lastName last name
   */
  @Transactional
  public void deleteByNameWithoutCascade(String firstName, String lastName) {
    List<Vet> vets = vetRepository.findByFirstNameAndLastName(firstName, lastName);
    if (vets == null || vets.isEmpty())
      throw new NoSuchElementException("No vet found with name: " + firstName + " " + lastName);
    Vet vet = vets.get(0);

    vetSpecialtyRepository.deleteByVetId(vet.getId());
    Long deletedCount = vetRepository.deleteByFirstNameAndLastName(firstName, lastName);
    if (deletedCount == null || deletedCount == 0)
      throw new RuntimeException("Failed to delete vet with name: " + firstName + " " + lastName);
  }

  /**
   * Updates a vet’s info and specialties.
   *
   * @param firstName current first name
   * @param lastName current last name
   * @param req update request
   * @return updated {@link VetResponse}
   */
  @Transactional
  public VetResponse updateVetByName(String firstName, String lastName, UpdateVetRequest req) {
    List<Vet> vets = vetRepository.findByFirstNameAndLastName(firstName, lastName);
    if (vets == null || vets.isEmpty())
      throw new NoSuchElementException("Vet not found: " + firstName + " " + lastName);
    Vet vet = vets.get(0);

    if (req.getFirstName() != null) vet.setFirstName(req.getFirstName());
    if (req.getLastName() != null) vet.setLastName(req.getLastName());

    List<Specialty> specialties = new ArrayList<>();
    if (req.getSpecialtyNames() != null) {
      for (String name : req.getSpecialtyNames()) {
        Specialty spec = specialtyRepository.findByName(name).orElse(null);
        if (spec == null) throw new NoSuchElementException("Specialty not found: " + name);
        specialties.add(spec);
      }
    }

    vetRepository.update(vet);
    vetSpecialtyRepository.deleteByVetId(vet.getId());
    for (Specialty spec : specialties) {
      vetSpecialtyRepository.save(new VetSpecialty(new VetSpecialtyId(vet.getId(), spec.getId())));
    }

    VetResponse response = vetToVetResponseMapper.toVetResponse(vet);
    response.setSpecialties(
        specialtyToSpecialtyResponseMapper.toSpecialtyResponseList(specialties));
    return response;
  }
}
