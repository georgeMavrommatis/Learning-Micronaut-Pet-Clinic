package com.gmavrommatis.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.net.URI;
import java.security.Principal;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
public class Home {

  @Get
  MutableHttpResponse<?> home(@Nullable Principal principal) {
    if (principal != null) return HttpResponse.redirect(URI.create("/pet-clinic/details"));
    else {
      return HttpResponse.ok().body("You are logged out, try to Login");
    }
  }
}
