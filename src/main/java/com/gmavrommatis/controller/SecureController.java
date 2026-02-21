package com.gmavrommatis.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import java.util.Map;

@Controller("/secure")
@Secured(SecurityRule.IS_AUTHENTICATED) // require a valid token
public class SecureController {

  @Get(uri = "/anonymous", produces = MediaType.APPLICATION_JSON)
  public Map<String, Object> me(Authentication auth) {
    return Map.of(
        "user", auth.getName(),
        "roles", auth.getRoles(),
        "attributes", auth.getAttributes());
  }
}
