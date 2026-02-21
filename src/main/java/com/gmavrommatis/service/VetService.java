package com.gmavrommatis.service;

import com.gmavrommatis.config.r2dbc.domain.Specialty;
import com.gmavrommatis.config.r2dbc.domain.Vet;
import com.gmavrommatis.config.r2dbc.domain.VetSpecialty;
import com.gmavrommatis.config.r2dbc.domain.VetSpecialtyId;
import com.gmavrommatis.config.r2dbc.repository.SpecialtyRepository;
import com.gmavrommatis.config.r2dbc.repository.VetRepository;
import com.gmavrommatis.config.r2dbc.repository.VetSpecialtyRepository;
import com.gmavrommatis.mapper.SpecialtyToSpecialtyResponseMapper;
import com.gmavrommatis.mapper.VetToVetResponseMapper;
import com.gmavrommatis.model.request.CreateVetRequest;
import com.gmavrommatis.model.request.UpdateVetRequest;
import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.model.response.SpecialtyResponse;
import com.gmavrommatis.model.response.VetResponse;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import io.r2dbc.spi.Connection;
import jakarta.inject.Singleton;
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

  private final ReactiveTransactionOperations<Connection> r2dbcTx;
  private final VetRepository vetRepository;
  private final SpecialtyRepository specialtyRepository;
  private final SpecialtyService specialtyService;
  private final VetSpecialtyRepository vetSpecialtyRepository;
  private final VetToVetResponseMapper vetToVetResponseMapper;
  private final SpecialtyToSpecialtyResponseMapper specialtyToSpecialtyResponseMapper;

  public VetService(
      ReactiveTransactionOperations<Connection> r2dbcTx,
      VetRepository vetRepository,
      SpecialtyRepository specialtyRepository,
      SpecialtyService specialtyService,
      VetSpecialtyRepository vetSpecialtyRepository,
      VetToVetResponseMapper vetToVetResponseMapper,
      SpecialtyToSpecialtyResponseMapper specialtyToSpecialtyResponseMapper) {
    this.r2dbcTx = r2dbcTx;
    this.vetRepository = vetRepository;
    this.specialtyRepository = specialtyRepository;
    this.specialtyService = specialtyService;
    this.vetSpecialtyRepository = vetSpecialtyRepository;
    this.vetToVetResponseMapper = vetToVetResponseMapper;
    this.specialtyToSpecialtyResponseMapper = specialtyToSpecialtyResponseMapper;
  }

  /**
   * Retrieves a page of {@link Vet} entities with specialties eagerly loaded.
   *
   * @param pageable the pagination parameters (zero-based page index and page size)
   * @return a {@link Flux} emitting the vets on the specified page
   */
  public Flux<Vet> findAllPageable(Pageable pageable) {
    return Flux.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            r2dbcStatus -> // R2DBC READ_ONLY transaction
            vetRepository.findAllPaged(pageable.getSize(), pageable.getOffset())));
  }

  /**
   * Finds a single {@link Vet} by first and last name within a read-only transaction.
   *
   * @param firstName the vet’s first name
   * @param lastName the vet’s last name
   * @return a {@link Mono} emitting the matching vet, or error if not found
   */
  public Mono<Vet> findByFirstAndLastName(String firstName, String lastName) {
    return Mono.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            r2dbcStatus -> // R2DBC Read only transaction
            vetRepository
                    .findByFirstNameAndLastName(firstName, lastName)
                    .switchIfEmpty(
                        Mono.error(
                            new Exception(
                                "No vet found with name: " + firstName + " " + lastName)))));
  }

  /**
   * Retrieves a paginated {@link PetClinicResponse} containing vet DTOs and metadata.
   *
   * @param pageable the pagination parameters (zero-based page index and size)
   * @return a {@link Mono} emitting the assembled {@code PetClinicResponse}
   */
  public Mono<PetClinicResponse> getVetsWithSpecialties(Pageable pageable) {

    return Mono.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            status ->
                findAllPageable(pageable)
                    .concatMap(
                        vet ->
                            vetSpecialtyRepository
                                .findByVetId(vet.getId())
                                .concatMap(
                                    vs -> specialtyService.findById(vs.getId().getSpecialtyId()))
                                .collectList()
                                .map(
                                    specialties -> {
                                      VetResponse response =
                                          vetToVetResponseMapper.toVetResponse(vet);
                                      response.setSpecialties(
                                          specialtyToSpecialtyResponseMapper
                                              .toSpecialtyResponseList(specialties));
                                      return response;
                                    }))
                    .collectList()
                    // 🔒 SEQUENTIAL count
                    .flatMap(
                        vets ->
                            vetRepository
                                .countAll()
                                .map(
                                    count ->
                                        PetClinicResponse.builder()
                                            .vets(vets)
                                            .page(pageable.getNumber())
                                            .size(pageable.getSize())
                                            .totalElements(count)
                                            .totalPages(
                                                (int)
                                                    Math.ceil((double) count / pageable.getSize()))
                                            .build()))));
  }

  /*public Mono<PetClinicResponse> getVetsWithSpecialties(Pageable pageable) {
    return Mono.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            r2dbcStatus -> // R2DBC Read only transaction
            findAllPageable(pageable)
                    .flatMap(
                        vet ->
                            vetSpecialtyRepository
                                .findByVetId(vet.getId())
                                .map(VetSpecialty::getId)
                                .flatMap(vs -> specialtyService.findById(vs.getSpecialtyId()))
                                .collectList()
                                .map(
                                    specialties -> {
                                      VetResponse response =
                                          vetToVetResponseMapper.toVetResponse(vet);
                                      response.setSpecialties(
                                          specialtyToSpecialtyResponseMapper
                                              .toSpecialtyResponseList(specialties));
                                      return response;
                                    }))
                    .collectList()
                    .zipWith(
                        vetRepository
                            .countAll()) // combine results into tuple->T1,T2 : T1:vets, T2:total
                    // count of vets
                    .map(
                        tuple ->
                            PetClinicResponse.builder()
                                .vets(tuple.getT1())
                                .page(pageable.getNumber())
                                .size(pageable.getSize())
                                .totalElements(tuple.getT2())
                                .totalPages(
                                    (int) Math.ceil((double) tuple.getT2() / pageable.getSize()))
                                .build())));
  }*/

  /**
   * Creates a new {@link Vet} and associates it with the named specialties.
   *
   * @param createVetRequest the request containing vet names and specialty names
   * @return a {@link Mono} emitting the created {@link VetResponse}
   */
  public Mono<VetResponse> createVetWithSpecialties(CreateVetRequest createVetRequest) {
    return Mono.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.DEFAULT,
            txStatus ->

                // 1) Lookup all specialties in parallel
                Flux.fromIterable(createVetRequest.getSpecialties())
                    .flatMap(
                        name ->
                            specialtyService
                                .findByName(name)
                                .switchIfEmpty(
                                    Mono.error(
                                        new IllegalArgumentException(
                                            "Specialty not found: " + name))))
                    .collectList() // Mono<List<Specialty>>
                    .flatMap(
                        specialties -> {
                          // 2) Save Vet
                          Vet vet = new Vet();
                          vet.setFirstName(createVetRequest.getFirstName());
                          vet.setLastName(createVetRequest.getLastName());

                          return Mono.from(vetRepository.save(vet))
                              // 2a) Fault-injection check
                              .flatMap(
                                  savedVet -> {
                                    if ("test_fail".equals(savedVet.getFirstName())) {
                                      // any error here rolls back the whole TX
                                      return Mono.error(
                                          new RuntimeException(
                                              "Intentional failure for test_fail"));
                                    }
                                    return Mono.just(savedVet);
                                  })
                              // 3) Save VetSpecialty associations
                              .flatMap(
                                  savedVet ->
                                      Flux.fromIterable(specialties)
                                          .flatMap(
                                              spec -> {
                                                VetSpecialtyId id =
                                                    new VetSpecialtyId(
                                                        savedVet.getId(), spec.getId());
                                                VetSpecialty vs = new VetSpecialty(id);
                                                return vetSpecialtyRepository.save(vs);
                                              })
                                          .then(Mono.just(savedVet)))
                              // 4) Build response
                              .map(
                                  v -> {
                                    VetResponse response = vetToVetResponseMapper.toVetResponse(v);
                                    response.setSpecialties(
                                        specialtyToSpecialtyResponseMapper.toSpecialtyResponseList(
                                            specialties));
                                    return response;
                                  });
                        })));
  }

  /**
   * Finds vets by last name lastName and filters by specialty names.
   *
   * @param lastName the lastName to match against vets’ last names
   * @param specialtyNames the specialty names to filter by
   * @return a {@link Flux} emitting matching {@code VetResponse} DTOs
   */
  public Flux<VetResponse> findByLastName(String lastName, List<String> specialtyNames) {
    return Flux.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.READ_ONLY,
            r2dbcStatus -> // R2DBC Read only transaction
            vetRepository
                    .findByLastName(lastName)
                    .flatMap(
                        vet ->
                            specialtyRepository
                                .findByVetId(vet.getId())
                                .collectList()
                                .map(
                                    specialties -> {
                                      VetResponse response =
                                          vetToVetResponseMapper.toVetResponse(vet);
                                      response.setSpecialties(
                                          specialtyToSpecialtyResponseMapper
                                              .toSpecialtyResponseList(specialties));
                                      return response;
                                    }))
                    // now filter out any responses whose specialties list
                    // has no overlap with the requested names
                    .filter(
                        vr ->
                            vr.getSpecialties().stream()
                                .map(SpecialtyResponse::getName)
                                .anyMatch(specialtyNames::contains))));
  }

  /**
   * Deletes vets by name, cascading to join table deletions.
   *
   * @param firstName the vet’s first name
   * @param lastName the vet’s last name
   * @return a {@link Mono<Void>} completing on success or error if none deleted
   */
  public Mono<Void> deleteByNameWithCascade(String firstName, String lastName) {
    return Mono.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.DEFAULT,
            r2dbcStatus -> // R2DBC DEFAULT transaction
            vetRepository
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
                                new Exception(
                                    "No vet found with name: " + firstName + " " + lastName));
                          }
                        })));
  }

  /**
   * Deletes a vet and its specialty associations in separate steps.
   *
   * @param firstName the vet’s first name
   * @param lastName the vet’s last name
   * @return a {@link Mono<Void>} completing on success or error if operation fails
   */
  public Mono<Void> deleteByNameWithoutCascade(String firstName, String lastName) {
    return Mono.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.DEFAULT,
            r2dbcStatus -> // R2DBC DEFAULT transaction
            vetRepository
                    .findByFirstNameAndLastName(firstName, lastName)
                    .switchIfEmpty(
                        Mono.error(
                            new Exception("No vet found with name: " + firstName + " " + lastName)))
                    .flatMap(
                        vet ->
                            vetSpecialtyRepository
                                .deleteByVetId(vet.getId())
                                .then(
                                    vetRepository.deleteByFirstNameAndLastName(firstName, lastName))
                                .flatMap(
                                    deletedCount -> {
                                      if (deletedCount > 0) {
                                        return Mono.empty();
                                      } else {
                                        return Mono.error(
                                            new Exception(
                                                "Failed to delete Vet with name: "
                                                    + firstName
                                                    + " "
                                                    + lastName));
                                      }
                                    }))));
  }

  /**
   * Updates a vet’s details and specialties in a single transaction.
   *
   * @param firstName the current first name of the vet
   * @param lastName the current last name of the vet
   * @param req the update request carrying new fields
   * @return a {@link Mono<VetResponse>} emitting the updated DTO or error if not found
   */
  public Mono<VetResponse> updateVetByName(
      String firstName, String lastName, UpdateVetRequest req) {
    return Mono.from(
        r2dbcTx.withTransaction(
            TransactionDefinition.DEFAULT,
            r2dbcStatus -> // R2DBC DEFAULT transaction
            vetRepository
                    .findByFirstNameAndLastName(firstName, lastName)
                    .switchIfEmpty(
                        Mono.error(new Exception("Vet not found: " + firstName + " " + lastName)))
                    .flatMap(
                        vet -> {
                          if (req.getFirstName() != null) vet.setFirstName(req.getFirstName());
                          if (req.getLastName() != null) vet.setLastName(req.getLastName());

                          Mono<List<Specialty>> specialtyMono;

                          if (req.getSpecialtyNames() != null
                              && !req.getSpecialtyNames().isEmpty()) {
                            specialtyMono =
                                Flux.fromIterable(req.getSpecialtyNames())
                                    .flatMap(
                                        name ->
                                            specialtyRepository
                                                .findByName(name)
                                                .switchIfEmpty(
                                                    Mono.error(
                                                        new Exception(
                                                            "Specialty not found: " + name))))
                                    .collectList();
                          } else {
                            specialtyMono = Mono.just(List.of());
                          }

                          return specialtyMono.flatMap(
                              specList ->
                                  Mono.from(vetRepository.update(vet))
                                      .then(vetSpecialtyRepository.deleteByVetId(vet.getId()))
                                      .thenMany(
                                          Flux.fromIterable(specList)
                                              .flatMap(
                                                  spec ->
                                                      vetSpecialtyRepository.save(
                                                          new VetSpecialty(
                                                              new VetSpecialtyId(
                                                                  vet.getId(), spec.getId())))))
                                      .collectList()
                                      .map(
                                          savedAssociations -> {
                                            VetResponse response =
                                                vetToVetResponseMapper.toVetResponse(vet);
                                            response.setSpecialties(
                                                specialtyToSpecialtyResponseMapper
                                                    .toSpecialtyResponseList(specList));
                                            return response;
                                          }));
                        })));
  }
}
