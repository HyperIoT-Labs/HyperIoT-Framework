package it.acsoftware.hyperiot.base.validators;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import it.acsoftware.hyperiot.base.validation.Pattern;

/**
 * @author Aristide Cittadino Since swagger is not recognizing the default
 * pattern implementation.
 */
public class PatternValidator implements ConstraintValidator<Pattern, CharSequence> {
    private java.util.regex.Pattern pattern;

    @Override
    public void initialize(Pattern parameters) {
        Pattern.Flag[] flags = parameters.flags();
        int intFlag = 0;
        for (Pattern.Flag flag : flags) {
            intFlag = intFlag | flag.getValue();
        }

        try {
            pattern = java.util.regex.Pattern.compile(parameters.regexp(), intFlag);
        } catch (PatternSyntaxException e) {
            throw e;
        }
    }

    @Override
    public boolean isValid(CharSequence value,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }

        Matcher m = pattern.matcher(value);
        return m.matches();
    }
}