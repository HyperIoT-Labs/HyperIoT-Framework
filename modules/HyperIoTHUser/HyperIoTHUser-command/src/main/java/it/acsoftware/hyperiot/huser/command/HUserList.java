package it.acsoftware.hyperiot.huser.command;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;
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
        "osgi.command.function:String=list"
    },
    service = HUserList.class
)
public class HUserList extends HyperIoTBaseCommand {
    /**
     * Using Api so permission are checked
     */
    private HUserApi huserApi;

    @Reference
    public void setHuserApi(HUserApi huserApi) {
        this.huserApi = huserApi;
    }

    public Object list(Integer delta, Integer page) {
        //retrieving user context
        HyperIoTContext context = this.getSecurityContext();
        ShellTable table = new ShellTable();
        table.column("User id");
        table.column("Username");
        HyperIoTPaginableResult result = this.huserApi.findAll((HyperIoTQuery) null, context, delta, page);
        Collection<HUser> users = result.getResults();
        Iterator<HUser> it = users.iterator();
        while (it.hasNext()) {
            HUser u = it.next();
            table.addRow().addContent(u.getId(), u.getUsername());
        }
        table.print(System.out);
        return null;
    }
}
