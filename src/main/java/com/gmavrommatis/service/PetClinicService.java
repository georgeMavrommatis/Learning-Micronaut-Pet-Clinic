package com.gmavrommatis.service;

import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.mapper.VetToPetClinicResponseMapper;
import com.gmavrommatis.model.response.PetClinicResponse;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
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
  private final VetToPetClinicResponseMapper vetToPetClinicResponseMapper;

  public PetClinicService(
      VetService vetService, VetToPetClinicResponseMapper vetToPetClinicResponseMapper) {
    this.vetService = vetService;
    this.vetToPetClinicResponseMapper = vetToPetClinicResponseMapper;
  }

  /**
   * Retrieves paginated Pet Clinic details.
   *
   * <p>Fetches a page of {@link Vet} entities (with specialties eagerly loaded) via the {@code
   * VetService}, converts them to {@link com.gmavrommatis.model.response.VetResponse} DTOs, and
   * wraps them in a {@link com.gmavrommatis.model.response.PetClinicResponse} along with pagination
   * metadata.
   *
   * @param from the pagination parameters (zero-based page index and page size)
   * @return a {@code PetClinicResponse} containing:
   *     <ul>
   *       <li>a list of vet DTOs for the requested page
   *       <li>the current page index
   *       <li>the requested page size
   *       <li>the total number of pages
   *       <li>the total number of elements across all pages
   *     </ul>
   */
  @Transactional(readOnly = true)
  public PetClinicResponse getPetClinicDetailsEagerly(Pageable from) {
    Page<Vet> vetsPage = vetService.findAllPageableEagerly(from);
    return vetToPetClinicResponseMapper.toDetailedResponsePageable(vetsPage);
  }

  @Transactional(readOnly = true)
  public PetClinicResponse getPetClinicDetailsLazily(Pageable from) {
    Page<Vet> vetsPage = vetService.findAllPageableLazily(from);
    return vetToPetClinicResponseMapper.toDetailedResponsePageable(vetsPage);
  }
}
