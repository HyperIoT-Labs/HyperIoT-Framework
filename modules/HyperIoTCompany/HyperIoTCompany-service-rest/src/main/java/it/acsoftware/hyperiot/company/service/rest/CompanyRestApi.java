package it.acsoftware.hyperiot.company.service.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseEntityRestApi;
import it.acsoftware.hyperiot.company.api.CompanyApi;
import it.acsoftware.hyperiot.company.model.Company;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * @author Aristide Cittadino Company rest service class. Registered with DOSGi
 * CXF
 */
@SwaggerDefinition(basePath = "/companies", info = @Info(description = "HyperIoT Company API", version = "2.0.0", title = "HyperIoT Company", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")), securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
        @ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/companies", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = CompanyRestApi.class, property = {
        "service.exported.interfaces=it.acsoftware.hyperiot.company.service.rest.CompanyRestApi",
        "service.exported.configs=org.apache.cxf.rs", "org.apache.cxf.rs.address=/companies",
        "service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
        "service.exported.intents=swagger", "service.exported.intents=exceptionmapper"}, immediate = true)
@Path("")
public class CompanyRestApi extends HyperIoTBaseEntityRestApi<Company> {
    private CompanyApi entityService;

    /**
     * Simple service for checking module status
     *
     * @return HyperIoT Company Module work!
     */
    @GET
    @Path("/module/status")
    @ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
    @JsonView(HyperIoTJSONView.Public.class)
    public Response checkModuleWorking() {
        getLog().debug( "In Rest Service GET /hyperiot/company/module/status");
        return Response.ok("Company Module works!").build();
    }

    /**
     * @Return the current entityService
     */
    @Override
    protected HyperIoTBaseEntityApi<Company> getEntityService() {
        getLog().debug( "invoking getEntityService, returning: {}", this.entityService);
        return entityService;
    }

    /**
     * @param entityService: Injecting entityService
     */
    @Reference(service = CompanyApi.class)
    protected void setEntityService(CompanyApi entityService) {
        getLog().debug( "invoking setEntityService, setting: {}", this.entityService);
        this.entityService = entityService;
    }

    /**
     * Service finds an existing Company
     *
     * @param id id from which Company object will retrieved
     * @return Company if found
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/companies/{id}", notes = "Service for finding company", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response findCompany(
            @ApiParam(value = "id from which Company object will retrieve", required = true) @PathParam("id") long id) {
        getLog().debug( "In Rest Service GET /hyperiot/companies/{}", id);
        return this.find(id);
    }

    /**
     * Service saves a new Company
     *
     * @param entity Company object to store in database
     * @return the Company saved
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/companies", notes = "Service for adding a new company entity", httpMethod = "POST", produces = "application/json", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response saveCompany(
            @ApiParam(value = "Company entity which must be saved ", required = true) Company entity) {
        getLog().debug( "In Rest Service POST /hyperiot/companies \n Body: {}", entity);
        return this.save(entity);
    }

    /**
     * Service updates a Company
     *
     * @param entity Company object to update in database
     * @return the Company updated
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/companies", notes = "Service for updating a company entity", httpMethod = "PUT", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 500, message = "Invalid ID supplied")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response updateCompany(
            @ApiParam(value = "Company entity which must be updated ", required = true) Company entity) {
        getLog().debug( "In Rest Service PUT /hyperiot/companies \n Body: {}", entity);
        return this.update(entity);
    }

    /**
     * Service deletes a Company
     *
     * @param id id from which Company object will deleted
     * @return 200 OK if it has been deleted
     */
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/companies/{id}", notes = "Service for deleting a company entity", httpMethod = "DELETE", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 404, message = "Entity not found")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response deleteCompany(
            @ApiParam(value = "The company id which must be deleted", required = true) @PathParam("id") long id) {
        getLog().debug( "In Rest Service DELETE /hyperiot/companies/{}", id);
        return this.remove(id);
    }

    /**
     * Service finds all available company
     *
     * @return list of all available company
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/companies/all", notes = "Service for finding all company entities", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response findAllCompany() {
        getLog().debug( "In Rest Service GET /hyperiot/companies/all");
        return this.findAll();
    }

    /**
     * Service finds all available company
     *
     * @return list of all available company
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/companies", notes = "Service for finding all company entities", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response findAllCompanyPaginated(@QueryParam("delta") Integer delta, @QueryParam("page") Integer page) {
        getLog().debug( "In Rest Service GET /hyperiot/companies/");
        return this.findAll(delta, page);
    }

}
