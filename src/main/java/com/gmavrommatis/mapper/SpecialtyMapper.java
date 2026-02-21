package com.gmavrommatis.mapper;

import com.gmavrommatis.config.domain.Specialty;
import com.gmavrommatis.config.domain.SpecialtyRelated;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper between {@link Specialty} and {@link SpecialtyRelated}.
 *
 * <p>Note: vetRelatedHashSet is ignored to avoid deep cycles by default.
 */
@Mapper(componentModel = "jsr330")
public interface SpecialtyMapper {

  SpecialtyMapper INSTANCE = Mappers.getMapper(SpecialtyMapper.class);

  /**
   * Map from core entity to "related" entity. vetRelatedHashSet is ignored to avoid recursion /
   * large graphs.
   */
  @Mapping(target = "vets", ignore = true)
  SpecialtyRelated toRelated(Specialty source);

  Set<SpecialtyRelated> toRelatedSet(Set<Specialty> source);
}
