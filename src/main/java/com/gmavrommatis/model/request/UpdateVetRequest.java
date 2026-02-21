package com.gmavrommatis.model.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Introspected
@Serdeable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVetRequest {
  /** New first name; required for full replace, or leave blank for no change */
  private String firstName;

  /** New last name; required for full replace, or leave blank for no change */
  private String lastName;

  /**
   * New set of specialty IDs. If non-null, replaces the vet’s specialties. If null, leave the
   * existing specialties unchanged.
   */
  private Set<String> specialtyNames;
}
