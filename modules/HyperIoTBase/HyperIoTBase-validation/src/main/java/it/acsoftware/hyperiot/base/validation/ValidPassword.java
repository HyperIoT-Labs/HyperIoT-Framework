package it.acsoftware.hyperiot.base.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import it.acsoftware.hyperiot.base.messages.HyperIoTValidationMessages;
import it.acsoftware.hyperiot.base.validators.ValidPasswordValidator;

/**
 * @author Aristide Cittadino This @interface ValidPassword checks that password
 * meets all rules defined by the platform, i.e. password length least 8
 * characters.
 */
@Constraint(validatedBy = ValidPasswordValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default HyperIoTValidationMessages.VALID_PASSWORD_VALIDATION;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
