package  it.acsoftware.hyperiot.shared.entity.example.service.rest;



import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonView;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;

import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseEntityRestApi ;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi ;
import it.acsoftware.hyperiot.shared.entity.example.api.SharedEntityExampleApi;

import it.acsoftware.hyperiot.shared.entity.example.model.SharedEntityExample;


/**
 *
 * @author Aristide Cittadino SharedEntityExample rest service class. Registered with DOSGi CXF
 *
 */
@SwaggerDefinition(basePath = "/sharedentityexamples", info = @Info(description = "HyperIoT SharedEntityExample API", version = "2.0.0", title = "hyperiot SharedEntityExample", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")),securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
		@ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/sharedentityexamples", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = SharedEntityExampleRestApi.class, property = {
	    "service.exported.interfaces=it.acsoftware.hyperiot.shared.entity.example.service.rest.SharedEntityExampleRestApi",
		"service.exported.configs=org.apache.cxf.rs","org.apache.cxf.rs.address=/sharedentityexamples",
		"service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
		"service.exported.intents=swagger","service.exported.intents=exceptionmapper"
		 }, immediate = true)
@Path("")
public class SharedEntityExampleRestApi extends HyperIoTBaseEntityRestApi<SharedEntityExample>  {
	private SharedEntityExampleApi entityService ;

	/**
	 * Simple service for checking module status
	 *
	 * @return HyperIoT Role Module work!
	 */
	@GET
	@Path("/module/status")
	@ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
	public Response checkModuleWorking() {
		getLog().debug( "In Rest Service GET /hyperiot/sharedentityexample/module/status");
		return Response.ok("SharedEntityExample Module works!").build();
	}

	/**
	 * @Return the current entityService
	 */
	@Override
	protected HyperIoTBaseEntityApi<SharedEntityExample> getEntityService() {
		getLog().debug( "invoking getEntityService, returning: {}" , this.entityService);
		return entityService;
	}

	/**
	 *
	 * @param entityService: Injecting entityService
	 */
	@Reference(service = SharedEntityExampleApi.class)
	protected void setEntityService(SharedEntityExampleApi entityService) {
		getLog().debug( "invoking setEntityService, setting: {}" , this.entityService);
		this.entityService = entityService;
	}

	/**
	 * Service finds an existing %- projectSuffixUC
	 *
	 * @param id id from which %- projectSuffixUC  object will retrieved
	 * @return  SharedEntityExample if found
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/sharedentityexamples/{id}", notes = "Service for finding sharedentityexample", httpMethod = "GET", produces = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Entity not found") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response findSharedEntityExample(@PathParam("id") long id) {
		getLog().debug( "In Rest Service GET /hyperiot/sharedentityexamples/{}" , id);
		return this.find(id);
	}

	/**
	 * Service saves a new SharedEntityExample
	 *
	 * @param entity SharedEntityExample object to store in database
	 * @return the SharedEntityExample saved
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/sharedentityexamples", notes = "Service for adding a new sharedentityexample entity", httpMethod = "POST", produces = "application/json", consumes = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 422, message = "Not validated"),
			@ApiResponse(code = 500, message = "Internal error") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response saveSharedEntityExample(
		@ApiParam(value = "SharedEntityExample entity which must be saved ", required = true) SharedEntityExample entity) {
		getLog().debug( "In Rest Service POST /hyperiot/sharedentityexamples \n Body: {}" , entity);
		return this.save(entity);
	}

	/**
	 * Service updates a SharedEntityExample
	 *
	 * @param entity SharedEntityExample object to update in database
	 * @return the SharedEntityExample updated
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/sharedentityexamples", notes = "Service for updating a sharedentityexample entity", httpMethod = "PUT", consumes = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 422, message = "Not validated"),
			@ApiResponse(code = 500, message = "Invalid ID supplied") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response updateSharedEntityExample(
		@ApiParam(value = "SharedEntityExample entity which must be updated ", required = true)SharedEntityExample entity) {
		getLog().debug( "In Rest Service PUT /hyperiot/sharedentityexamples \n Body: {}" , entity);
		return this.update(entity);
	}

	/**
	 * Service deletes a SharedEntityExample
	 *
	 * @param id id from which SharedEntityExample object will deleted
	 * @return 200 OK if it has been deleted
	 */
	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/sharedentityexamples/{id}", notes = "Service for deleting a sharedentityexample entity", httpMethod = "DELETE", consumes = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Entity not found") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response deleteSharedEntityExample(
		@ApiParam(value = "The sharedentityexample id which must be deleted", required = true) @PathParam("id") long id) {
		getLog().debug( "In Rest Service DELETE /hyperiot/sharedentityexamples/{}" , id);
		return this.remove(id);
	}

	/**
	 * Service finds all available sharedentityexample
	 *
	 * @return list of all available sharedentityexample
	 */
	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/sharedentityexamples/all", notes = "Service for finding all sharedentityexample entities", httpMethod = "GET", produces = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Internal error") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response findAllSharedEntityExample() {
		getLog().debug( "In Rest Service GET /hyperiot/sharedentityexamples/");
		return this.findAll();
	}

	/**
	 * Service finds all available sharedentityexample
	 *
	 * @return list of all available sharedentityexample
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/sharedentityexamples", notes = "Service for finding all sharedentityexample entities", httpMethod = "GET", produces = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Internal error") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response findAllSharedEntityExamplePaginated(@QueryParam("delta") Integer delta,@QueryParam("page") Integer page) {
		getLog().debug( "In Rest Service GET /hyperiot/sharedentityexamples/");
		return this.findAll(delta,page);
	}

}
