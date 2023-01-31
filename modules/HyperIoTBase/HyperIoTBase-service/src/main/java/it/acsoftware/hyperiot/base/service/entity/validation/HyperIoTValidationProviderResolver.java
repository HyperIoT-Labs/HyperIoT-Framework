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
