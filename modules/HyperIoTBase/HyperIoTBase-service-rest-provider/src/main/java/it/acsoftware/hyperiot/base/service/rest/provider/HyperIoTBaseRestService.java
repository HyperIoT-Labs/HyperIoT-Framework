package it.acsoftware.hyperiot.base.service.rest.provider;


import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

/**
 * @author Aristide Cittadino Base rest service class. Registered with DOSGi CXF
 * This service is necessary to create basic contextRoot of the defined application
 */
public class HyperIoTBaseRestService {

    /**
     * Simple service for checking module status
     *
     * @return HyperIoTBase Module works!
     */
    @GET
    public Response checkModuleWorking() {
        return Response.ok("HyperIoTBase Rest Module works!").build();
    }

}

