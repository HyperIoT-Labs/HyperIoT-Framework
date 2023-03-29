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

package it.acsoftware.hyperiot.asset.category.service.rest;

import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import it.acsoftware.hyperiot.asset.category.api.AssetCategoryApi;
import it.acsoftware.hyperiot.asset.category.model.AssetCategory;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseEntityRestApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * @author Aristide Cittadino AssetCategory rest service class. Registered with
 * DOSGi CXF
 */
@SwaggerDefinition(basePath = "/assets/categories", info = @Info(description = "HyperIoT AssetCategory API", version = "2.0.0", title = "HyperIoT AssetCategory", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")), securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
        @ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/assets/categories", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = AssetCategoryRestApi.class, property = {
        "service.exported.interfaces=it.acsoftware.hyperiot.asset.category.service.rest.AssetCategoryRestApi",
        "service.exported.configs=org.apache.cxf.rs", "org.apache.cxf.rs.address=/assets/categories",
        "service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
        "service.exported.intents=swagger", "service.exported.intents=exceptionmapper"}, immediate = true)
@Path("")
public class AssetCategoryRestApi extends HyperIoTBaseEntityRestApi<AssetCategory> {
    private AssetCategoryApi entityService;

    /**
     * Simple service for checking module status
     *
     * @return HyperIoT AssetCategory Module work!
     */
    @GET
    @Path("/module/status")
    @ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
    public Response checkModuleWorking() {
        getLog().debug( "In Rest Service GET /hyperiot/assets/categories/module/status");
        return Response.ok("AssetCategory Module works!").build();
    }

    /**
     * @Return the current entityService
     */
    @Override
    protected HyperIoTBaseEntityApi<AssetCategory> getEntityService() {
        getLog().debug( "invoking getEntityService, returning: {}", this.entityService);
        return entityService;
    }

    /**
     * @param entityService: Injecting entityService
     */
    @Reference(service = AssetCategoryApi.class)
    protected void setEntityService(AssetCategoryApi entityService) {
        getLog().debug( "invoking setEntityService, setting: {}" , this.entityService);
        this.entityService = entityService;
    }

    /**
     * Service finds an existing AssetCategory
     *
     * @param id id from which AssetCategory object will retrieved
     * @return AssetCategory if found
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/assets/categories/{id}", notes = "Service for finding assetcategory", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public Response findAssetCategory(
            @ApiParam(value = "id from which assetcategory object will retrieve", required = true) @PathParam("id") long id) {
        getLog().debug( "In Rest Service GET /hyperiot/assets/categories/{}" , id);
        return this.find(id);
    }

    /**
     * Service saves a new AssetCategory
     *
     * @param entity AssetCategory object to store in database
     * @return the AssetCategory saved
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/assets/categories", notes = "Service for adding a new assetcategory entity", httpMethod = "POST", produces = "application/json", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 500, message = "Internal error")})
    public Response saveAssetCategory(
            @ApiParam(value = "AssetCategory entity which must be saved ", required = true) AssetCategory entity) {
        getLog().debug( "In Rest Service POST /hyperiot/assets/categories/ \n Body: {}" , entity);
        return this.save(entity);
    }

    /**
     * Service updates a AssetCategory
     *
     * @param entity AssetCategory object to update in database
     * @return the AssetCategory updated
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/assets/categories", notes = "Service for updating a assetcategory entity", httpMethod = "PUT", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated")})
    public Response updateAssetCategory(
            @ApiParam(value = "AssetCategory entity which must be updated ", required = true) AssetCategory entity) {
        getLog().debug( "In Rest Service PUT /hyperiot/assets/categories/ \n Body: {}" , entity);
        return this.update(entity);
    }

    /**
     * Service deletes a AssetCategory
     *
     * @param id id from which AssetCategory object will deleted
     * @return 200 OK if it has been deleted
     */
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/assets/categories/{id}", notes = "Service for deleting a assetcategory entity", httpMethod = "DELETE", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found")})
    public Response deleteAssetCategory(
            @ApiParam(value = "The assetcategory id which must be deleted", required = true) @PathParam("id") long id) {
        getLog().debug( "In Rest Service DELETE /hyperiot/assets/categories/{}" , id);
        return this.remove(id);
    }

    /**
     * Service finds all available AssetCategory
     *
     * @return list of all available AssetCategory
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/assets/categories/all", notes = "Service for finding all assetcategory entities", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")})
    public Response findAllAssetCategory() {
        getLog().debug( "In Rest Service GET /hyperiot/assets/categories/all");
        return this.findAll();
    }

    /**
     * Service finds all available AssetCategory
     *
     * @return list of all available AssetCategory
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/assets/categories", notes = "Service for finding all assetcategory entities", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")})
    public Response findAllAssetCategoryPaginated(@QueryParam("delta") Integer delta, @QueryParam("page") Integer page) {
        getLog().debug( "In Rest Service GET /hyperiot/assets/categories/");
        return this.findAll(delta, page);
    }

}
