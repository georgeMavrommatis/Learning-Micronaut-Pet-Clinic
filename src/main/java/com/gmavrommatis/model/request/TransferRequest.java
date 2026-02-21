package com.gmavrommatis.model.request;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public @Serdeable // enables compile-time (de)serialization
record TransferRequest(
    @NotNull @Positive BigDecimal amount,
    @NotBlank @Size(max = 64) String to,
    @Size(max = 140) String note) {}
