package com.gmavrommatis.mapper;

import com.gmavrommatis.config.domain.Specialty;
import com.gmavrommatis.model.response.SpecialtyResponse;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting {@link Specialty} domain entities into {@link SpecialtyResponse}
 * DTOs.
 *
 * <p>Provides methods for mapping single instances as well as lists of specialties.
 *
 * <p>Implemented at compile time by MapStruct.
 *
 * @author Your Name
 */
@Mapper(componentModel = "jsr330")
public interface SpecialtyToSpecialtyResponseMapper {

  /**
   * Converts a single {@link Specialty} entity into a {@link SpecialtyResponse} DTO.
   *
   * @param specialty the {@code Specialty} entity to map; may be {@code null}
   * @return the corresponding {@code SpecialtyResponse} DTO, or {@code null} if the input was
   *     {@code null}
   */
  SpecialtyResponse toSpecialtyResponse(Specialty specialty);

  /**
   * Converts a list of {@link Specialty} entities into a list of {@link SpecialtyResponse} DTOs.
   *
   * @param specialties the list of {@code Specialty} entities to map; may be {@code null}
   * @return a list of {@code SpecialtyResponse} DTOs, or {@code null} if the input list was {@code
   *     null}
   */
  List<SpecialtyResponse> toSpecialtyResponseList(List<Specialty> specialties);
}
