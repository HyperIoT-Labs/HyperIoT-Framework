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

package it.acsoftware.hyperiot.authentication.service.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;
import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * @author Aristide Cittadino Authentication rest service class. Registered with
 * DOSGi CXF
 */
@SwaggerDefinition(basePath = "/authentication", info = @Info(description = "HyperIoT Authentication API", version = "2.0.0", title = "HyperIoT Authentication", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")))
@Api(value = "/authentication", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = AuthenticationRestApi.class, property = {
    "service.exported.interfaces=it.acsoftware.hyperiot.authentication.service.rest.AuthenticationRestApi",
    "service.exported.configs=org.apache.cxf.rs", "org.apache.cxf.rs.address=/authentication",
    "service.exported.intents=jackson", "service.exported.intents=swagger",
    "service.exported.intents=exceptionmapper"})
@Path("")
public class AuthenticationRestApi extends HyperIoTBaseRestApi {
    private AuthenticationApi service;

    @GET
    @Path("/module/status")
    @ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
    public Response checkModuleWorking() {
        getLog().debug("In Rest Service GET /hyperiot/authentication/module/status");
        return Response.ok("Authentication Module works!").build();
    }

    @GET
    @Path("/whoAmI")
    @ApiOperation(value = "/whoAmI", notes = "Simple service for checking current logged user", httpMethod = "GET")
    @ApiResponses(value = {@ApiResponse(code = 401, message = "Not logged in"),
        @ApiResponse(code = 200, message = "Request completed!")})
    public Response whoAmI() {
        HyperIoTContext context = this.getHyperIoTContext();
        if (context != null) {
            return Response.ok(context.getLoggedUsername()).build();
        }
        return Response.status(401).build();
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    @ApiOperation(value = "/authentication/login", response = String.class, notes = "Login service for JWT Token", httpMethod = "POST", produces = "application/json", consumes = "application/x-www-form-urlencoded")
    @ApiResponses(value = {@ApiResponse(code = 401, message = "login failed"),
        @ApiResponse(code = 200, message = "login successed")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        getLog().debug("In Rest Service GET /hyperiot/authentication/login with username: {}", username);
        try {
            HyperIoTAuthenticable user = service.login(username, password);
            if (user != null) {
                String jwtToken = this.service.generateToken(user);
                return Response.ok(jwtToken).build();
            } else {
                return Response.status(401).build();
            }
        } catch (Throwable t) {
            getLog().error(t.getMessage(), t);
            return handleException(t);
        }
    }

    /**
     * @return the current service
     */
    protected HyperIoTBaseApi getService() {
        getLog().debug("invoking getService, returning: {}", this.service);
        return service;
    }

    /**
     * @param service Injecting service
     */
    @Reference(service = AuthenticationApi.class)
    protected void setService(AuthenticationApi service) {
        getLog().debug("invoking setService, setting: {}", service);
        this.service = service;
    }

}
