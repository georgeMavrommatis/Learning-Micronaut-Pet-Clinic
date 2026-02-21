package com.gmavrommatis.config.security.refresh;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
record KeycloakTokenDTO(
    String access_token,
    String refresh_token,
    String token_type,
    Integer expires_in,
    String id_token) {}
