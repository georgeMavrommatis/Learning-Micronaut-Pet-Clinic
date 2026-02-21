package com.gmavrommatis.config.r2dbc.repository;

import com.gmavrommatis.config.r2dbc.domain.RefreshTokenEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import org.reactivestreams.Publisher;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface RefreshTokenRepository
    extends ReactiveStreamsCrudRepository<RefreshTokenEntity, Long> {

  @Query(
      """
      select * from petclinic.refresh_tokens
       where token_hash = :hash
       limit 1
      """)
  Publisher<RefreshTokenEntity> findByHash(String hash);

  /**
   * Revoke only the users refresh tokens expired or never expire that are used and not revoked
   * already.
   *
   * @param username
   * @return
   */
  @Query(
      """
      update petclinic.refresh_tokens
         set revoked_at = now()
       where username = :username
         and revoked_at is null
         and used_at is null
         and (expires_at is null or expires_at > now())
      """)
  Publisher<Long> revokeAllActiveForUser(String username);

  @Query(
      """
      update petclinic.refresh_tokens
         set used_at = now()
       where token_hash = :hash and used_at is null
      """)
  Publisher<Long> markUsed(String hash);
}
