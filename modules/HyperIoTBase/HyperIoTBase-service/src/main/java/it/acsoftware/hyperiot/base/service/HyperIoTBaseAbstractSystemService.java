package it.acsoftware.hyperiot.base.service;

import it.acsoftware.hyperiot.base.api.HyperIoTResource;
import it.acsoftware.hyperiot.base.exception.HyperIoTValidationException;
import it.acsoftware.hyperiot.base.service.entity.validation.HyperIoTValidationProviderResolver;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.*;
import java.util.Set;


public abstract class HyperIoTBaseAbstractSystemService extends HyperIoTAbstractService {
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Validates bean instance
     */
    private static Validator validator;

    protected void validate(HyperIoTResource entity) {
        configureValidator();
        Set<ConstraintViolation<HyperIoTResource>> validationResults = validator.validate(entity);
        if (validationResults != null && validationResults.size() > 0) {
            log.debug( "System Service Validation failed for entity {}: {}, errors: {}"
                , new Object[]{entity.getResourceName(), entity, validationResults});
            throw new HyperIoTValidationException(validationResults);
        }
    }


    private static synchronized void configureValidator(){
        if(validator == null) {
            Configuration<?> config = Validation.byDefaultProvider()
                    .providerResolver(new HyperIoTValidationProviderResolver()).configure()
                    .messageInterpolator(new ParameterMessageInterpolator());
            ValidatorFactory factory = config.buildValidatorFactory();
            validator = factory.getValidator();
        }
    }


}
