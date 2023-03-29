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
