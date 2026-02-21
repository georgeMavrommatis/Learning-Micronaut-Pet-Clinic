package com.gmavrommatis.validation;

import com.gmavrommatis.model.request.UpdateVetRequest;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * A class‐level constraint that ensures an {@link UpdateVetRequest} has at least one of its fields
 * non-blank:
 *
 * <ul>
 *   <li>{@code firstName}
 *   <li>{@code lastName}
 *   <li>{@code specialtyNames}
 * </ul>
 *
 * <p>Apply this to your DTO to enforce “at least one provided” semantics:
 *
 * <pre>
 *   &#64;UpdateVetRequestValidation
 *   public class UpdateVetRequest { … }
 * </pre>
 *
 * @see UpdateVetRequestValidator
 * @author gewrgios mavrommatis
 */
@Target(ElementType.TYPE) // Only on classes (or interfaces)
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for validation
@Documented // Included in generated Javadoc
@Constraint(validatedBy = UpdateVetRequestValidator.class)
public @interface UpdateVetRequestValidation {

  /**
   * The default validation message when <em>all</em> fields are null. This message can be
   * overridden via resource bundles (e.g. in messages.properties with key {@code
   * com.example.vet.UpdateVetRequestValidation.message}).
   *
   * @return the message template
   */
  String message() default "At least one field must be provided";

  /**
   * Allows the specification of validation groups to which this constraint belongs.
   *
   * @return array of group classes
   */
  Class<?>[] groups() default {};

  /**
   * Can be used by clients of the Jakarta Validation API to assign custom payload objects to a
   * constraint.
   *
   * @return array of payload classes
   */
  Class<? extends Payload>[] payload() default {};
}
