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
