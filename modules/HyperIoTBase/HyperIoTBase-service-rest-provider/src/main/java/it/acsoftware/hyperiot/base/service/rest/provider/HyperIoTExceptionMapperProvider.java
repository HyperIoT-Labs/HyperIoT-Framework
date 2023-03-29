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

package it.acsoftware.hyperiot.base.service.rest.provider;

import it.acsoftware.hyperiot.base.exception.GenericExceptionMapperProvider;
import org.apache.cxf.dosgi.common.api.IntentsProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author Generoso Martello
 */
@Component(property = {"org.apache.cxf.dosgi.IntentName=exceptionmapper"}, immediate = true)
public class HyperIoTExceptionMapperProvider implements IntentsProvider {
    private Logger log = LoggerFactory.getLogger(HyperIoTExceptionMapperProvider.class.getName());
    private GenericExceptionMapperProvider exceptionProvider;
    private List<?> intentArray;

    public HyperIoTExceptionMapperProvider() {
        log.info( "Register HyperIoT Provider ExceptionMapper Intent");
        this.exceptionProvider = new GenericExceptionMapperProvider();
        intentArray = Arrays.asList(this.exceptionProvider);
    }

    @Override
    public List<?> getIntents() {
        return intentArray;
    }

}
