package com.gmavrommatis.utils;

import static io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_REQUEST;

import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.utils.SecurityService;
import java.security.Principal;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {

  /** extract jti */
  public static Mono<String> requireJti(SecurityService security) {
    return Mono.defer(
        () -> {
          Optional<String> fromAuth =
              security
                  .getAuthentication()
                  .map(a -> a.getAttributes().get("jti"))
                  .filter(String.class::isInstance)
                  .map(String.class::cast);

          return fromAuth
              .map(Mono::just)
              .orElseGet(
                  () ->
                      Mono.error(
                          new OauthErrorResponseException(
                              INVALID_REQUEST, "Missing jti claim", null)));
        });
  }

  public static String requireJti(Authentication authentication) {
    return (String) authentication.getAttributes().get("jti");
  }

  /** extract user */
  public static Mono<String> requireUsername(SecurityService security) {
    return Mono.defer(
        () ->
            security
                .getAuthentication()
                .map(Principal::getName)
                .map(Mono::just)
                .orElseGet(
                    () ->
                        Mono.error(
                            new OauthErrorResponseException(
                                INVALID_REQUEST, "Missing username", null))));
  }
}
