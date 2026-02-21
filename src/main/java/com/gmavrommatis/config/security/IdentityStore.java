package com.gmavrommatis.config.security;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.format.MapFormat;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("identity-store")
public class IdentityStore {

  @MapFormat Map<String, String> users;
  @MapFormat Map<String, String> roles;
  // username → (attributeName → value)
  @NonNull @NotNull private Map<String, Map<String, Object>> attributes = new HashMap<>();

  public String getUserPassword(String username) {
    return users.get(username);
  }

  public String getUserRole(String username) {
    return roles.get(username);
  }

  public Map<String, Object> getAttributes(String username) {
    return attributes.get(username);
  }

  public void setAttributes(Map<String, Map<String, Object>> attributes) {
    this.attributes = attributes;
  }
}
