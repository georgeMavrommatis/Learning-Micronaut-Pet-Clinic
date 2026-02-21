package com.gmavrommatis.config.security;

import com.gmavrommatis.config.r2dbc.repository.AccessTokenRepository;
import com.gmavrommatis.utils.JwtUtils;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.security.utils.SecurityService;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * HTTP server filter that enforces access-token revocation.
 *
 * <p>The filter runs <em>after</em> Micronaut security has already authenticated the request using
 * the bearer JWT. It extracts the token's {@code jti} claim and checks against a backing store (via
 * {@link AccessTokenRepository}) to decide whether the token is still active. If the token is
 * revoked, the request is rejected with {@code 401 Unauthorized}; otherwise, the filter lets the
 * request proceed.
 *
 * <p>Lookups are performed reactively to avoid blocking the event loop.
 *
 * @author gewrgios mavrommatis
 */
@Singleton
@Filter("/**")
public class JtiBlacklistFilter implements HttpServerFilter {

  private final SecurityService securityService;
  private final AccessTokenRepository accessTokenRepo;

  /**
   * Creates a new blacklist filter.
   *
   * @param securityService Micronaut security service used to check authentication state
   * @param accessTokenRepo Repository that can determine if a given {@code jti} is still active
   */
  public JtiBlacklistFilter(
      SecurityService securityService, AccessTokenRepository accessTokenRepo) {
    this.securityService = securityService;
    this.accessTokenRepo = accessTokenRepo;
  }

  /**
   * Run after the security phase so bearer authentication has already taken place. If your
   * Micronaut version lacks {@link ServerFilterPhase}, return a large number (e.g. {@code 1000}).
   */
  @Override
  public int getOrder() {
    return ServerFilterPhase.SECURITY.after();
  }

  /**
   * If the request is authenticated, extract the JWT's {@code jti} and verify that it has not been
   * revoked. Otherwise, pass the request through untouched.
   */
  @Override
  public Publisher<MutableHttpResponse<?>> doFilter(
      HttpRequest<?> request, ServerFilterChain chain) {
    return Mono.defer(
        () -> {
          if (!securityService.isAuthenticated()) {
            return Mono.from(chain.proceed(request));
          }

          return JwtUtils.requireJti(securityService)
              .flatMap(jti -> Mono.from(accessTokenRepo.existsActiveByJti(jti)))
              .flatMap(
                  active ->
                      active
                          ? Mono.from(chain.proceed(request))
                          : Mono.just(HttpResponse.unauthorized()));
        });
  }
}
