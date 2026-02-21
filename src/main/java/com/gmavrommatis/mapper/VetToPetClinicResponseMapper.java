package com.gmavrommatis.mapper;

import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.model.response.PetClinicResponse;
import io.micronaut.data.model.Page;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Mapper that converts lists or pages of {@link Vet} entities into {@link PetClinicResponse} DTOs,
 * including pagination metadata.
 *
 * <p>This abstract class is implemented at compile time by MapStruct.
 *
 * <ul>
 *   <li><strong>toLazyResponse</strong> – builds a response using basic vet mapping.
 *   <li><strong>toDetailedResponsePageable</strong> – builds a response using detailed vet mapping.
 * </ul>
 *
 * @author GewrgiosMmavrommatis
 */
@Singleton
public class VetToPetClinicResponseMapper {

  private final VetToVetResponseMapper vetToVetResponseMapper;

  @Inject
  public VetToPetClinicResponseMapper(VetToVetResponseMapper vetToVetResponseMapper) {
    this.vetToVetResponseMapper = vetToVetResponseMapper;
  }

  /**
   * Maps a page of {@link Vet} entities into a {@link PetClinicResponse} DTO using eager
   * vet-to-response mapping for the content, and includes pagination details.
   *
   * <p>If the provided list of vets is {@code null}, this method returns {@code null}.
   *
   * @param vetsPage the {@link Page} containing the vets and pagination info
   * @return a {@code PetClinicResponse} containing:
   *     <ul>
   *       <li>the list of vet DTOs for the current page
   *       <li>current page index
   *       <li>page size
   *       <li>total number of pages
   *       <li>total number of elements
   *     </ul>
   *     or {@code null} if {@code vets} is {@code null}
   */
  public PetClinicResponse toLazyResponse(Page<Vet> vetsPage) {
    if (vetsPage == null) return null;
    return PetClinicResponse.builder()
        .vets(vetToVetResponseMapper.toVetResponseLazyList(vetsPage.getContent()))
        .page(vetsPage.getPageable().getNumber()) // zero-based page index
        .size(vetsPage.getSize())
        .totalPages(vetsPage.getTotalPages())
        .totalElements(vetsPage.getNumberOfElements())
        .build();
  }

  /**
   * Maps a page of {@link Vet} entities into a {@link PetClinicResponse} DTO using detailed
   * vet-to-response mapping for the content, and includes pagination details. Contains also
   * specialties.
   *
   * @param vetsPage the {@link Page} containing the vets and pagination info
   * @return a {@code PetClinicResponse} populated with detailed vet DTOs and pagination metadata,
   *     or {@code null} if {@code vets} is {@code null}
   */
  public PetClinicResponse toDetailedResponsePageable(Page<Vet> vetsPage) {
    if (vetsPage == null) return null;
    return PetClinicResponse.builder()
        .vets(vetToVetResponseMapper.toVetResponseEagerList(vetsPage.getContent()))
        .page(vetsPage.getPageable().getNumber())
        .size(vetsPage.getPageable().getSize())
        .totalPages(vetsPage.getTotalPages())
        .totalElements(vetsPage.getTotalSize())
        .build();
  }
}
