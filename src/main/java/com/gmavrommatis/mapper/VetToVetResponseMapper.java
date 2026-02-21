package com.gmavrommatis.mapper;

import com.gmavrommatis.config.domain.Vet;
import com.gmavrommatis.model.response.VetResponse;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper interface for converting {@link Vet} entities into {@link VetResponse} DTOs.
 *
 * <p>Supports two mapping strategies:
 *
 * <ul>
 *   <li><strong>Lazy (Basic)</strong> – does not fetch or include lazy-loaded relationships (e.g.,
 *       specialties).
 *   <li><strong>Eager (Detailed)</strong> – includes all data, triggering any lazy-loaded fields.
 * </ul>
 *
 * <p>Implemented at compile time by MapStruct.
 *
 * @author GewrgiosMmavrommatis
 */
@Mapper(componentModel = "jsr330")
public interface VetToVetResponseMapper {

  /*** BASIC MAPPING ***/

  /**
   * Maps a single {@link Vet} entity to a {@link VetResponse} DTO using basic mapping.
   *
   * <p>Ignores the specialties field to avoid triggering lazy loading.
   *
   * @param vet the {@code Vet} entity to map; may be {@code null}
   * @return a {@code VetResponse} with core fields mapped, or {@code null} if {@code vet} is {@code
   *     null}
   */
  @Named("lazyVet")
  @Mapping(target = "specialties", ignore = true)
  VetResponse toVetResponseLazy(Vet vet);

  /**
   * Maps a list of {@link Vet} entities to a list of {@link VetResponse} DTOs using basic mapping.
   *
   * @param vets the list of {@code Vet} entities; may be {@code null}
   * @return a list of {@code VetResponse} objects, or {@code null} if {@code vets} is {@code null}
   */
  @IterableMapping(qualifiedByName = "lazyVet")
  List<VetResponse> toVetResponseLazyList(List<Vet> vets);

  /*** DETAILED MAPPING ***/

  /**
   * Maps a single {@link Vet} entity to a {@link VetResponse} DTO using detailed mapping.
   *
   * <p>Includes all fields, triggering any lazy-loaded relationships.
   *
   * @param vet the {@code Vet} entity to map; may be {@code null}
   * @return a fully populated {@code VetResponse}, or {@code null} if {@code vet} is {@code null}
   */
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
