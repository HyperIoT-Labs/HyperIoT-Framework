/*
 * Copyright 2019-2023 HyperIoT
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
