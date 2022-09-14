package it.acsoftware.hyperiot.base.validators;

import it.acsoftware.hyperiot.base.validation.ValidLatitude;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class LatitudeValidator implements ConstraintValidator<ValidLatitude, Double> {

    private static final String LATITUDE_PATTERN="^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))$";

    private final DecimalFormat df;

    public LatitudeValidator() {
        df = new DecimalFormat("#.######");
        df.setRoundingMode(RoundingMode.UP);
    }

    @Override
    public boolean isValid(Double latitude, ConstraintValidatorContext context) {
        return latitude != 0.0 && df.format(latitude).matches(LATITUDE_PATTERN);
    }
}
