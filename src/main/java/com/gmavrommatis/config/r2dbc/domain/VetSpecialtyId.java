package com.gmavrommatis.config.r2dbc.domain;

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
 * @version 1.0
 */
@Serdeable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetSpecialtyId implements Serializable {

  /**
   * The identifier of the {@link com.gmavrommatis.config.r2dbc.domain.Vet}.
   *
   * <p>Mapped to the <code>vet_id</code> column in the join table.
   */
  @MappedProperty("vet_id")
  private Long vetId;

  /**
   * The identifier of the {@link com.gmavrommatis.config.r2dbc.domain.Specialty}.
   *
   * <p>Mapped to the <code>specialty_id</code> column in the join table.
   */
  @MappedProperty("specialty_id")
  private Long specialtyId;
}
