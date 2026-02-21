package com.gmavrommatis.config.domain;

import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for the {@link VetSpecialty} join entity.
 *
 * <p>Combines the IDs of a {@link Vet} and a {@link Specialty} to uniquely identify the association
 * between them.
 *
 * @author Your Name
 */
@Serdeable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetSpecialtyId implements Serializable {

  /**
   * The identifier of the {@link Vet}.
   *
   * <p>Mapped to the <code>vet_id</code> column in the join table.
   */
  @MappedProperty("vet_id")
  private Long vetId;

  /**
   * The identifier of the {@link Specialty}.
   *
   * <p>Mapped to the <code>specialty_id</code> column in the join table.
   */
  @MappedProperty("specialty_id")
  private Long specialtyId;
}
