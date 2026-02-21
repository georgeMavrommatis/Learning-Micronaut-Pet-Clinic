package com.gmavrommatis;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@OpenAPIDefinition
public class Application {

  public static void main(String[] args) {
    Micronaut.run(Application.class, args);
  }
}
