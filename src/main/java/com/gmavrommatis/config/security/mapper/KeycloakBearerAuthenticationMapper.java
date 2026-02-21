package com.gmavrommatis.config.security.mapper;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Maps a successful OAuth2 token exchange from the {@code keycloak} client into a Micronaut {@link
 * AuthenticationResponse}. The goal is to make the upstream tokens (access/refresh/type/expiry)
 * available to downstream components (e.g. a custom {@code LoginHandler}) via authentication
 * attributes.
 *
 * <p>The bean name must match the OAuth client id under {@code micronaut.security.oauth2.clients}:
 * this mapper will only be used for that client.
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>Attributes are kept simple and provider-agnostic.
 *   <li>Nulls are allowed (e.g. refresh token may not be issued).
 *   <li>Do not log the tokens; treat them as secrets.
 * </ul>
 *
 * @author gewrgios mavrommatis
 * @since 1.0
 */
@Named("keycloak") // must match your client name under oauth2.clients
@Singleton
public class KeycloakBearerAuthenticationMapper implements OauthAuthenticationMapper {

  /**
   * Builds a successful {@link AuthenticationResponse} carrying the upstream tokens in the
   * attributes map so that later layers (e.g. a {@code LoginHandler}) can serialize them back to
   * the client.
   *
   * @param tr the token response returned by the IdP after code exchange or client credentials
   * @param state the original state (unused here)
   * @return a publisher emitting a successful authentication with token attributes
   */
  @Override
  public Publisher<AuthenticationResponse> createAuthenticationResponse(
      TokenResponse tr, @Nullable State state) {
    Map<String, Object> attrs = new HashMap<>();
    attrs.put("kc_access_token", tr.getAccessToken());
    attrs.put("kc_refresh_token", tr.getRefreshToken());
    attrs.put("kc_token_type", tr.getTokenType());
    attrs.put("kc_expires_in", tr.getExpiresIn());

    // principal name is not used by our LoginHandler, keep it constant
    return Mono.just(AuthenticationResponse.success("kc", attrs));
    // If you prefer to avoid Reactor:
    // return Publishers.just(AuthenticationResponse.success("kc", attrs));
  }
}
