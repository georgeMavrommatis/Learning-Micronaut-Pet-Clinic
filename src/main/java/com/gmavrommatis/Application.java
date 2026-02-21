package com.gmavrommatis;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Pet Clinic",
            version = "0.0",
            description = "Pet Clinic API",
            license =
                @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
            contact =
                @Contact(
                    url = "https://www.linkedin.com/in/george-mavrommatis-0988121b6/",
                    name = "George Mavrommatis")))
public class Application {

  public static void main(String[] args) {
    Micronaut.run(Application.class, args);
  }
}
