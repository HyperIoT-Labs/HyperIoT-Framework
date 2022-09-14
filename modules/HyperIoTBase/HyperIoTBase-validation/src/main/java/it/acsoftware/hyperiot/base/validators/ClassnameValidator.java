package it.acsoftware.hyperiot.base.validators;

import it.acsoftware.hyperiot.base.validation.ValidClassname;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassnameValidator implements ConstraintValidator<ValidClassname, String> {

    private final Logger log = LoggerFactory.getLogger(PasswordMustMatchValidator.class.getName());

    @Override
    public boolean isValid(String classname, ConstraintValidatorContext context) {
        log.debug( "Validating value {} with @ValidClassname" , classname);
        final String VALID_CLASSNAME_REGEX = "^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+\\.[A-Z][A-Za-z0-9_]*$";
        if (classname != null && !classname.isEmpty() && !classname.matches(VALID_CLASSNAME_REGEX)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "{it.acsoftware.hyperiot.algorithm.validator.validClassname.message}")
                    .addConstraintViolation();
            log.debug("@ValidClassname validation failed");
            return false;
        }
        return true;
    }

}
