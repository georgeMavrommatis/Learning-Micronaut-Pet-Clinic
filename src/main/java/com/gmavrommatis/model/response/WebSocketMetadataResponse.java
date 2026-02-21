package com.gmavrommatis.model.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

@Introspected
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WebSocketMetadataResponse {

  private String type = "meta";
  private long expectedStreamSize;
}
