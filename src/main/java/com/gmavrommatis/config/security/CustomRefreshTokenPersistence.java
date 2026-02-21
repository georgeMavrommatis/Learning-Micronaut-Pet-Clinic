package com.gmavrommatis.config.security;

import static io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT;

import com.gmavrommatis.config.r2dbc.domain.RefreshTokenEntity;
import com.gmavrommatis.config.r2dbc.repository.RefreshTokenRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import io.r2dbc.spi.Connection;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Persists and validates refresh tokens for the local JWT refresh flow.
 *
 * <p>Policy decisions implemented here:
 *
 * <ul>
 *   <li><b>Rotation on login/issue:</b> when a new refresh token is generated, any previous active
 *       tokens for the same user are revoked (see {@link #persistToken}).
 *   <li><b>Single-use on refresh:</b> the presented refresh token is marked as used; concurrent
 *       reuse is denied (see {@link #getAuthentication}).
 *   <li><b>Hash-at-rest:</b> only a SHA-256 hash of the refresh token is stored.
 *   <li><b>TTL:</b> refresh TTL = access-token TTL + 2 days (config-driven).
 * </ul>
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>This implementation uses R2DBC and Reactor. The persistence callback returns {@code void},
 *       so we subscribe internally (fire-and-forget) and log errors.
 *   <li>Revoking all active tokens on issue will sign users out on other devices; adjust to your
 *       needs.
 * </ul>
 */
@Slf4j
@Singleton
public class CustomRefreshTokenPersistence implements RefreshTokenPersistence {

  /** R2DBC repository for refresh token rows. */
  private final RefreshTokenRepository repo;

  /** Reactive transaction boundary for R2DBC work. */
  private final ReactiveTransactionOperations<Connection> tx;

  /** Duration a refresh token remains valid (access TTL + 2 days). */
  private final Duration refreshTtl;

  /**
   * @param repo R2DBC repository
   * @param tx Reactive transaction operations
   * @param accessExpSeconds Access token expiration in seconds
   *     (micronaut.security.token.jwt.generator.access-token.expiration)
   */
  public CustomRefreshTokenPersistence(
      RefreshTokenRepository repo,
      ReactiveTransactionOperations<Connection> tx,
      @Value("${micronaut.security.token.jwt.generator.access-token.expiration:1800}")
          long accessExpSeconds) {
    this.repo = repo;
    this.tx = tx;
    // Give clients a window to come back after the access token expires.
    this.refreshTtl = Duration.ofSeconds(accessExpSeconds).plus(Duration.ofDays(2));
  }

  /**
   * Called by Micronaut when a refresh token is minted (login/rotation).
   *
   * <ol>
   *   <li>Optionally revoke any previous active tokens for this user (current policy).
   *   <li>Store the new token (hashed), roles, attributes, and expiry.
   * </ol>
   *
   * <p>Fire-and-forget: the interface is {@code void}, so we subscribe here and log on error.
   */
  @Override
  public void persistToken(RefreshTokenGeneratedEvent event) {
    // Basic sanity checks. If your app doesn't require attributes, remove that condition.
    if (event == null
        || event.getRefreshToken() == null
        || event.getAuthentication() == null
        || event.getAuthentication().getName() == null
        || event.getAuthentication().getAttributes().isEmpty()) {
      return;
    }

    String username = event.getAuthentication().getName();
    Instant now = Instant.now();

    RefreshTokenEntity entity = new RefreshTokenEntity();
    entity.setTokenHash(hash(event.getRefreshToken())); // Store hash, not raw token
    entity.setUsername(username);
    entity.setRoles(event.getAuthentication().getRoles().toArray(new String[0]));
    entity.setAttributes(event.getAuthentication().getAttributes()); // Carry custom claims forward
    entity.setCreatedAt(now);
    entity.setExpiresAt(now.plus(refreshTtl));

    // One transaction: revoke previous active tokens, then insert the new one.
    Mono.from(
            tx.withTransaction(
                TransactionDefinition.DEFAULT,
                status ->
                    Mono.from(repo.revokeAllActiveForUser(username))
                        .then(Mono.from(repo.save(entity)))
                        .then()))
        .doOnError(t -> log.error("Failed to persist refresh token for {}", username, t))
        .subscribe(); // Required because method is void
  }

  /**
   * Validates a presented refresh token and returns the {@link Authentication} to be used for
   * minting a new access token (and, if your generator rotates, a new refresh token).
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Lookup by token hash.
   *   <li>Reject if missing, revoked, used, or expired (INVALID_GRANT).
   *   <li>Mark the token as used (single-use). Concurrent attempts after the first will fail.
   *   <li>Rebuild {@link Authentication} using the stored roles and attributes.
   * </ol>
   *
   * <p>Errors are reported with {@link OauthErrorResponseException} and code {@code invalid_grant}
   * to match OAuth expectations.
   */
  @Override
  public Publisher<Authentication> getAuthentication(String refreshToken) {
    String hash = hash(refreshToken);
    Instant now = Instant.now();

    Mono<RefreshTokenEntity> tokenMono =
        Mono.from(repo.findByHash(hash))
            .switchIfEmpty(
                Mono.error(
                    new OauthErrorResponseException(
                        INVALID_GRANT, "refresh token not found", null)))
            .filter(rt -> rt.getRevokedAt() == null)
            .filter(rt -> rt.getUsedAt() == null)
            .filter(rt -> rt.getExpiresAt() == null || rt.getExpiresAt().isAfter(now))
            .switchIfEmpty(
                Mono.error(
                    new OauthErrorResponseException(
                        INVALID_GRANT, "Refresh token inactive", null)));

    return tokenMono
        // Prevent replay/race: only the first caller will update a row; others get INVALID_GRANT.
        .flatMap(
            rt ->
                Mono.from(repo.markUsed(hash))
                    .flatMap(
                        rows ->
                            rows > 0
                                ? Mono.just(rt)
                                : Mono.error(
                                    new OauthErrorResponseException(
                                        INVALID_GRANT, "Refresh token already used", null))))
        // Build Authentication used by the JWT generator to shape the new access token claims.
        .map(
            rt -> {
              Map<String, Object> attrs = Optional.ofNullable(rt.getAttributes()).orElse(Map.of());
              return Authentication.build(
                  rt.getUsername(),
                  rt.getRoles() == null ? List.of() : Arrays.asList(rt.getRoles()),
                  attrs);
            });
  }

  /**
   * Compute a SHA-256 hash of the passed token and return it as a lowercase Base64URL string.
   * Useful to avoid storing refresh tokens in clear text.
   *
   * @param token The raw refresh token value.
   * @return Hex-encoded SHA-256 digest of {@code token}.
   * @throws IllegalStateException if SHA-256 is unavailable (shouldn't happen on a standard JVM).
   */
  private String hash(String token) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
