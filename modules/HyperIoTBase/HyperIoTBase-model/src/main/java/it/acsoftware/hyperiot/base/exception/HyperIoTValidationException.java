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

package it.acsoftware.hyperiot.base.exception;

import it.acsoftware.hyperiot.base.api.HyperIoTResource;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * @author Aristide Cittadino Model class for HyperIoTValidationException. It is
 * used to describe any constraint violation that occurs during runtime
 * exceptions.
 */
public class HyperIoTValidationException extends HyperIoTRuntimeException {
    /**
     * A unique serial version identifier
     */
    private static final long serialVersionUID = 1L;

    /**
     * Collection that contains constraint violations
     */
    private Set<ConstraintViolation<HyperIoTResource>> violations;

    /**
     * Constructor for HyperIoTValidationException
     *
     * @param violations parameter that indicates constraint violations produced
     */
    public HyperIoTValidationException(Set<ConstraintViolation<HyperIoTResource>> violations) {
        this.violations = violations;
    }

    /**
     * Gets the constraint violations
     *
     * @return Collection of constraint violations
     */
    public Set<ConstraintViolation<HyperIoTResource>> getViolations() {
        return violations;
    }

}
