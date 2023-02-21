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

package it.acsoftware.hyperiot.websocket.channel.role;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilter;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelRole;
import org.osgi.framework.ServiceReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HyperIoTWebSocketChannelRoleManager {

    public static Set<HyperIoTWebSocketChannelRole> newRoleSet(HyperIoTWebSocketChannelRole... roleList) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(roleList)));
    }

    public static Set<HyperIoTWebSocketChannelRole> newRoleSet(Set<HyperIoTWebSocketChannelRole>... roleList) {
        Set<HyperIoTWebSocketChannelRole> mergedSet = new HashSet<>();
        Arrays.asList(roleList).stream().forEach(roleSet -> mergedSet.addAll(roleSet));
        return Collections.unmodifiableSet(mergedSet);
    }

    public static HyperIoTWebSocketChannelRole getHyperIoTWebSocketChannelRole(String roleName) {
        OSGiFilter roleNameFilter = OSGiFilterBuilder.createFilter(HyperIoTConstants.OSGI_WEBSOCKET_CHANNEL_ROLE_NAME, roleName);
        String filter = roleNameFilter.getFilter();
        ServiceReference<HyperIoTWebSocketChannelRole>[] refs = HyperIoTUtil.getServices(HyperIoTWebSocketChannelRole.class, filter);
        //returns the first instance
        if (refs != null && refs.length == 1) {
            return HyperIoTUtil.getBundleContext(HyperIoTWebSocketChannelRoleManager.class).getService(refs[0]);
        } else if (refs.length > 1) {
            throw new HyperIoTRuntimeException("Multiple HyperIoT Web Socket Channel role found with name " + roleName);
        } else {
            throw new HyperIoTRuntimeException("No HyperIoT Web Socket Channel role found with name " + roleName);
        }
    }

    public static String rolesAsCommaSeparatedList(Set<HyperIoTWebSocketChannelRole> roles) {
        StringBuilder sb = new StringBuilder();
        roles.stream().forEach(role -> sb.append(role.getRoleName() + ","));
        String commaSeparatedList = sb.substring(0, sb.length() - 1);
        return commaSeparatedList;
    }

    public static Set<HyperIoTWebSocketChannelRole> fromCommaSeparatedList(String rolesCommaSeparatedList) {
        String[] roles = rolesCommaSeparatedList.split(",");
        Set<HyperIoTWebSocketChannelRole> rolesSet = new HashSet<>();
        Arrays.stream(roles).forEach(roleName -> {
            HyperIoTWebSocketChannelRole r = getHyperIoTWebSocketChannelRole(roleName);
            if (r != null)
                rolesSet.add(r);
        });
        return rolesSet;
    }


}
