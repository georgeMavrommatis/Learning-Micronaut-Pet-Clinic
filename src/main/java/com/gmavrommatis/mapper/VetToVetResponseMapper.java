package com.gmavrommatis.mapper;

import com.gmavrommatis.config.r2dbc.domain.Vet;
import com.gmavrommatis.model.response.VetResponse;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting {@link Vet} entities into {@link VetResponse} DTOs.
 *
 * <p>Provides methods for mapping a single {@code Vet} as well as lists of vets. Implemented at
 * compile time by MapStruct.
 *
 * @author Your Name
 */
@Mapper(componentModel = "jsr330")
public interface VetToVetResponseMapper {

  /**
   * Converts a single {@link Vet} entity into a {@link VetResponse} DTO.
   *
   * @param vet the {@code Vet} entity to map; may be {@code null}
   * @return the corresponding {@code VetResponse} DTO, or {@code null} if the input was {@code
   *     null}
   */
  VetResponse toVetResponse(Vet vet);

  /**
   * Converts a list of {@link Vet} entities into a list of {@link VetResponse} DTOs.
   *
   * @param vets the list of {@code Vet} entities to map; may be {@code null}
   * @return a list of {@code VetResponse} DTOs, or {@code null} if the input list was {@code null}
   */
  List<VetResponse> toVetResponseList(List<Vet> vets);
}
