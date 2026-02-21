package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.Vet;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;
import java.util.List;

/**
 * Repository interface for {@link Vet} entities. Provides CRUD operations, pagination support, and
 * custom queries.
 *
 * <p>Note: For paginated queries returning {@link Page}, Micronaut requires both a content query
 * and a count query.
 *
 * @author GewrgiosMmavrommatis
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VetRepository extends PageableRepository<Vet, Long> {

  /**
   * Fetches a page of vets with content and total count queries.
   *
   * @param pageable the {@link Pageable} describing page number and size
   * @return a {@link Page} containing the requested slice of vets
   */
  @Query(value = "SELECT * FROM petclinic.vets", countQuery = "SELECT COUNT(*) FROM petclinic.vets")
  Page<Vet> findAllPaged(Pageable pageable);

  /**
   * Returns the total number of vets.
   *
   * @return the total count of vet records
   */
  @Query("SELECT COUNT(*) FROM petclinic.vets")
  Long countAll();

  /**
   * Finds all vets with the given last name.
   *
   * @param lastName the last name to match
   * @return a list of matching {@link Vet} entities
   */
  List<Vet> findByLastName(String lastName);

  /**
   * Finds all vets matching both first and last name.
   *
   * @param firstName the first name to match
   * @param lastName the last name to match
   * @return a list of matching {@link Vet} entities
   */
  List<Vet> findByFirstNameAndLastName(String firstName, String lastName);

  /**
   * Deletes vets matching the given first and last name.
   *
   * @param firstName the first name of the vet(s) to delete
   * @param lastName the last name of the vet(s) to delete
   * @return the number of rows deleted
   */
  Long deleteByFirstNameAndLastName(String firstName, String lastName);
}
