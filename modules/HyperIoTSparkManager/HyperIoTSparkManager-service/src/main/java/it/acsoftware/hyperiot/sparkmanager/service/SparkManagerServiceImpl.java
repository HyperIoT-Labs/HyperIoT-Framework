package it.acsoftware.hyperiot.sparkmanager.service;

import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;
import it.acsoftware.hyperiot.sparkmanager.actions.SparkManagerAction;
import it.acsoftware.hyperiot.sparkmanager.api.SparkManagerApi;
import it.acsoftware.hyperiot.sparkmanager.api.SparkManagerSystemApi;
import it.acsoftware.hyperiot.sparkmanager.model.SparkManager;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiResponse;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiSubmissionRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;




/**
 * @author Aristide Cittadino Implementation class of SparkManagerApi interface.
 * It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = SparkManagerApi.class, immediate = true)
public final class SparkManagerServiceImpl extends HyperIoTBaseServiceImpl implements SparkManagerApi {
    public static final String SPARK_MANAGER_RESOURCE_NAME = "it.acsoftware.hyperiot.sparkmanager.model.SparkManager";

    /**
     * Injecting the SparkManagerSystemApi
     */
    private SparkManagerSystemApi systemService;

    /**
     * @return The current SparkManagerSystemApi
     */
    protected SparkManagerSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param sparkManagerSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(SparkManagerSystemApi sparkManagerSystemService) {
        getLog().debug( "invoking setSystemService, setting: {}", systemService);
        this.systemService = sparkManagerSystemService;
    }

    @Override
    @AllowGenericPermissions(actions = SparkManagerAction.Names.GET_JOB_STATUS, resourceName = SPARK_MANAGER_RESOURCE_NAME)
    public SparkRestApiResponse getStatus(HyperIoTContext context, String driverId) {
        return systemService.getStatus(driverId);
    }

    @Override
    @AllowGenericPermissions(actions = SparkManagerAction.Names.KILL_JOB, resourceName = SPARK_MANAGER_RESOURCE_NAME)
    public SparkRestApiResponse kill(HyperIoTContext context, String driverId) {
        return systemService.kill(driverId);
    }

    @Override
    @AllowGenericPermissions(actions = SparkManagerAction.Names.SUBMIT_JOB, resourceName = SPARK_MANAGER_RESOURCE_NAME)
    public SparkRestApiResponse submitJob(HyperIoTContext context, SparkRestApiSubmissionRequest data) {
        return systemService.submitJob(data);
    }

}
