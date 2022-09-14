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
