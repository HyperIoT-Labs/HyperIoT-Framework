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

package it.acsoftware.hyperiot.role.service.rest;

import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import it.acsoftware.hyperiot.base.api.HyperIoTRestAction;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseEntityRestApi;
import it.acsoftware.hyperiot.role.api.RoleApi;
import it.acsoftware.hyperiot.role.model.Role;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * @author Aristide Cittadino Role rest service class. Registered with DOSGi CXF
 */
@SwaggerDefinition(basePath = "/roles", info = @Info(description = "HyperIoT Role API", version = "2.0.0", title = "HyperIoT Role", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")), securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
        @ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/roles", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = RoleRestApi.class, property = {
        "service.exported.interfaces=it.acsoftware.hyperiot.role.service.rest.RoleRestApi",
        "service.exported.configs=org.apache.cxf.rs", "org.apache.cxf.rs.address=/roles",
        "service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
        "service.exported.intents=exceptionmapper", "service.exported.intents=swagger"}, immediate = true)
@Path("")
public class RoleRestApi extends HyperIoTBaseEntityRestApi<Role> {
    private RoleApi entityService;

    /**
     * Simple service for checking module status
     *
     * @return HyperIoT Role Module work!
     */
    @GET
    @Path("/module/status")
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles/module/status", notes = "Simple service for checking module status", httpMethod = "GET", authorizations = @Authorization("jwt-auth"))
    public Response checkModuleWorking() {
        getLog().debug( "In Rest Service GET /hyperiot/roles/module/status");
        return Response.ok("HyperIoT Role Module works!").build();
    }

    /**
     * @Return the current entityService
     */
    @Override
    public HyperIoTBaseEntityApi<Role> getEntityService() {
        getLog().debug( "invoking getEntityService, returning: {}", this.entityService);
        return entityService;
    }

    /**
     * @param entityService: Injecting entityService
     */
    @Reference(service = RoleApi.class)
    protected void setEntityService(RoleApi entityService) {
        getLog().debug( "invoking setEntityService, setting: {}", this.entityService);
        this.entityService = entityService;
    }

    /**
     * Service finds an existing role
     *
     * @param id id from which role object will retrieve
     * @return Role if found
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles/{id}", notes = "Find role", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public Response findRole(
            @ApiParam(value = "id from which role object will retrieve", required = true) @PathParam("id") long id) {
        getLog().debug( "In Rest Service GET /hyperiot/roles/{}", id);
        return this.find(id);
    }

    /**
     * Service saves a new role
     *
     * @param r Role object to store in database
     * @return the role saved
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles", notes = "Save role", httpMethod = "POST", produces = "application/json", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 500, message = "Internal error")})
    public Response saveRole(@ApiParam(value = "Role object to store in database", required = true) Role r) {
        getLog().debug( "In Rest Service POST /hyperiot/roles/ \n Body: {}", r);
        return this.save(r);
    }

    /**
     * Service updates a role
     *
     * @param r Role object to update in database
     * @return the role updated
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles", notes = "Update role", httpMethod = "PUT", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 500, message = "Invalid ID supplied")})
    public Response updateRole(@ApiParam(value = "Role object to update in database", required = true) Role r) {
        getLog().debug( "In Rest Service PUT /hyperiot/roles/ \n Body: {}", r);
        return this.update(r);
    }

    /**
     * Service deletes a role
     *
     * @param id id from which role object will deleted
     * @return 200 OK if it has been deleted
     */
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles/{id}", notes = "Delete role", httpMethod = "DELETE", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public Response deleteRole(
            @ApiParam(value = "id from which role object will deleted", required = true) @PathParam("id") long id) {
        getLog().debug( "In Rest Service DELETE /hyperiot/roles/{}", id);
        return this.remove(id);
    }

    /**
     * Service finds all available roles
     *
     * @return list of all available roles
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles/all", notes = "Find all roles", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")})
    public Response findAllRoles() {
        getLog().debug( "In Rest Service GET /hyperiot/roles/");
        return this.findAll();
    }

    /**
     * Service finds all available roles
     *
     * @return list of all available roles
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles", notes = "Find all roles", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")})
    public Response findAllRolesPaginated(@QueryParam("delta") Integer delta, @QueryParam("page") Integer page) {
        getLog().debug( "In Rest Service GET /hyperiot/roles/");
        return this.findAll(delta, page);
    }

    /**
     * Service finds all roles by a specific user
     *
     * @param userId id from which user object will retrieve
     * @return list of all roles for a specific user
     */
    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles/user/{userId}", notes = "Find all roles by user id", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public Response findAllUserRoles(
            @ApiParam(value = "id from which user object will retrieve", required = true) @PathParam("userId") long userId) {
        getLog().debug( "In Rest Service GET /hyperiot/roles/user/{}", userId);
        return this.createResponse(new HyperIoTRestAction() {
            @Override
            public Response doAction() {
                return Response.ok(entityService.getUserRoles(userId, getHyperIoTContext())).build();
            }
        });

    }

    /**
     * Service assigns a new user role
     *
     * @param roleId id from which role object will saved
     * @param userId id from which user object will saved
     * @return the new assigned user role
     */
    @POST
    @Path("/{roleId}/user/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/hyperiot/roles/{roleId}/user/{userId}", notes = "Save role by user id", httpMethod = "POST", consumes = "application/json", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public Response saveUserRole(
            @ApiParam(value = "id from which role object will saved", required = true) @PathParam("roleId") long roleId,
            @ApiParam(value = "id from which user object will saved", required = true) @PathParam("userId") long userId) {
        getLog().debug( "In Rest Service POST /hyperiot/roles/{}/user/{}", new Object[]{roleId, userId});
        return this.createResponse(new HyperIoTRestAction() {
            @Override
            public Response doAction() {
                return Response.ok(entityService.saveUserRole(userId, roleId, getHyperIoTContext())).build();
            }
        });

    }

    /**
     * Service deletes a user role
     *
     * @param roleId id from which role object will deleted
     * @param userId id from which user object will deleted
     * @return a message that the role has been deleted by the user
     */
    @DELETE
    @Path("/{roleId}/user/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/roles/{roleId}/user/{userId}", notes = "Delete role by user id", httpMethod = "DELETE", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public Response deleteUserRole(
            @ApiParam(value = "id from which role object will deleted", required = true) @PathParam("roleId") long roleId,
            @ApiParam(value = "id from which user object will deleted", required = true) @PathParam("userId") long userId) {
        getLog().debug( "In Rest Service DELETE /hyperiot/roles/{}/user/{}", new Object[]{roleId, userId});
        return this.createResponse(new HyperIoTRestAction() {
            @Override
            public Response doAction() {
                return Response.ok(entityService.removeUserRole(userId, roleId, getHyperIoTContext())).build();
            }
        });
    }
}
