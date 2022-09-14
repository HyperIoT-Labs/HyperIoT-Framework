package it.acsoftware.hyperiot.base.validators;

import it.acsoftware.hyperiot.base.validation.ValidLongitude;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class LongitudeValidator implements ConstraintValidator<ValidLongitude, Double> {

    protected static final String LONGITUDE_PATTERN="^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,6})?))$";

    private final DecimalFormat df;

    public LongitudeValidator() {
        df = new DecimalFormat("#.######");
        df.setRoundingMode(RoundingMode.UP);
    }

    @Override
    public boolean isValid(Double longitude, ConstraintValidatorContext context) {
        return longitude != 0.0 && df.format(longitude).matches(LONGITUDE_PATTERN);
    }

}
