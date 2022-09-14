package it.acsoftware.hyperiot.base.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import it.acsoftware.hyperiot.base.messages.HyperIoTValidationMessages;
import it.acsoftware.hyperiot.base.validators.PasswordMustMatchValidator;

/**
 * @author Aristide Cittadino This @interface PasswordMustMatch checks that
 * password and passConfirm have the same value when the user registers
 * on the platform.
 */
@Constraint(validatedBy = PasswordMustMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMustMatch {
    String message() default HyperIoTValidationMessages.PASSWORD_MUST_MATCH_VALIDATION;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
