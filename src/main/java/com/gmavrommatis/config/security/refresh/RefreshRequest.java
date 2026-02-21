package com.gmavrommatis.config.security.refresh;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record RefreshRequest(String refresh_token) {}
