package it.acsoftware.hyperiot.base.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import it.acsoftware.hyperiot.base.messages.HyperIoTValidationMessages;
import it.acsoftware.hyperiot.base.validators.PowOf2Validator;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PowOf2Validator.class)
public @interface PowOf2 {
    String message() default HyperIoTValidationMessages.POW_OF_2_VALIDATION;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
