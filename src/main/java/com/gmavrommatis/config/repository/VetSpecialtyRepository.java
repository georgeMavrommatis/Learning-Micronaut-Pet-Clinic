package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.VetSpecialty;
import com.gmavrommatis.config.domain.VetSpecialtyId;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

/**
 * Repository interface for {@link VetSpecialty} entities. Provides CRUD operations and custom
 * queries for vet-specialty associations.
 *
 * @author GewrgiosMmavrommatis
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VetSpecialtyRepository extends CrudRepository<VetSpecialty, VetSpecialtyId> {

  /**
   * Finds all vet-specialty associations for a given vet.
   *
   * @param vetId the ID of the vet
   * @return a list of {@link VetSpecialty} entities associated with the vet
   */
  List<VetSpecialty> findByVetId(Long vetId);

  /**
   * Deletes all vet-specialty associations for a given vet.
   *
   * @param vetId the ID of the vet whose associations should be deleted
   * @return the number of rows deleted
   */
  @Query("DELETE FROM petclinic.vet_specialties WHERE vet_id = :vetId")
  Long deleteByVetId(Long vetId);
}
