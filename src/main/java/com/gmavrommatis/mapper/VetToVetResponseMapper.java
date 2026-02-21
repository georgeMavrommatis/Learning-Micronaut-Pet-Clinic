package com.gmavrommatis.mapper;

import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.model.response.VetResponse;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "jsr330")
public interface VetToVetResponseMapper {

  /*** DETAILED MAPPING ***/

  @Named("eagerVet")
  VetResponse toVetResponseEager(Vet vet);

  /**
   * Maps a list of {@link Vet} entities to a list of {@link VetResponse} DTOs using detailed
   * mapping.
   *
   * @param vets the list of {@code Vet} entities; may be {@code null}
   * @return a list of fully populated {@code VetResponse} objects, or {@code null} if {@code vets}
   *     is {@code null}
   */
  @IterableMapping(qualifiedByName = "eagerVet")
  List<VetResponse> toVetResponseEagerList(List<Vet> vets);
}
