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

package  it.acsoftware.hyperiot.bundle.listener.service.rest;



import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonView;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;

import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import  it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi ;
import  it.acsoftware.hyperiot.base.api.HyperIoTBaseApi ;
import it.acsoftware.hyperiot.bundle.listener.api.BundleListenerApi;


/**
 *
 * @author Aristide Cittadino BundleListener rest service class. Registered with DOSGi CXF
 *
 */
@SwaggerDefinition(basePath = "/bundles", info = @Info(description = "HyperIoT BundleListener API", version = "2.0.0", title = "hyperiot BundleListener", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")),securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
		@ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/bundles", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = BundleListenerRestApi.class, property = {
	    "service.exported.interfaces=it.acsoftware.hyperiot.bundle.listener.service.rest.BundleListenerRestApi",
		"service.exported.configs=org.apache.cxf.rs","org.apache.cxf.rs.address=/bundles",
		"service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
		"service.exported.intents=swagger","service.exported.intents=exceptionmapper"
		 }, immediate = true)
@Path("")
public class BundleListenerRestApi extends  HyperIoTBaseRestApi  {
	private BundleListenerApi  service;

	/**
	 * Simple service for checking module status
	 *
	 * @return HyperIoT Role Module work!
	 */
	@GET
	@Path("/module/status")
	@ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
	public Response checkModuleWorking() {
		getLog().debug( "In Rest Service GET /hyperiot/bundlelistener/module/status");
		return Response.ok("BundleListener Module works!").build();
	}


	/**
	 * @Return the current service class
	 */
	protected HyperIoTBaseApi getService() {
		getLog().debug( "invoking getService, returning: {}" , this.service);
		return service;
	}

	/**
	 *
	 * @param service: Injecting service class
	 */
	@Reference(service = BundleListenerApi.class)
	protected void setService(BundleListenerApi service) {
		getLog().debug( "invoking setService, setting: {}" , service);
		this.service = service;
	}

	/**
	 * Service finds all bundles
	 *
	 * @return list of all bundles
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/bundles", notes = "Service for listing bundles", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Internal error")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response findAll() {
		getLog().debug( "In Rest Service GET /hyperiot/bundles/");
		return Response.ok(service.list()).build();
	}

	/**
	 * Service find bundle by id
	 *
	 * @return the bundle item
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/bundles/{id}", notes = "Service for getting bundle data and status", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Internal error")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response find(@ApiParam(value = "id of the bundle", required = true) @PathParam("id") String id) {
		getLog().debug( "In Rest Service GET /hyperiot/bundles/{}", id);
		return Response.ok(service.get(id)).build();
	}

}
