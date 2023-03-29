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

package  it.acsoftware.hyperiot.sparkmanager.service.rest;



import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonView;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;

import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;

import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiResponse;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiSubmissionRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import  it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi ;
import  it.acsoftware.hyperiot.base.api.HyperIoTBaseApi ;
import it.acsoftware.hyperiot.sparkmanager.api.SparkManagerApi;


/**
 *
 * @author Aristide Cittadino SparkManager rest service class. Registered with DOSGi CXF
 *
 */
@SwaggerDefinition(basePath = "/sparkmanager", info = @Info(description = "HyperIoT SparkManager API", version = "2.0.0", title = "hyperiot SparkManager", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")),securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
		@ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/sparkmanager", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = SparkManagerRestApi.class, property = {
	    "service.exported.interfaces=it.acsoftware.hyperiot.sparkmanager.service.rest.SparkManagerRestApi",
		"service.exported.configs=org.apache.cxf.rs","org.apache.cxf.rs.address=/sparkmanager",
		"service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
		"service.exported.intents=swagger","service.exported.intents=exceptionmapper"
		 }, immediate = true)
@Path("")
public class SparkManagerRestApi extends  HyperIoTBaseRestApi  {
	private SparkManagerApi  service;

	/**
	 * Simple service for checking module status
	 *
	 * @return HyperIoT Role Module work!
	 */
	@GET
	@Path("/module/status")
	@ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
	public Response checkModuleWorking() {
		getLog().debug( "In Rest Service GET /hyperiot/sparkmanager/module/status");
		return Response.ok("SparkManager Module works!").build();
	}


	/**
	 * @return the current service class
	 */
	protected HyperIoTBaseApi getService() {
		getLog().debug( "invoking getService, returning: {}" , this.service);
		return service;
	}

	/**
	 *
	 * @param service: Injecting service class
	 */
	@Reference(service = SparkManagerApi.class)
	protected void setService(SparkManagerApi service) {
		getLog().debug( "invoking setService, setting: {}" , service);
		this.service = service;
	}

	/**
	 * This service gets status of a specific job
	 * @param driverId driver ID
	 * @return Response object
	 */
	@GET
	@Path("/submissions/status/{driverId}")
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/submissions/status/{driverId}", notes = "Service for getting the status of a specific job", httpMethod = "GET",
			produces = "application/json",
			authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
			@ApiResponse(code = 500, message = "Internal error")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response getStatus(
			@ApiParam(value = "Driver ID", required = true) @PathParam("driverId") String driverId) {
		getLog().debug( "In Rest Service GET /hyperiot/sparkmanager/submissions/status/{}", driverId);
		SparkRestApiResponse response = service.getStatus(this.getHyperIoTContext(), driverId);
		return Response.ok(response).build();
	}

	/**
	 * This service kills the specified job
	 * @param driverId Driver ID
	 * @return Response object
	 */
	@POST
	@Path("/submissions/kill/{driverId}")
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/submissions/kill/{driverId}", notes = "Service for killing job", httpMethod = "DELETE",
			produces = "application/json",
			authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
			@ApiResponse(code = 500, message = "Internal error")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response kill(
			@ApiParam(value = "Driver ID", required = true) @PathParam("driverId") String driverId) {
		getLog().debug( "In Rest Service POST /hyperiot/sparkmanager/submissions/kill/{}", driverId);
		SparkRestApiResponse response = service.kill(this.getHyperIoTContext(), driverId);
		return Response.ok(response).build();
	}

	/**
	 * This service submits Spark job
	 * @param data Job config in JSON format
	 * @return Response object
	 */
	@POST
	@Path("/submissions")
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/submissions", notes = "Service for submitting Spark job", httpMethod = "POST",
			consumes = "application/json",
			produces = "application/json",
			authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
			@ApiResponse(code = 500, message = "Internal error")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response submitJob(
			@ApiParam(value = "Job config in JSON format", required = true) SparkRestApiSubmissionRequest data) {
		getLog().debug( "In Rest Service POST /hyperiot/sparkmanager/submissions");
		SparkRestApiResponse response = service.submitJob(this.getHyperIoTContext(), data);
		return Response.ok(response).build();
	}

}
