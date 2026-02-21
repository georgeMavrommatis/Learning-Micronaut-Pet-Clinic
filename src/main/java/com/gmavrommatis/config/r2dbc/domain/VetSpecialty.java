package com.gmavrommatis.config.r2dbc.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.EmbeddedId;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join entity representing the many-to-many relationship between {@link Vet} and {@link Specialty}.
 *
 * <p>Mapped to the <code>petclinic.vet_specialties</code> join table in the database. Uses a
 * composite key {@link VetSpecialtyId} to link veterinarians and specialties.
 *
 * @author Your Name
 * @version 1.0
 */
@Introspected
@Serdeable
@MappedEntity(value = "vet_specialties", schema = "petclinic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VetSpecialty {

  /**
   * The composite primary key identifying the vet-specialty association.
   *
   * <p>Contains both the vet’s ID and the specialty’s ID.
   */
  @EmbeddedId private VetSpecialtyId id;
}
