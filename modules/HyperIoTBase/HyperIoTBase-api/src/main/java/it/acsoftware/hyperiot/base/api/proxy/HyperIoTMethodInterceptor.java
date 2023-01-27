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

package it.acsoftware.hyperiot.base.api.proxy;

import it.acsoftware.hyperiot.base.api.HyperIoTService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Author Aristide Cittadino
 * @param <A>
 * Interface which maps the concept of HyperIoT method interceptor.
 * An HyperIoTMethodInterceptor is a method which can be invoked before and/or after an HyperIoT service method invocation.
 * It can be used to implement custom annotation for pre-processing or post-processing execution before methods are invoked inside *Api or *SystemApi classes.
 * NOTE: this capability works only on HyperIoTServices methods (no rest, you can use filters for that layer, no persistence)
 */
public interface HyperIoTMethodInterceptor<A extends Annotation> {

}
