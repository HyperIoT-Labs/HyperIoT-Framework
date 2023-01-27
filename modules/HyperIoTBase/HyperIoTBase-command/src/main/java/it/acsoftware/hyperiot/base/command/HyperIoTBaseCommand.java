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

package it.acsoftware.hyperiot.base.command;

import it.acsoftware.hyperiot.base.model.authentication.context.HyperIoTContextFactory;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;

import javax.security.auth.Subject;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

/**
 * Author Aristide Cittadino
 * This class exposes utility methods for classes that must implement karaf command
 */
public class HyperIoTBaseCommand {

    /**
     * Return the (HyperIoTContext) security context
     * based on the current logged user.
     *
     * @return Current User Security Context
     */
    protected HyperIoTContext getSecurityContext() {
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject(acc);
        Set<Principal> principals = subject.getPrincipals();
        if (principals.size() > 0) {
            return HyperIoTContextFactory.createBasicContext(principals);
        }
        return null;
    }
}
