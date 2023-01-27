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

package it.acsoftware.hyperiot.base.security.annotations;

import it.acsoftware.hyperiot.base.api.proxy.HyperIoTInterceptorExecutor;
import it.acsoftware.hyperiot.base.security.annotations.implementation.AllowGenericPermissionInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Aristide Cittadino
 * Annotation to limit method execution only to users which has generic permission on the resource.
 * Generic permission means that the user owns the <resouceName> <ActionId> permission not the specific one
 * which is identified by the primary key of the resource.
 */
@Target({ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@HyperIoTInterceptorExecutor(interceptor = AllowGenericPermissionInterceptor.class)
public @interface AllowGenericPermissions {
    /**
     *
     * @return List of actions to be verified by the permission system
     */
    String[] actions() default {};

    /**
     * Optional: if you want to force the resource name (in case you are managing a non-entity service)
     * @return
     */
    String resourceName() default "";

    /**
     * Optional: if you want to force the resource name given by a parameter name passed to the method
     * @return
     */
    String resourceParamName() default "";
}
