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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Aristide Cittadino
 * Annotation to use in addition to those the user will define which are related to pre-processing or post-processing
 * of HyperIoT Services methods
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface HyperIoTInterceptorExecutor {
    Class<? extends HyperIoTMethodInterceptor> interceptor();
}
