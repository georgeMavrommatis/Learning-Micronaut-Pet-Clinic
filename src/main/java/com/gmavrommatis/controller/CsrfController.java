package com.gmavrommatis.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import io.micronaut.session.Session;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller()
public class CsrfController {

  /**
   * This csrf is generated with Same Site Strict, so it will not be accessible to cross-origin
   * sites.
   *
   * @param session Session
   * @return HttpResponse
   */
  @Get("/csrf")
  public HttpResponse<String> csrf(Session session) {
    Optional<String> token = session.get("csrfToken", String.class);
    Cookie cookie =
        Cookie.of(
                "CSRF-TOKEN",
                token.orElseThrow(() -> new RuntimeException("No CSRF enabled for application")))
            .httpOnly(false)
            .path("/")
            .secure(true)
            .sameSite(SameSite.Strict);
    return HttpResponse.ok("csrf cookie retrieved").cookie(cookie);
  }
}
