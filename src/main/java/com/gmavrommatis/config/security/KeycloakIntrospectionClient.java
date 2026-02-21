package com.gmavrommatis.config.security;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.endpoints.introspection.IntrospectionResponse;
import java.util.Map;
import org.reactivestreams.Publisher;

/**
 * Minimal HTTP client for Keycloak's OAuth 2.0 Token Introspection endpoint (RFC 7662).
 *
 * <p>Sends a {@code application/x-www-form-urlencoded} request containing the token to check, and
 * expects a JSON body with the introspection result. This is used by the validator to decide
 * whether an access token is still active (revocation-aware).
 *
 * <h3>Configuration</h3>
 *
 * Base URL comes from {@code keycloak.base-url}; default points to a local dev instance. The realm
 * is fixed to {@code pet-clinic} in the path. If you need a dynamic realm, parameterize the path to
 * {@code /realms/{realm}/...} and add a {@code @PathVariable}.
 *
 * <h3>Request</h3>
 *
 * <ul>
 *   <li>Header: {@code Authorization: Basic base64(clientId:clientSecret)}
 *   <li>Form fields:
 *       <ul>
 *         <li>{@code token} (required): the token to inspect
 *         <li>{@code token_type_hint} (optional): {@code access_token} or {@code refresh_token}
 *       </ul>
 * </ul>
 *
 * <h3>Response</h3>
 *
 * A JSON object mapped to {@link IntrospectionResponse}. The important flag is {@code active}.
 * Treat network errors or non-2xx statuses as "inactive" in the caller.
 *
 * <h3>Notes</h3>
 *
 * <ul>
 *   <li>Do not log tokens or client secrets.
 *   <li>Use HTTPS in non-local environments.
 * </ul>
 *
 * @author gewrgios mavrommatis
 * @since 1.0
 */
@Client("${keycloak.base-url:`http://localhost:8888`}")
public interface KeycloakIntrospectionClient {

  /**
   * Calls Keycloak token introspection for the {@code pet-clinic} realm.
   *
   * @param body form fields (at minimum {@code token}; optionally {@code token_type_hint})
   * @param basicAuth {@code Authorization} header value with HTTP Basic credentials
   * @return a publisher emitting the parsed {@link IntrospectionResponse}
   */
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_FORM_URLENCODED)
  @Post("/realms/pet-clinic/protocol/openid-connect/token/introspect")
  Publisher<IntrospectionResponse> introspect(
      @Body Map<String, String> body, @Header("Authorization") String basicAuth);
}
