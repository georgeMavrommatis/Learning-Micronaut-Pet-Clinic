package com.gmavrommatis.model.kafka;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record VetReviewNotificationEvent(
    String vetFirstName, String vetLastName, String reviewer, String reviewContent) {}
