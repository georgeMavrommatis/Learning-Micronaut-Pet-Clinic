package com.gmavrommatis.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.utils.SecurityService;
import java.net.URI;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
public class HomeController {

  private final SecurityService security;

  public HomeController(SecurityService security) {
    this.security = security;
  }

  @Get("/")
  public Publisher<MutableHttpResponse<String>> home() {
    return Mono.defer(
        () ->
            Mono.just(security.getAuthentication().isPresent())
                .map(
                    authenticated ->
                        Boolean.TRUE.equals(authenticated)
                            ? HttpResponse.seeOther(URI.create("/pet-clinic/details"))
                            : HttpResponse.seeOther(URI.create("/oauth/login/keycloak"))));
  }
}
