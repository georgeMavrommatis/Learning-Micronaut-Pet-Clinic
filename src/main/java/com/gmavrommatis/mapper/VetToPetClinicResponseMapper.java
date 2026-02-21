package com.gmavrommatis.mapper;

import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.model.response.PetClinicResponse;
import io.micronaut.data.model.Page;
import jakarta.inject.Inject;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * Mapper that converts pages of {@link Vet} entities into {@link PetClinicResponse} DTOs, including
 * pagination metadata.
 *
 * <p>Uses a {@link VetToVetResponseMapper} to map individual {@link Vet} instances into their
 * corresponding response DTOs.
 *
 * <p>Implemented at compile time by MapStruct.
 *
 * @author Your Name
 */
@Mapper(componentModel = "jsr330", uses = VetToVetResponseMapper.class)
public abstract class VetToPetClinicResponseMapper {

  @Inject protected VetToVetResponseMapper vetToVetResponseMapper;

  /**
   * Converts a list of {@link Vet} entities and a {@link Page} wrapper into a {@link
   * PetClinicResponse} using basic mapping for each vet.
   *
   * <p>If the provided list of vets is {@code null}, this method returns {@code null}. Otherwise,
   * it maps the page content to DTOs and populates pagination fields.
   *
   * @param vets the raw list of {@code Vet} entities (ignored if {@code null})
   * @param vetsPage the {@link Page} containing the vets and pagination info
   * @return a {@code PetClinicResponse} containing:
   *     <ul>
   *       <li>the list of mapped vet DTOs for the current page
   *       <li>current page index
   *       <li>page size
   *       <li>total number of pages
   *       <li>total number of elements across all pages
   *     </ul>
   *     or {@code null} if {@code vets} is {@code null}
   */
  public PetClinicResponse toLazyResponse(List<Vet> vets, Page<Vet> vetsPage) {
    if (vets == null) return null;
    return PetClinicResponse.builder()
        .vets(vetToVetResponseMapper.toVetResponseList(vetsPage.getContent()))
        .page(vetsPage.getPageable().getNumber())
        .size(vetsPage.getPageable().getSize())
        .totalPages(vetsPage.getTotalPages())
        .totalElements(vetsPage.getTotalSize())
        .build();
  }

  /**
   * Converts a list of {@link Vet} entities and a {@link Page} wrapper into a {@link
   * PetClinicResponse} using detailed mapping for each vet.
   *
   * <p>Functionally identical to {@link #toLazyResponse(List, Page)}, but provided to convey
   * semantic intent when detailed mapping is desired. Returns {@code null} if the provided list of
   * vets is {@code null}.
   *
   * @param vets the raw list of {@code Vet} entities (ignored if {@code null})
   * @param vetsPage the {@link Page} containing the vets and pagination info
   * @return a {@code PetClinicResponse} containing detailed vet DTOs and pagination metadata, or
   *     {@code null} if {@code vets} is {@code null}
   */
  public PetClinicResponse toDetailedResponsePageable(List<Vet> vets, Page<Vet> vetsPage) {
    if (vets == null) return null;
    return PetClinicResponse.builder()
        .vets(vetToVetResponseMapper.toVetResponseList(vetsPage.getContent()))
        .page(vetsPage.getPageable().getNumber())
        .size(vetsPage.getPageable().getSize())
        .totalPages(vetsPage.getTotalPages())
        .totalElements(vetsPage.getTotalSize())
        .build();
  }
}
