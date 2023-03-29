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

import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.validation.PasswordMustMatch;

/**
 * @author Aristide Cittadino This class implements a constraint validator, able
 * to validate password with a @PasswordMustMatch annotation.
 */
public class PasswordMustMatchValidator implements ConstraintValidator<PasswordMustMatch, HyperIoTAuthenticable> {
    private Logger log = LoggerFactory.getLogger(PasswordMustMatchValidator.class.getName());

    @Override
    public boolean isValid(HyperIoTAuthenticable authEntity, ConstraintValidatorContext context) {
        log.debug(
                "Validating value with @PasswordMustMatch with authEntity: {}" , authEntity.getClass().getName());
        if ((authEntity.getId() == 0 || (authEntity.getPassword() != null && authEntity.getPasswordConfirm() != null))
                && (authEntity.getPassword() == null
                || !authEntity.getPassword().equals(authEntity.getPasswordConfirm()))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "{it.acsoftware.hyperiot.huser.validator.passwordMustMatch.message}").addConstraintViolation();
            log.debug( "@PasswordMustMatch validation failed: {}" , authEntity.getClass().getName());
            return false;
        }
        return true;
    }

}
