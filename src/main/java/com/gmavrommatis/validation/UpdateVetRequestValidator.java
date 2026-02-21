package com.gmavrommatis.validation;

import com.gmavrommatis.model.request.UpdateVetRequest;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validator for the {@link UpdateVetRequestValidation} constraint.
 *
 * <p>Enforces that at least one of the following fields on {@link UpdateVetRequest} is non-blank:
 *
 * <ul>
 *   <li>firstName
 *   <li>lastName
 *   <li>specialtyNames
 * </ul>
 *
 * If none are provided, it dynamically constructs an error message naming each property.
 *
 * @author gewrgios mavrommatis
 */
@Singleton
public class UpdateVetRequestValidator
    implements ConstraintValidator<UpdateVetRequestValidation, UpdateVetRequest> {

  @Override
  public boolean isValid(UpdateVetRequest dto, ConstraintValidatorContext context) {

    if (dto == null) {
      return false;
    }

    // Check each field for non-blank
    boolean anyProvided =
        StringUtils.trimToNull(dto.getFirstName()) != null
            || StringUtils.trimToNull(dto.getLastName()) != null
            || (dto.getSpecialtyNames() != null
                && dto.getSpecialtyNames().stream()
                    .allMatch(specialty -> StringUtils.trimToNull(specialty) != null));

    if (!anyProvided) {
      // Turn off the default "{…must be provided}" message
      context.disableDefaultConstraintViolation();

      // Reflectively gather the field names from the DTO class
      String fieldList =
          Stream.of(UpdateVetRequest.class.getDeclaredFields())
              .map(java.lang.reflect.Field::getName)
              .collect(Collectors.joining(", "));

      // Build a custom violation message
      String message = "At least one of [" + fieldList + "] must be provided";

      // Associate the custom message with the root bean (class-level constraint)
      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    return anyProvided;
  }
}
