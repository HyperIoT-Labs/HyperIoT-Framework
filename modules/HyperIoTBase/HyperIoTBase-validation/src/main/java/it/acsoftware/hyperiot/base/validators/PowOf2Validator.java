package it.acsoftware.hyperiot.base.validators;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import it.acsoftware.hyperiot.base.validation.PowOf2;

public class PowOf2Validator implements ConstraintValidator<PowOf2, Number> {
    private Logger log = LoggerFactory.getLogger(PowOf2Validator.class.getName());

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        log.debug( "Validating value with @PowOf2 with value: {}" , value);
        if (value == null)
            return false;
        int intValue = value.intValue();
        boolean isValid = intValue > 0 && Math.round(value.doubleValue()) == intValue && (intValue == 1 || intValue % 2 == 0);
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{it.acsoftware.hyperiot.validator.powOf2.message}")
                    .addConstraintViolation();
            log.debug( "@PowOf2 validation failed: {}" , value);
        }
        return isValid;
    }

}
