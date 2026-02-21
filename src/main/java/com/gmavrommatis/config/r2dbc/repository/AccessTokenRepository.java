package com.gmavrommatis.config.r2dbc.repository;

import com.gmavrommatis.config.r2dbc.domain.AccessTokenEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import org.reactivestreams.Publisher;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public interface AccessTokenRepository
    extends ReactiveStreamsCrudRepository<AccessTokenEntity, Long> {

  /** Find a token row by its JWT ID (jti). */
  @Query(
      """
      select * from petclinic.access_tokens
       where jti = :jti
       limit 1
      """)
  Publisher<AccessTokenEntity> findByJti(String jti);

  /** Check if a token is currently active (not revoked and not expired). */
  @Query(
      """
      select exists(
        select 1
          from petclinic.access_tokens
         where jti = :jti
           and revoked = false
      )
      """)
  Publisher<Boolean> existsActiveByJti(String jti);

  /**
   * Revoke a single token by jti. Returns rows updated (1 if success, 0 if already
   * revoked/missing).
   */
  @Query(
      """
      update petclinic.access_tokens
         set revoked = true
       where jti = :jti
         and revoked = false
      """)
  Publisher<Long> revokeByJti(String jti);

  /** Revoke all currently active tokens for a user (useful for "logout all devices"). */
  @Query(
      """
      update petclinic.access_tokens
         set revoked = true
       where username = :username
         and revoked = false
         and (expires_at is null or expires_at > now())
      """)
  Publisher<Long> revokeAllActiveForUser(String username);

  /** Optional housekeeping: delete rows whose access token is already expired. */
  @Query(
      """
      delete from petclinic.access_tokens
       where expires_at is not null
         and expires_at < now()
      """)
  Publisher<Long> deleteExpired();
}
