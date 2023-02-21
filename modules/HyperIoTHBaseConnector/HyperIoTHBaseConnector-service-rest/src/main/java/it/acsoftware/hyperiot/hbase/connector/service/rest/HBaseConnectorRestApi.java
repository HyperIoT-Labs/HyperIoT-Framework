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

package  it.acsoftware.hyperiot.hbase.connector.service.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.hbase.connector.api.HBaseConnectorApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Aristide Cittadino HBaseConnector rest service class. Registered with DOSGi CXF
 *
 */
@SwaggerDefinition(basePath = "/hbaseconnectors", info = @Info(description = "HyperIoT HBaseConnector API", version = "2.0.0", title = "hyperiot HBaseConnector", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")),securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
		@ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/hbaseconnectors", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = HBaseConnectorRestApi.class, property = {
	    "service.exported.interfaces=it.acsoftware.hyperiot.hbase.connector.service.rest.HBaseConnectorRestApi",
		"service.exported.configs=org.apache.cxf.rs","org.apache.cxf.rs.address=/hbaseconnectors",
		"service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
		"service.exported.intents=swagger","service.exported.intents=exceptionmapper"
		 }, immediate = true)
@Path("")
public class HBaseConnectorRestApi extends  HyperIoTBaseRestApi  {
	private HBaseConnectorApi  service;

	/**
	 * Check if there's an active connection to the database.
	 * @return Response object
	 */
	@GET
	@Path("/checkConnection")
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/hbaseconnector/checkConnection", notes = "Check HBase client connection", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Internal server error")
	})
	public Response checkConnection() {
		getLog().debug( "In REST Service GET /hyperiot/hbase/checkConnection");
		try {
			service.checkConnection(this.getHyperIoTContext());
			return Response.ok("Connection OK").build();
		} catch (Throwable e) {
			return handleException(e);
		}
	}

	/**
	 * Simple service for checking module status
	 *
	 * @return HyperIoT Role Module work!
	 */
	@GET
	@Path("/module/status")
	@ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
	public Response checkModuleWorking() {
		getLog().debug( "In Rest Service GET /hyperiot/hbaseconnector/module/status");
		return Response.ok("HBaseConnector Module works!").build();
	}

	/**
	 * Service creates table
	 * //@param tableName Table name
	 * //@param columnFamilies Column families
	 * @return Response object
	 */
	@POST
	@Path("/tables")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/hbaseconnector/tables", notes = "Service for creating new table",
			httpMethod = "POST", consumes = "application/x-www-form-urlencoded", produces = "application/json",
			authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
			@ApiResponse(code = 500, message = "Internal error")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response createTable(
			@FormParam("tableName") String tableName,
			@FormParam("columnFamilies") String columnFamilies) {
		getLog().debug( "In Rest Service POST /hyperiot/hbaseconnector/tables");
		getLog().debug( "\t tableName: {}", tableName);
		getLog().debug( "\t columnFamilies: {}", columnFamilies);
		try {
			List<String> columnFamilyList = Arrays.asList(columnFamilies.trim().split(","));
			service.createTable(this.getHyperIoTContext(), tableName, columnFamilyList);
			return Response.ok("Table created").build();
		} catch (Throwable e) {
			return handleException(e);
		}
	}

	/**
	 * Service deletes row from table
	 * @param tableName Table name
	 * @param rowKey Row key
	 * @return Response object
	 */
	@DELETE
	@Path("/tables/rows")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/hbaseconnector/tables/rows", notes = "Service for delete data",
			httpMethod = "DELETE", consumes = "application/x-www-form-urlencoded", produces = "application/json",
			authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
			@ApiResponse(code = 500, message = "Internal error")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response deleteData(
			@FormParam(value = "tableName") String tableName,
			@FormParam(value = "rowKey") String rowKey) {
		getLog().debug( "In Rest Service DELETE hyperiot/hbaseconnector/tables/rows");
		getLog().debug( "\t tableName: {}", tableName);
		getLog().debug( "\t rowKey: {}", rowKey);
		try {
			service.deleteData(this.getHyperIoTContext(), tableName, rowKey);
			return Response.ok().entity("Row deleted").build();
		} catch (Throwable e) {
			return handleException(e);
		}
	}

    /**
     * Service drops table
     *
     * @param tableName Table to drop
     * @return Response object
     */
    @DELETE
    @Path("/tables/{tableName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/hbaseconnector/tables/{tableName}", notes = "Service for deleting a table", httpMethod = "DELETE", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found"),
			@ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response dropTable(
            @ApiParam(value = "The table name which must be dropped", required = true)
			@PathParam("tableName") String tableName) {
        getLog().debug( "In Rest Service DELETE /hyperiot/hbaseconnector/tables/{}", tableName);
        try {
            service.dropTable(this.getHyperIoTContext(), tableName);
            return Response.ok().entity("Table dropped").build();
        } catch (Throwable e) {
            return handleException(e);
        }

    }

    /**
     * Service changes table status, i.e. enabled or disabled
     *
     * @param tableName Table name to enable
     * @return Response object
     */
    @PUT
	@Path("/tables/{tableName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/hbaseconnector/tables/{tableName}",
			notes = "Service for changing table status, i.e. enabled or disabled", httpMethod = "PUT",
			consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 500, message = "Internal Error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response enableTable(
            @ApiParam(value = "Table name whose status must be updated", required = true) @PathParam("tableName") String tableName,
            @ApiParam(value = "True if table must be enabled, false otherwise", required = true) @PathParam("tableName") boolean enabled) {
    	String newStatus = enabled ? "enabled" : "disabled";
        getLog().debug( "In Rest Service PUT hyperiot/hbaseconnector/tables/{}/{}", new Object[] {tableName, enabled});
        try {
        	if (enabled)
            	service.enableTable(this.getHyperIoTContext(), tableName);
        	else
        		service.disableTable(this.getHyperIoTContext(), tableName);
            return Response.ok().entity("Table " + newStatus).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

	/**
	 * @return the current service class
	 */
	@SuppressWarnings("unused")
	protected HyperIoTBaseApi getService() {
		getLog().debug( "invoking getService, returning: " + this.service);
		return service;
	}


	/**
	 * Service inserts data into table
	 * @param tableName Table name
	 * @param rowKey Row key
	 * @param columnFamily Column Family
	 * @param column Column
	 * @param cellValue Value to insert
	 * @return Response object
	 */
	@PUT
	@Path("/tables")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/hbaseconnector/tables", notes = "Service for insert data", httpMethod = "PUT", consumes = "application/x-www-form-urlencoded", produces = "application/json", authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
			@ApiResponse(code = 500, message = "Internal error")})
	@JsonView(HyperIoTJSONView.Public.class)
	public Response insertData(
			@FormParam(value = "tableName") String tableName,
			@FormParam(value = "rowKey") String rowKey,
			@FormParam(value = "columnFamily") String columnFamily,
			@FormParam(value = "column") String column,
			@FormParam(value = "cellValue") String cellValue) {
		getLog().debug( "In Rest Service PUT hyperiot/hbaseconnector/tables");
		getLog().debug( "\t tableName: {}", tableName);
		getLog().debug( "\t rowKey: {}", rowKey);
		getLog().debug( "\t columnFamily: {}", columnFamily);
		getLog().debug( "\t column: {}", column);
		getLog().debug( "\t cellValue: {}", cellValue);
		try {
			service.insertData(this.getHyperIoTContext(), tableName, rowKey, columnFamily, column, cellValue);
			return Response.ok().entity("Value inserted").build();
		} catch (Throwable e) {
			return handleException(e);
		}
	}

	/**
	 *
	 * @param service: Injecting service class
	 */
	@Reference(service = HBaseConnectorApi.class)
	protected void setService(HBaseConnectorApi service) {
		getLog().debug( "invoking setService, setting: " + service);
		this.service = service;
	}

}
