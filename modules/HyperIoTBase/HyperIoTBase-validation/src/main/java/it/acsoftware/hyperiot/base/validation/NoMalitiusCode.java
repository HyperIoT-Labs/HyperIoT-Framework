package it.acsoftware.hyperiot.base.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import it.acsoftware.hyperiot.base.messages.HyperIoTValidationMessages;
import it.acsoftware.hyperiot.base.validators.NoMalitiusCodeValidator;

/**
 * @author Aristide Cittadino Generic interface component for @interface
 * NoMalitiusCode. This interface defines the violation of constraints
 * to prevent the insertion of malicious code in the HyperIoT platform.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoMalitiusCodeValidator.class)
public @interface NoMalitiusCode {

    /**
     * @return default message key for creating error messages in case of constraint
     * is violated
     */
    String message() default HyperIoTValidationMessages.NO_MALITIUS_CODE_VALIDATION;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
