package com.gmavrommatis.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.session.Session;
import io.micronaut.session.SessionStore;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/admin")
public class SessionInspectorController {

  @Inject SessionStore<Session> sessionStore;

  @Get("/session")
  public CompletableFuture<HttpResponse<?>> getSession(@CookieValue("SESSION") String encodedSid) {
    // Decode the cookie back to the raw session ID
    String rawId = new String(Base64.getUrlDecoder().decode(encodedSid), StandardCharsets.UTF_8);

    // Find the session
    return sessionStore
        .findSession(rawId)
        .thenApply(
            optSession -> {
              if (optSession.isEmpty()) {
                return HttpResponse.notFound();
              }
              Session session = optSession.get();

              // Extract attribute names → values
              Map<String, Object> attrs =
                  session.names().stream()
                      .collect(Collectors.toMap(cs -> cs, cs -> session.get(cs).orElse("")));

              Map<String, Object> payload =
                  Map.of(
                      "id", session.getId(),
                      "creationTime", session.getCreationTime(),
                      "lastAccessedTime", session.getLastAccessedTime(),
                      "maxInactiveInterval", session.getMaxInactiveInterval().getSeconds(),
                      "attributes", attrs);

              return HttpResponse.ok(payload);
            });
  }
}
