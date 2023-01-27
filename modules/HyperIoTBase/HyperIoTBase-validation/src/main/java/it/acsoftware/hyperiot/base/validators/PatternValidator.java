/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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