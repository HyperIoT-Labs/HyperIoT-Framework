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

package it.acsoftware.hyperiot.base.service;

import it.acsoftware.hyperiot.base.api.HyperIoTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class HyperIoTAbstractService implements HyperIoTService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * @return the default logger for the class
     */
    protected Logger getLog() {
        return log;
    }

}
