package com.gmavrommatis.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.utils.SecurityService;
import io.micronaut.serde.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.Data;

@Data
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/admin")
public class TokenInspectorController {

  private final SecurityService security;
  private final ObjectMapper mapper;

  @io.micronaut.context.annotation.Value(
      "${micronaut.security.token.jwt.signatures.secret.generator.secret:}")
  Optional<String> hsSecret;

  @Get("/claims")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  Object claims() {
    return security
        .getAuthentication()
        .map(a -> a.getAttributes()) // verified JWT claims
        .orElseGet(Map::of);
  }

  @RolesAllowed({"ADMIN"})
  @Get("/token-debug")
  public Map<String, Object> tokenDebug(HttpRequest<?> req) throws Exception {
    String auth = req.getHeaders().get("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) return Map.of("error", "No Bearer token");

    String jwt = auth.substring(7);
    String[] p = jwt.split("\\."); // [header, payload, signature]
    if (p.length != 3) return Map.of("error", "Malformed JWT");

    String headerJson = new String(Base64.getUrlDecoder().decode(p[0]));
    String payloadJson = new String(Base64.getUrlDecoder().decode(p[1]));
    String signature = p[2]; // base64url

    boolean verified = false;
    if (hsSecret.isPresent()) {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(hsSecret.get().getBytes(), "HmacSHA256"));
      byte[] expected = mac.doFinal((p[0] + "." + p[1]).getBytes());
      byte[] actual = Base64.getUrlDecoder().decode(signature);
      verified = MessageDigest.isEqual(expected, actual);
    } // For RS256/EC: rely on Micronaut’s validation (JWKS). You can still show header/claims.

    return Map.of(
        "header", mapper.readValue(headerJson, Map.class),
        "claims", mapper.readValue(payloadJson, Map.class),
        "signature",
            Map.of(
                "b64url",
                signature.substring(0, Math.min(20, signature.length())) + "…",
                "verifiedHS256",
                verified));
  }
}
