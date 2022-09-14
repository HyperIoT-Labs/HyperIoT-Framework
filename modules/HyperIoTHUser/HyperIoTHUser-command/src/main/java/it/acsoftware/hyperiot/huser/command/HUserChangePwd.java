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
