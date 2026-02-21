package com.gmavrommatis.config.security.refresh;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;
import java.util.Map;
import org.reactivestreams.Publisher;

/**
 * Typed HTTP client for Keycloak's OAuth2/OIDC token endpoint.
 *
 * <p>Sends an {@code application/x-www-form-urlencoded} request to obtain tokens for common grants:
 *
 * <ul>
 *   <li><b>client_credentials</b>: machine-to-machine tokens
 *   <li><b>password</b>: username/password exchange (if enabled on the realm/client)
 *   <li><b>refresh_token</b>: exchange a refresh token for a new access token
 * </ul>
 *
 * <h3>Configuration</h3>
 *
 * Base URL is resolved from {@code keycloak.base-url}. The default points to a local Keycloak dev
 * server:
 *
 * <pre>{@code
 * keycloak:
 *   base-url: http://localhost:8888
 * }</pre>
 *
 * <h3>Form fields</h3>
 *
 * Provide a map with the fields required by the chosen grant:
 *
 * <ul>
 *   <li>Always: {@code grant_type}, {@code client_id}, {@code client_secret} (or Authorization
 *       header)
 *   <li>Password grant: {@code username}, {@code password}
 *   <li>Refresh grant: {@code refresh_token}
 *   <li>Optional: {@code scope} (space-delimited)
 * </ul>
 *
 * <h3>Notes</h3>
 *
 * <ul>
 *   <li>The path includes the realm: {@code /realms/{realm}/protocol/openid-connect/token}.
 *   <li>Response errors are surfaced as {@code HttpClientResponseException}.
 *   <li>Prefer HTTPS in non-local environments and avoid logging token contents.
 * </ul>
 *
 * @author gewrgios mavrommatis
 * @since 1.0
 */
@Client("${keycloak.base-url:`http://localhost:8888`}")
public interface KeycloakTokenClient {

  /**
   * Calls Keycloak's token endpoint with a form-url-encoded body. examples of form param:
   *
   * <pre>
   *              // client credentials
   *              Map.of(
   *                "grant_type", "client_credentials",
   *                "client_id",  "...",
   *                "client_secret", "..."
   *              )
   *
   *              // password
   *              Map.of(
   *                "grant_type", "password",
   *                "client_id",  "...",
   *                "client_secret", "...",
   *                "username", "...",
   *                "password", "..."
   *              )
   *
   *              // refresh
   *              Map.of(
   *                "grant_type", "refresh_token",
   *                "client_id",  "...",
   *                "client_secret", "...",
   *                "refresh_token", "..."
   *              )
   *              </pre>
   *
   * @param realm the Keycloak realm (e.g. {@code pet-clinic})
   * @param form form fields required by the grant type;
   * @return a publisher that emits {@code KeycloakTokenDTO} on success
   */
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_FORM_URLENCODED)
  @Post("/realms/{realm}/protocol/openid-connect/token")
  Publisher<KeycloakTokenDTO> token(@PathVariable String realm, @Body Map<String, String> form);
}
