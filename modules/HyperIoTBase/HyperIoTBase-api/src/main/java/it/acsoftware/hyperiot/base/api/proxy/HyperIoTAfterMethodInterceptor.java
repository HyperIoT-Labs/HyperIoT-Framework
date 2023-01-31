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
 *
 * @param <A> Annotation
 *            Used for defining annotation for post-processing execution after a method invocation
 */
public interface HyperIoTAfterMethodInterceptor<A extends Annotation> extends HyperIoTMethodInterceptor<A> {
    /**
     * @param destination  HyperIoT Service which is going to be invoked
     * @param m            Method
     * @param args         Method arguments
     * @param returnResult Object returned by the method
     * @param annotation   Annotation processed on the method which maps the Interceptor definition
     * @param <S>          HyperIoT Service Type
     */
    <S extends HyperIoTService> void interceptMethod(S destination, Method m, Object[] args, Object returnResult, A annotation);
}
