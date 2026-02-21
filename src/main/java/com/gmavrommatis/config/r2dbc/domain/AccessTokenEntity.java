package com.gmavrommatis.config.r2dbc.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.*;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Serdeable
@Introspected
@MappedEntity(schema = "petclinic", value = "access_tokens")
public class AccessTokenEntity {

  @Id @GeneratedValue private Long id;

  /** Subject / username the token was issued for. */
  @NonNull private String username;

  /** Optional JWT ID; useful for revocation checks. */
  @Nullable private String jti;

  /** Creation timestamp (when this row is written). */
  @DateCreated
  @MappedProperty("created_at")
  private Instant createdAt;

  /**
   * Expiration timestamp from JWT `exp` (seconds since epoch).
   *
   * <p>Expiration timestamp (when this access token expires) after the specified time, this entity
   * no longer need to exist in database as JWT is expired and Micronaut will not accept it anyway
   */
  @MappedProperty("expires_at")
  private Instant expiresAt;

  /** If true, deny this token even if not expired. */
  @MappedProperty("revoked")
  private boolean revoked;
}
