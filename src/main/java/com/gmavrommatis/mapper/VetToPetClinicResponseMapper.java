package com.gmavrommatis.mapper;

import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.model.response.PetClinicResponse;
import com.gmavrommatis.model.response.VetResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class VetToPetClinicResponseMapper {

  private final VetToVetResponseMapper vetToVetResponseMapper;

  @Inject
  public VetToPetClinicResponseMapper(VetToVetResponseMapper vetToVetResponseMapper) {
    this.vetToVetResponseMapper = vetToVetResponseMapper;
  }

  /**
   * Maps a list of {@link Vet} objects into a {@link PetClinicResponse}, using lazy/basic mapping.
   *
   * @param vets the list of {@link Vet} entities to map; may be {@code null}
   * @return a {@code PetClinicResponse} containing the lazy-mapped vets, or {@code null} if the
   *     input list was {@code null}
   */
  public PetClinicResponse toLazyResponse(List<Vet> vets) {
    if (vets == null) {
      return null;
    }
    List<VetResponse> lazyList =
        vets.stream().map(vetToVetResponseMapper::toVetResponseLazy).toList();
    return new PetClinicResponse(lazyList);
  }

  /**
   * Maps a list of {@link Vet} objects into a {@link PetClinicResponse}, using eager/detailed
   * mapping.
   *
   * @param vets the list of {@link Vet} entities to map; may be {@code null}
   * @return a {@code PetClinicResponse} containing the eager-mapped vets, or {@code null} if the
   *     input list was {@code null}
   */
  public PetClinicResponse toDetailedResponse(List<Vet> vets) {
    if (vets == null) {
      return null;
    }
    List<VetResponse> eagerList =
        vets.stream().map(vetToVetResponseMapper::toVetResponseEager).toList();
    return new PetClinicResponse(eagerList);
  }
}
