package com.gmavrommatis.model.kafka;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public record VetReviewNotificationEvent(
    String vetFirstName, String vetLastName, String reviewer, String reviewContent) {}
