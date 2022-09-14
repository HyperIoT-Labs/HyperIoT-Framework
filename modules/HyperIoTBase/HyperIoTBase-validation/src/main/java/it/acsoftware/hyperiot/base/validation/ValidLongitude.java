package it.acsoftware.hyperiot.base.validation;

import it.acsoftware.hyperiot.base.messages.HyperIoTValidationMessages;
import it.acsoftware.hyperiot.base.validators.LongitudeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LongitudeValidator.class)
public @interface ValidLongitude {

    String message() default HyperIoTValidationMessages.LONGITUDE_VALIDATION;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
