package com.gmavrommatis.service;

import com.gmavrommatis.config.jpa.domain.Vet;
import com.gmavrommatis.mapper.VetToPetClinicResponseMapper;
import com.gmavrommatis.model.response.PetClinicResponse;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for Pet Clinic operations.
 *
 * @author GewrgiosMmavrommatis
 */
@Singleton
@Slf4j
public class PetClinicService {

  private final VetService vetService;
  private final VetToPetClinicResponseMapper mapper;

  public PetClinicService(VetService vetService, VetToPetClinicResponseMapper mapper) {
    this.vetService = vetService;
    this.mapper = mapper;
  }

  /**
   * Retrieves detailed Pet Clinic information.
   *
   * <p>Fetches all veterinarians along with their specialties in a single transactional, read-only
   * operation, then maps the result into a {@link PetClinicResponse}.
   *
   * @return a {@code PetClinicResponse} containing detailed veterinarian data
   */
  @Transactional(readOnly = true, transactionManager = "jpaTx")
  public PetClinicResponse getPetClinicDetails() {
    List<Vet> vets = vetService.findAll();
    return mapper.toDetailedResponse(vets);
  }
}
