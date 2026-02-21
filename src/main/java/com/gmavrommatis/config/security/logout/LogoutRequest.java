package com.gmavrommatis.config.security.logout;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record LogoutRequest(String refresh_token) {}
