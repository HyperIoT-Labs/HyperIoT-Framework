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

package  it.acsoftware.hyperiot.zookeeper.connector.service.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.zookeeper.connector.api.ZookeeperConnectorApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



/**
 *
 * @author Aristide Cittadino ZookeeperConnector rest service class. Registered with DOSGi CXF
 *
 */
@SwaggerDefinition(basePath = "/zookeeperConnectors", info = @Info(description = "HyperIoT ZookeeperConnector API", version = "2.0.0", title = "hyperiot ZookeeperConnector", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")),securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
		@ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/zookeeperConnectors", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = ZookeeperConnectorRestApi.class, property = {
	    "service.exported.interfaces=it.acsoftware.hyperiot.zookeeper.connector.service.rest.ZookeeperConnectorRestApi",
		"service.exported.configs=org.apache.cxf.rs","org.apache.cxf.rs.address=/zookeeperConnectors",
		"service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
		"service.exported.intents=swagger","service.exported.intents=exceptionmapper"
		 }, immediate = true)
@Path("")
public class ZookeeperConnectorRestApi extends HyperIoTBaseRestApi {

	private ZookeeperConnectorApi  service;

	/**
	 * Simple service for checking module status
	 *
	 * @return HyperIoT Role Module work!
	 */
	@GET
	@Path("/module/status")
	@ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
	public Response checkModuleWorking() {
		getLog().debug( "In Rest Service GET /hyperiot/zookeeperConnectors/module/status");
		return Response.ok("ZookeeperConnector Module works!").build();
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
	@Reference(service = ZookeeperConnectorApi.class)
	protected void setService(ZookeeperConnectorApi service) {
		getLog().debug( "invoking setService, setting: {}" , service);
		this.service = service;
	}

	/**
	 * Service tells if connector is Zookeeper leader on given zkNode path
	 *
	 * @return Response object
	 */
	@GET
	@Path("/isLeader/{mutexPath}")
	@Produces(MediaType.TEXT_PLAIN)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/zookeeperConnectors/isLeader/{mutexPath}",
			notes = "Service for telling if connector is Zookeeper leader", httpMethod = "GET", produces = "text/plain",
			authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response isLeader(
			@ApiParam(value = "ZkNode path", required = true) @QueryParam("mutexPath") String mutexPath) {
		getLog().debug( "In Rest Service GET /hyperiot/zookeeperConnectors/isLeader?mutexPath={}", mutexPath);
		boolean isLeader = service.isLeader(getHyperIoTContext(), mutexPath);
		return Response.ok(isLeader).build();
	}

}
