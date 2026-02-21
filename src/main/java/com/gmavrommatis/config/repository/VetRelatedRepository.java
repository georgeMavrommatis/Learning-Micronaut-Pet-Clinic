package com.gmavrommatis.config.repository;

import com.gmavrommatis.config.domain.VetRelated;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

/**
 * Repository interface for {@link VetRelated} entities. Provides CRUD operations, pagination
 * support, and custom queries.
 *
 * <p>Note: For paginated queries returning {@link Page}, Micronaut requires both a content query
 * and a count query.
 *
 * @author GewrgiosMmavrommatis
 */
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface VetRelatedRepository extends PageableRepository<VetRelated, Long> {

  /**
   * Returns a page of veterinarians with their specialties eagerly fetched.
   *
   * <p>The {@code specialties} association is fetched with a LEFT join so that the returned {@link
   * VetRelated} instances will include populated {@code specialties} collections when available.
   * Take care when paging with fetch-joins: underlying SQL rows may repeat the same veterinarian
   * when it has multiple specialties, which can affect pagination semantics.
   *
   * @param pageable paging and sorting information; must not be {@code null}
   * @return a {@link Page} of {@link VetRelated} entities; the page content may include vets whose
   *     {@code specialties} collections are populated or empty if none exist
   * @throws io.micronaut.data.exceptions.DataAccessException if a data access error occurs
   */
  @Join(value = "specialties", type = Join.Type.LEFT_FETCH) // reference:
  // https://micronaut-projects.github.io/micronaut-data/latest/guide/?utm_source=chatgpt.com#hibernateJoinQueries
  Page<VetRelated> findAll(Pageable pageable);
}
