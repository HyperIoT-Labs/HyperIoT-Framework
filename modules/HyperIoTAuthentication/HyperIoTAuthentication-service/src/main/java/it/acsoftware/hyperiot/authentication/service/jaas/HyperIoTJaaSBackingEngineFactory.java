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

package it.acsoftware.hyperiot.authentication.service.jaas;

import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.karaf.jaas.modules.BackingEngineFactory;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(service = BackingEngineFactory.class)
public class HyperIoTJaaSBackingEngineFactory implements BackingEngineFactory {
    @Override
    public String getModuleClass() {
        return HyperIoTJaaSAuthenticationModule.class.getName();
    }

    @Override
    public BackingEngine build(Map<String, ?> options) {
        return new HyperIoTJaaSBackingEngine();
    }
}
