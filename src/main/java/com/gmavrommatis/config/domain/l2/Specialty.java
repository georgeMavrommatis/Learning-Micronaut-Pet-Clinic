package com.gmavrommatis.config.domain.l2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The {@code Specialty} entity represents a veterinary specialty area (e.g., surgery, dentistry)
 * within the Pet Clinic domain.
 *
 * <p>Each specialty has a unique name and may be associated with multiple {@link Vet veterinarians}
 * who practice in that area.
 *
 * @author GewrgiosMmavrommatis
 */
@Entity
@Introspected
@Serdeable
@Table(name = "specialties", schema = "petclinic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Specialty {

  /**
   * The primary key identifier for this specialty.
   *
   * <p>Generated automatically by the database using an identity column.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The unique name of the specialty.
   *
   * <p>This field cannot be null and must be unique across all specialties.
   */
  @Column(name = "name", nullable = false, unique = true)
  private String name;

  /**
   * The set of veterinarians associated with this specialty.
   *
   * <p>This is the inverse side of the {@link Vet#specialties} relationship. Lazy-loaded to avoid
   * unnecessary fetching.
   */
  @JsonIgnore
  @ManyToMany(mappedBy = "specialties", fetch = FetchType.LAZY)
  private Set<Vet> vets = new HashSet<>();
}
