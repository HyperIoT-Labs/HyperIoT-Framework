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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;

/**
 * @author Aristide Cittadino Implementation class of HyperIoTBaseApi. It is
 * used to implement methods in order to interact with the system layer.
 */
public abstract class HyperIoTBaseServiceImpl extends HyperIoTBaseAbstractService implements HyperIoTBaseApi {
    /**
     * @return The current SystemService
     */
    protected abstract HyperIoTBaseSystemApi getSystemService();

}
