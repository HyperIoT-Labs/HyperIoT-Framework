package it.acsoftware.hyperiot.huser.command;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.command.HyperIoTBaseCommand;
import it.acsoftware.hyperiot.huser.api.HUserApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import org.apache.karaf.shell.support.table.ShellTable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;
import java.util.Iterator;

@Component(
    property = {
        "osgi.command.scope:String=hyperiot-users",
        "osgi.command.function:String=searchByUsername"
    },
    service = HUserSearchByUsername.class
)
public class HUserSearchByUsername extends HyperIoTBaseCommand {
    /**
     * Using Api so permission are checked
     */
    private HUserApi huserApi;

    @Reference
    public void setHuserApi(HUserApi huserApi) {
        this.huserApi = huserApi;
    }

    public Object searchByUsername(String username) {
        //retrieving user context
        HyperIoTContext context = this.getSecurityContext();
        ShellTable table = new ShellTable();
        table.column("User id");
        table.column("Username");
        try {
            HUser user = this.huserApi.findUserByUsername(username);
            table.addRow().addContent(user.getId(), user.getUsername());
        } catch (Throwable t) {
            table.addRow().addContent("Failed:" + t.getMessage());
        }
        table.print(System.out);
        return null;
    }
}
