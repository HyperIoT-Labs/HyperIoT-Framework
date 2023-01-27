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

package it.acsoftware.hyperiot.huser.service.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseEntityRestApi;
import it.acsoftware.hyperiot.base.util.HyperIoTErrorConstants;
import it.acsoftware.hyperiot.huser.api.HUserApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.huser.model.HUserPasswordReset;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;


/**
 * @author Aristide Cittadino HUser rest service class. Registered with DOSGi
 * CXF
 */
@SwaggerDefinition(basePath = "/husers", info = @Info(description = "HyperIoT HUser API", version = "2.0.0", title = "HyperIoT HUser", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")), securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
    @ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/husers", produces = "application/json")
@Component(service = HUserRestApi.class, property = {
    "service.exported.interfaces=it.acsoftware.hyperiot.huser.service.rest.HUserRestApi",
    "service.exported.configs=org.apache.cxf.rs", "org.apache.cxf.rs.address=/husers",
    "service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
    "service.exported.intents=exceptionmapper", "service.exported.intents=swagger"}, immediate = true)
@Path("")
public class HUserRestApi extends HyperIoTBaseEntityRestApi<HUser> {
    private HUserApi entityService;

    // Area Config keys
    public static final String HYPERIOT_USER_UPLOAD_FOLDER_PATH = "it.acsoftware.hyperiot.user.uploadFolder.path";
    public static final String HYPERIOT_USER_UPLOAD_FOLDER_MAX_FILE_SIZE = "it.acsoftware.hyperiot.user.uploadFolder.maxFileSize";

    private String assetsFolderPath = "./data/assets/";
    private long assetsFileMaxLength = 1000000;

    /**
     * Simple service for checking module status
     *
     * @return HyperIoT HUser Module works!
     */
    @GET
    @Path("/module/status")
    @ApiOperation(value = "/hyperiot/huser/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
    public Response checkModuleWorking() {
        getLog().debug("In Rest Service GET /hyperiot/huser/module/status: ");
        return Response.ok("HyperIoT HUser Module works!").build();
    }

    /**
     * @return the current entityService
     */
    @Override
    public HUserApi getEntityService() {
        getLog().debug("invoking getEntityService, returning: {}", this.entityService);
        return entityService;
    }

    /**
     * @param entityService Injecting entityService
     */
    @Reference(service = HUserApi.class)
    protected void setEntityService(HUserApi entityService) {
        getLog().debug("invoking setEntityService, setting: {}", this.entityService);
        this.entityService = entityService;
    }

    /**
     * Service finds an existing user
     *
     * @param id id from which user object will retrieve
     * @return User if found
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers/{id}", notes = "Find User", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 403, message = "Not authorized"),
        @ApiResponse(code = 404, message = "Entity not found")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response findHUser(
        @ApiParam(value = "id from which user object will retrieve", required = true) @PathParam("id") long id) {
        getLog().debug("In Rest Service GET /hyperiot/husers/{}", id);
        return this.find(id);
    }

    /**
     * Service saves a new user
     *
     * @param h HUser object to store in database
     * @return User saved
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers", notes = "Save User", httpMethod = "POST", produces = "application/json", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
        @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response saveHUser(@ApiParam(value = "HUser object to store in database", required = true) HUser h) {
        getLog().debug("In Rest Service POST /hyperiot/husers/ \n Body: {}", h);
        return this.save(h);
    }

    /**
     * Register a new user
     *
     * @param h HUser object to store in database
     * @return User saved
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/hyperiot/husers/register", notes = "Save User", httpMethod = "POST", produces = "application/json", consumes = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 422, message = "Not validated"), @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response register(@ApiParam(value = "HUser object to store in database", required = true) HUser h) {
        getLog().debug("In Rest Service POST /hyperiot/husers/register \n Body: {}", h);
        try {
            // forcing active false
            h.setActive(false);
            h.setAdmin(false);
            h.setActivateCode(UUID.randomUUID().toString());
            this.entityService.registerUser(h, this.getHyperIoTContext());
            return Response.ok().entity(h).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

    /**
     * Activate a new user
     *
     * @param email Email of the activating HUser
     * @param code  Code of the activating HUser
     * @return User saved
     */
    @POST
    @Path("/activate")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/hyperiot/husers/activate", notes = "Activate User", httpMethod = "POST", produces = "application/json", consumes = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 422, message = "Not validated"), @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response activate(
        @ApiParam(value = "Email of the activating user", required = true) @QueryParam("email") String email,
        @ApiParam(value = "Code of the activating user", required = true) @QueryParam("code") String code) {
        getLog().debug(
            "In Rest Service POST /hyperiot/husers/activate with code: {}", new Object[]{code, email});
        try {
            this.entityService.activateUser(email, code);
            return Response.ok().build();
        } catch (Throwable t) {
            return handleException(t);
        }
    }

    /**
     * Reset user password request
     *
     * @param email Email of the HUser
     * @return User saved
     */
    @POST
    @Path("/resetPasswordRequest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/hyperiot/husers/resetPasswordRequest", notes = "Reset User Password", httpMethod = "POST", produces = "application/json", consumes = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 404, message = "Entity not found"), @ApiResponse(code = 422, message = "Not validated"),
        @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response resetPasswordRequest(
        @ApiParam(value = "Email of the user", required = true) @QueryParam("email") String email) {
        getLog().debug("In Rest Service POST /hyperiot/husers/resetPassword?email={}", email);
        try {
            this.entityService.passwordResetRequest(email);
            return Response.ok().build();
        } catch (Throwable t) {
            return handleException(t);
        }
    }

    /**
     * Reset user password
     *
     * @param pwdReset Code for resetting user password
     * @return User saved
     */
    @POST
    @Path("/resetPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/hyperiot/husers/resetPassword", notes = "Change User Password", httpMethod = "POST", produces = "application/json", consumes = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 404, message = "Entity not found"), @ApiResponse(code = 422, message = "Not validated"),
        @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response resetPassword(
        @ApiParam(value = "Code for resetting user password", required = true) HUserPasswordReset pwdReset) {
        getLog().debug("In Rest Service POST /hyperiot/husers/changePassword?email={}", pwdReset.getEmail()
            + "  with code:" + pwdReset.getResetCode());
        try {
            if (pwdReset.getPassword() == null || pwdReset.getPasswordConfirm() == null) {
                throw new HyperIoTRuntimeException("it.acsoftware.hyperiot.error.huser.password.reset.not.null");
            }
            this.entityService.resetPassword(pwdReset.getEmail(), pwdReset.getResetCode(), pwdReset.getPassword(),
                pwdReset.getPasswordConfirm());
            return Response.ok().build();
        } catch (Throwable t) {
            return handleException(t);
        }

    }

    /**
     * Service updates a user
     *
     * @param h HUser object to update in database
     * @return User updated
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers", notes = "Update User", httpMethod = "PUT", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
        @ApiResponse(code = 500, message = "Invalid ID supplied")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response updateHUser(@ApiParam(value = "HUser object to update in database", required = true) HUser h) {
        getLog().debug("In Rest Service PUT /hyperiot/husers/ \n Body: {}", h);
        return this.update(h);
    }

    /**
     * Service updates a user
     *
     * @param hUser HUser object to update in database
     * @return User updated
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers/account", notes = "Update User Account", httpMethod = "PUT", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
        @ApiResponse(code = 500, message = "Invalid ID supplied")})
    @Path("/account")
    @JsonView(HyperIoTJSONView.Public.class)
    public Response updateAccountInfo(
        @ApiParam(value = "HUser object to update in database", required = true) HUser hUser) {
        getLog().debug("In Rest Service PUT /hyperiot/husers/ \n Body: {}", hUser);
        try {
            //If user modifies his information, ok if it is another user, should be checked against permission system
            if (this.getHyperIoTContext().getLoggedEntityId() == hUser.getId())
                return Response.ok().entity(this.entityService.updateAccountInfo(this.getHyperIoTContext(), hUser)).build();
            else
                return Response.ok().entity(this.entityService.adminUpdateAccountInfo(this.getHyperIoTContext(), hUser)).build();
        } catch (Throwable e) {
            return this.handleException(e);
        }
    }

    /**
     * Service changes HUser password
     *
     * @param userId          id from which user object will retrieve
     * @param oldPassword     Old HUser password
     * @param newPassword     New HUser password
     * @param passwordConfirm New HUser password confirm
     * @return User updated
     */
    @PUT
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/x-www-form-urlencoded")
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers/password", notes = "Update User", httpMethod = "PUT", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 403, message = "Not authorized"), @ApiResponse(code = 422, message = "Not validated"),
        @ApiResponse(code = 500, message = "Invalid ID supplied")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response changeHUserPassword(
        @ApiParam(value = "HUser id which must be updated ", required = true) @FormParam("userId") long userId,
        @ApiParam(value = "Old HUser Password", required = true) @FormParam("oldPassword") String oldPassword,
        @ApiParam(value = "New HUser Password", required = true) @FormParam("newPassword") String newPassword,
        @ApiParam(value = "New HUser Password confirm", required = true) @FormParam("passwordConfirm") String passwordConfirm) {
        getLog().debug("In Rest Service PUT /hyperiot/husers/password \n Body: {}", userId);
        try {
            if (this.getHyperIoTContext().getLoggedEntityId() == userId) {
                return Response.ok(this.entityService.changePassword(this.getHyperIoTContext(), userId, oldPassword,
                    newPassword, passwordConfirm)).build();
            } else {
                return Response.ok(this.entityService.adminChangePassword(this.getHyperIoTContext(), userId, oldPassword,
                    newPassword, passwordConfirm)).build();
            }

        } catch (Throwable t) {
            return this.handleException(t);
        }
    }

    /**
     * Service deletes an existing user
     *
     * @param id id from which user object will deleted
     * @return 200 OK if it has been deleted
     */
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers/{id}", notes = "Delete User", httpMethod = "DELETE", consumes = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 403, message = "Not authorized"),
        @ApiResponse(code = 404, message = "Entity not found")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response deleteHUser(
        @ApiParam(value = "id from which user object will deleted", required = true) @PathParam("id") long id) {
        getLog().debug("In Rest Service DELETE /hyperiot/husers/{}", id);
        return this.remove(id);
    }

    /**
     * Service finds all available users
     *
     * @return List of all available users
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers/all", notes = "Find all users", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 403, message = "Not authorized"),
        @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response findAllHUser() {
        getLog().debug("In Rest Service GET /hyperiot/husers/ ");
        return this.findAll();
    }

    /**
     * Service finds all available users
     *
     * @return List of all available users
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers", notes = "Find all users", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 403, message = "Not authorized"),
        @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response findAllHUserPaginated(@QueryParam("delta") Integer delta, @QueryParam("page") Integer page) {
        getLog().debug("In Rest Service GET /hyperiot/husers/ ");
        return this.findAll(delta, page);
    }

    /**
     * Set user account image
     *
     * @param id user id
     * @param imageFile Image file
     * @return success or error response
     */
    @POST
    @Path("/{id}/image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers/{id}/image", notes = "Service for setting the user account image", httpMethod = "POST", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @JsonView(HyperIoTJSONView.Public.class)
    public Response setUserImage(@ApiParam(value = "The user id", required = true) @PathParam("id") long id,
                                 @Multipart(value = "image_file") Attachment imageFile) {
        getLog().debug( "In Rest Service POST /hyperiot/user/{}/image", id);
        HUser user = null;
        try {
            user = this.entityService.find(id, this.getHyperIoTContext());
        } catch (Throwable t) {
            return this.handleException(t);
        }
        if (imageFile == null) {
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(new IOException(), Arrays.asList("Missing image file"),
                    HyperIoTErrorConstants.VALIDATION_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        } else {
            try {
                String fileName = "";
                String[] contentDispositionHeader = imageFile.getHeader("Content-Disposition").split(";");
                for (String name : contentDispositionHeader) {
                    if ((name.trim().startsWith("filename"))) {
                        String[] tmp = name.split("=");
                        fileName = tmp[1].trim().replaceAll("\"","");
                    }
                }
                String fileExtension = "";
                if (fileName.indexOf(".") > 0) {
                    fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                }
                if (fileExtension.equals("jpg") || fileExtension.equals("png") || fileExtension.equals("svg") || fileExtension.equals("webp")) {
                    // copy file to assets folder
                    File assetsFolder = new File(assetsFolderPath);
                    if (!assetsFolder.exists()){
                        assetsFolder.mkdirs();
                    }
                    File assetsUserFile = new File(assetsFolder.getAbsolutePath(), String.valueOf(id).concat("_img.").concat(fileExtension));
                    imageFile.transferTo(assetsUserFile);
                    if (assetsUserFile.length() > assetsFileMaxLength) {
                        assetsUserFile.delete();
                        HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(new IOException(), Arrays.asList("File length must be <= " + assetsFileMaxLength),
                                HyperIoTErrorConstants.INTERNAL_ERROR);
                        return Response.status(response.getStatusCode()).entity(response).build();
                    }
                    //update user image path
                   user.setImagePath(assetsUserFile.getPath());
                } else {
                    HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(new IOException(), Arrays.asList("File type not supported."),
                            HyperIoTErrorConstants.INTERNAL_ERROR);
                    return Response.status(response.getStatusCode()).entity(response).build();
                }
            } catch (IOException e) {
                HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e, Arrays.asList("Error while creating output file (JAR)"),
                        HyperIoTErrorConstants.INTERNAL_ERROR);
                return Response.status(response.getStatusCode()).entity(response).build();
            }
        }

        return this.update(user);
    }


    /**
     * Gets the user account image file
     *
     * @param id The user id
     * @return The image file or error
     */

    @GET
    @Path("/{id}/image")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers/{id}/image", notes = "Service to get the user account image", httpMethod = "GET", produces = "application/octet-stream", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    public Response getUserImage(@ApiParam(value = "The user id", required = true) @PathParam("id") long id) {
        getLog().debug( "In Rest Service GET /hyperiot/husers/{}/image", id);
        HUser user = null;
        try {
            user = this.entityService.find(id, this.getHyperIoTContext());
        } catch (Throwable t) {
            return this.handleException(t);
        }
        File assetsUserFile = new File(user.getImagePath());
        if (!assetsUserFile.exists()) {
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(new IOException(), Arrays.asList("Image file not found"),
                    HyperIoTErrorConstants.INTERNAL_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        }
        return Response.ok(assetsUserFile, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + assetsUserFile.getName() + "\"" )
                .build();
    }


    /**
     * Unset the user account image
     *
     * @param id The user id
     * @return
     */
    @DELETE
    @Path("/{id}/image")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/husers/{id}/image", notes = "Service to unset the user account image", httpMethod = "DELETE", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @JsonView(HyperIoTJSONView.Public.class)
    public Response unsetUserImage(@ApiParam(value = "The user id", required = true) @PathParam("id") long id) {
        getLog().debug( "In Rest Service DELETE /hyperiot/husers/{}/image", id);
        HUser user = null;
        try {
            user = this.entityService.find(id, this.getHyperIoTContext());
        } catch (Throwable t) {
            return this.handleException(t);
        }
        File assetsUserFile = new File(user.getImagePath());

        if (assetsUserFile.exists()) {
            assetsUserFile.delete();
        }
        user.setImagePath(null);
        return this.update(user);
    }

    /**
     * Service send mail to user with account deletion code (Used to complete the deregistration process).
     * @return 200 OK if request's processing is successful
     */
    @PUT
    @LoggedIn
    @Path("/account/deletioncode")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/hyperiot/husers/account/deletioncode", notes = "Request user account unregistration. Service send mail to user with account deletion code", httpMethod = "PUT", produces = "application/json", consumes = "application/json" , authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 404, message = "Entity not found"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response requestAccountDeletion() {
        try {
            HyperIoTContext ctx = this.getHyperIoTContext();
            getLog().debug("In Rest Service PUT /hyperiot/husers/account/deletioncode , logged User Id : {} ", ctx );
            this.entityService.deleteAccountRequest(ctx);
            return Response.ok().build();
        } catch (Throwable t) {
            return handleException(t);
        }
    }

    /**
     * Delete user account
     *
     * @return 200 OK if user account will be deleted
     */
    @DELETE
    @Path("/account")
    @LoggedIn
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/hyperiot/husers/account", notes = "Delete user account", httpMethod = "DELETE", produces = "application/json", consumes = "application/x-www-form-urlencoded" , authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 404, message = "Entity not found"), @ApiResponse(code = 422, message = "Not validated"),
            @ApiResponse(code = 500, message = "Internal error")})
    @JsonView(HyperIoTJSONView.Public.class)
    public Response deleteAccount(
            @FormParam("userId") long userId,
            @FormParam("accountDeletionCode") String accountDeletionCode) {
        getLog().debug("In Rest Service DELETE /hyperiot/husers/account/");
        try {
            HyperIoTContext ctx = this.getHyperIoTContext();
            getLog().debug("In deleteAccount : logged user with id : {} , try to delete user with id : {}",  ctx.getLoggedEntityId(), userId);
            this.entityService.deleteAccount(ctx, userId, accountDeletionCode);
            return Response.ok().build();
        } catch (Throwable t) {
            return handleException(t);
        }

    }




}
