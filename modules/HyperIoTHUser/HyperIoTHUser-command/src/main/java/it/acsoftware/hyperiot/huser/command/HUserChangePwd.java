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

package it.acsoftware.hyperiot.huser.command;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.command.HyperIoTBaseCommand;
import it.acsoftware.hyperiot.huser.api.HUserApi;

import org.apache.karaf.shell.support.table.ShellTable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(
    property = {
        "osgi.command.scope:String=hyperiot-users",
        "osgi.command.function:String=changePwd"
    },
    service = HUserChangePwd.class
)
public class HUserChangePwd extends HyperIoTBaseCommand {
    /**
     * Using Api so permission are checked
     */
    private HUserApi huserApi;

    @Reference
    public void setHuserApi(HUserApi huserApi) {
        this.huserApi = huserApi;
    }

    public Object changePwd(long userId,String current,String password,String passwordConfirm) {
        //retrieving user context
        HyperIoTContext context = this.getSecurityContext();
        ShellTable table = new ShellTable();
        table.column("Result");
        try {
            this.huserApi.changePassword(context, userId, current, password, passwordConfirm);
            table.addRow().addContent("Success!");
        } catch(Throwable t){
            table.addRow().addContent("Failed:"+t.getMessage());
        }
        table.print(System.out);
        return null;
    }
}
