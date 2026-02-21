package com.gmavrommatis.service;

import com.gmavrommatis.mapper.VetToVetResponseMapper;
import com.gmavrommatis.model.response.PetClinicResponse;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service layer for Pet Clinic operations.
 *
 * @author GewrgiosMmavrommatis
 */
@Singleton
@Slf4j
public class PetClinicService {

  private final VetService vetService;
  private final VetToVetResponseMapper vetToVetResponseMapper;

  public PetClinicService(VetService vetService, VetToVetResponseMapper vetToVetResponseMapper) {
    this.vetService = vetService;
    this.vetToVetResponseMapper = vetToVetResponseMapper;
  }

  /**
   * Retrieves detailed Pet Clinic information in a paginated, reactive, non-blocking fashion.
   *
   * <p>Delegates to the {@link VetService#findAllWithSpecialties(Pageable)} method to fetch a
   * {@code Mono<Page<Vet>>} of vets (with specialties eagerly loaded), then maps the page content
   * into {@link com.gmavrommatis.model.response.VetResponse} DTOs and assembles a {@link
   * com.gmavrommatis.model.response.PetClinicResponse} containing both the DTO list and pagination
   * metadata.
   *
   * @param from the pagination parameters (zero-based page index and page size)
   * @return a {@code Mono<PetClinicResponse>} that emits a response object containing:
   *     <ul>
   *       <li>a list of vet DTOs for the requested page
   *       <li>the current page index
   *       <li>the requested page size
   *       <li>the total number of pages
   *       <li>the total number of elements across all pages
   *     </ul>
   */
  @Transactional(readOnly = true)
  public Mono<PetClinicResponse> getPetClinicDetails(Pageable from) {
    return vetService
        .findAllWithSpecialties(from) // Mono<Page<Vet>>
        .map(
            vetsPage ->
                PetClinicResponse.builder()
                    .vets(vetToVetResponseMapper.toVetResponseEagerList(vetsPage.getContent()))
                    .page(vetsPage.getPageable().getNumber())
                    .size(vetsPage.getPageable().getSize())
                    .totalPages(vetsPage.getTotalPages())
                    .totalElements(vetsPage.getTotalSize())
                    .build());
  }
}
