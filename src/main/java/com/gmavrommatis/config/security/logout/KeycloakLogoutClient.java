package com.gmavrommatis.config.security.logout;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;
import java.util.Map;
import org.reactivestreams.Publisher;

@Client("${keycloak.base-url:`http://localhost:8888`}")
interface KeycloakLogoutClient {

  @Produces(MediaType.APPLICATION_FORM_URLENCODED)
  @Post("/realms/{realm}/protocol/openid-connect/logout")
  Publisher<HttpResponse<?>> logout(@PathVariable String realm, @Body Map<String, String> form);

  @Produces(MediaType.APPLICATION_FORM_URLENCODED)
  @Post("/realms/{realm}/protocol/openid-connect/revoke")
  Publisher<HttpResponse<?>> revoke(@PathVariable String realm, @Body Map<String, String> form);
}
