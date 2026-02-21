package com.gmavrommatis.mapper;

import com.gmavrommatis.config.domain.VetRelated;
import com.gmavrommatis.model.response.VetResponse;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting {@link VetRelated} entities into {@link VetResponse} DTOs.
 *
 * <p>Provides methods for mapping a single {@code VetRelated} as well as lists of vets. Implemented
 * at compile time by MapStruct.
 *
 * @author Your Name
 */
@Mapper(componentModel = "jsr330")
public interface VetRelatedToVetResponseMapper {

  /**
   * Converts a single {@link VetRelated} entity into a {@link VetResponse} DTO.
   *
   * @param vet the {@code VetRelated} entity to map; may be {@code null}
   * @return the corresponding {@code VetResponse} DTO, or {@code null} if the input was {@code
   *     null}
   */
  VetResponse toVetResponse(VetRelated vet);

  /**
   * Converts a list of {@link VetRelated} entities into a list of {@link VetResponse} DTOs.
   *
   * @param vets the list of {@code VetRelated} entities to map; may be {@code null}
   * @return a list of {@code VetResponse} DTOs, or {@code null} if the input list was {@code null}
   */
  List<VetResponse> toVetResponseList(List<VetRelated> vets);
}
