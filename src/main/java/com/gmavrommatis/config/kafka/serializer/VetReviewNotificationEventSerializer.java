package com.gmavrommatis.config.kafka.serializer;

import com.gmavrommatis.model.kafka.VetReviewNotificationEvent;
import io.micronaut.serde.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class VetReviewNotificationEventSerializer
    implements Serializer<VetReviewNotificationEvent> {

  private final ObjectMapper mapper = ObjectMapper.getDefault();

  @Override
  public byte[] serialize(String topic, VetReviewNotificationEvent data) {
    try {
      return mapper.writeValueAsBytes(data);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize VetReviewNotificationEvent", e);
    }
  }
}
