package com.gmavrommatis.config.r2dbc.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Serdeable
@Introspected
@MappedEntity(schema = "petclinic", value = "refresh_tokens")
public class RefreshTokenEntity {
  @Id @GeneratedValue private Long id;

  @NonNull private String tokenHash;
  @NonNull private String username;

  @TypeDef(type = DataType.STRING_ARRAY)
  @Nullable
  private String[] roles;

  @TypeDef(type = DataType.JSON)
  @Nullable
  private Map<String, Object> attributes;

  @DateCreated @Nullable private Instant createdAt;
  @Nullable private Instant expiresAt;
  @Nullable private Instant revokedAt;
  @Nullable private Instant usedAt;
}
