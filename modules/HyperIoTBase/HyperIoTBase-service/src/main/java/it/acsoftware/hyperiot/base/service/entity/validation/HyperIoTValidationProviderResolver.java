package it.acsoftware.hyperiot.base.service.entity.validation;

import org.hibernate.validator.HibernateValidator;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Aristide Cittadino Model class for
 * HyperIoTValidationProviderResolver. This class implements
 * ValidationProviderResolver interface in order to validate a list of
 * data from hibernate side.
 */
public class HyperIoTValidationProviderResolver implements ValidationProviderResolver {
    protected Logger log = LoggerFactory.getLogger(HyperIoTValidationProviderResolver.class.getName());

    @Override
    public List<ValidationProvider<?>> getValidationProviders() {
        log.debug( "Returning Validation provider HibernateValidator");
        return Collections.singletonList(new HibernateValidator());
    }

}
