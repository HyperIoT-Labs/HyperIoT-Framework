package it.acsoftware.hyperiot.base.validation;

import it.acsoftware.hyperiot.base.messages.HyperIoTValidationMessages;
import it.acsoftware.hyperiot.base.validators.ClassnameValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = ClassnameValidator.class)
public @interface ValidClassname {

    String message() default HyperIoTValidationMessages.VALID_CLASSNAME_VALIDATION;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
