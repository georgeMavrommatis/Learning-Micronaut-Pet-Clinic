package com.gmavrommatis.config.security;

import com.gmavrommatis.config.r2dbc.domain.AccessTokenEntity;
import com.gmavrommatis.config.r2dbc.repository.AccessTokenRepository;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.event.AccessTokenGeneratedEvent;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import io.r2dbc.spi.Connection;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
public class AccessTokenPersistListener
    implements ApplicationEventListener<AccessTokenGeneratedEvent> {

  private final AccessTokenRepository repo;
  private final ReactiveTransactionOperations<Connection> tx;
  private final ObjectMapper mapper;

  public AccessTokenPersistListener(
      AccessTokenRepository repo,
      ReactiveTransactionOperations<Connection> tx,
      ObjectMapper mapper) {
    this.repo = repo;
    this.tx = tx;
    this.mapper = mapper;
  }

  @Override
  public void onApplicationEvent(AccessTokenGeneratedEvent ev) {
    try {
      // ---- Get access token string (supports different Micronaut versions) ----
      // Many versions expose getAccessToken(); some expose getSource().
      String accessToken = null;
      try {
        accessToken =
            (String) AccessTokenGeneratedEvent.class.getMethod("getAccessToken").invoke(ev);
      } catch (ReflectiveOperationException ignore) {
        Object src = ev.getSource();
        // In some versions source is the token string; in others it's an AccessRefreshToken
        if (src instanceof String s) {
          accessToken = s;
        } else {
          // Try to call getAccessToken on the source object via reflection
          try {
            accessToken = (String) src.getClass().getMethod("getAccessToken").invoke(src);
          } catch (ReflectiveOperationException e) {
            log.warn("Cannot extract access token from event; skip persist.");
            return;
          }
        }
      }

      // ---- Extract claims from JWT payload ----
      Map<String, Object> claims = parseClaims(accessToken);
      String username = optString(claims, "sub").or(() -> getAuthName(ev)).orElse("anonymous");
      String jti = optString(claims, "jti").orElse(null);
      Number expNum = (Number) claims.get("exp");
      if (expNum == null) {
        log.warn("JWT has no exp; skip persist");
        return;
      }
      Instant expiresAt = Instant.ofEpochSecond(expNum.longValue());

      // ---- Build and save entity ----
      AccessTokenEntity row = new AccessTokenEntity();
      row.setUsername(username);
      row.setJti(jti);
      row.setExpiresAt(expiresAt);
      row.setRevoked(false);

      Mono.from(
              tx.withTransaction(
                  TransactionDefinition.DEFAULT, status -> Mono.from(repo.save(row)).then()))
          .doOnError(t -> log.error("Failed to persist access token for {}", username, t))
          .subscribe(); // listener is void → fire-and-forget

    } catch (Exception e) {
      log.error("Error handling AccessTokenGeneratedEvent", e);
    }
  }

  // ---- helpers ----

  private Map<String, Object> parseClaims(String jwt) throws Exception {
    String[] parts = jwt.split("\\.");
    if (parts.length != 3) throw new IllegalArgumentException("Invalid JWT");
    String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    return mapper.readValue(json, Map.class);
  }

  private Optional<String> optString(Map<String, Object> m, String key) {
    Object v = m.get(key);
    return (v instanceof String s && !s.isEmpty()) ? Optional.of(s) : Optional.empty();
  }

  private Optional<String> getAuthName(AccessTokenGeneratedEvent ev) {
    try {
      Authentication auth =
          (Authentication)
              AccessTokenGeneratedEvent.class.getMethod("getAuthentication").invoke(ev);
      return Optional.ofNullable(auth).map(Authentication::getName);
    } catch (ReflectiveOperationException ignore) {
      return Optional.empty();
    }
  }
}
