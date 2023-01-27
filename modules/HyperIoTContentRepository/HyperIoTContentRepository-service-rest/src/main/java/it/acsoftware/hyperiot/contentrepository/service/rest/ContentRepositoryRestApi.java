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

package  it.acsoftware.hyperiot.contentrepository.service.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import it.acsoftware.hyperiot.base.exception.HyperIoTValidationException;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.contentrepository.api.ContentRepositoryApi;
import it.acsoftware.hyperiot.contentrepository.model.DocumentResourceType;
import it.acsoftware.hyperiot.contentrepository.model.HyperIoTDocumentNode;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.*;


/**
 * 
 * @author Aristide Cittadino ContentRepository rest service class. Registered with DOSGi CXF
 * 
 */
@SwaggerDefinition(basePath = "/contentrepositorys", info = @Info(description = "HyperIoT ContentRepository API", version = "2.0.0", title = "hyperiot ContentRepository", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")),securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
		@ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/contentrepositorys", produces = "application/json")
@Produces(MediaType.APPLICATION_JSON)
@Component(service = ContentRepositoryRestApi.class, property = { 
	    "service.exported.interfaces=it.acsoftware.hyperiot.contentrepository.service.rest.ContentRepositoryRestApi",
		"service.exported.configs=org.apache.cxf.rs","org.apache.cxf.rs.address=/contentrepositorys",
		"service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
		"service.exported.intents=swagger","service.exported.intents=exceptionmapper"
		 }, immediate = true)
@Path("")
public class ContentRepositoryRestApi extends HyperIoTBaseRestApi {

	private ContentRepositoryApi contentRepositoryApi ;

	/**
	 * Simple service for checking module status
	 * 
	 * @return HyperIoT Role Module work!
	 */
	@GET
	@Path("/module/status")
	@ApiOperation(value = "/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
	public Response checkModuleWorking() {
		getLog().debug("In Rest Service GET /hyperiot/contentrepository/module/status");
		return Response.ok("ContentRepository Module works!").build();
	}

	/**
	 * @return the current contentRepositoryApi
	 */
	protected ContentRepositoryApi getContentRepositoryApi() {
		getLog().debug("invoking getContentRepositoryApi, returning: {}" , this.contentRepositoryApi);
		return contentRepositoryApi;
	}

	/**
	 * 
	 * @param contentRepositoryApi: Injecting contentRepositoryApi
	 */
	@Reference(service = ContentRepositoryApi.class)
	protected void setContentRepositoryApi(ContentRepositoryApi contentRepositoryApi) {
		getLog().debug("invoking setContentRepositoryApi, setting: {}" , this.contentRepositoryApi);
		this.contentRepositoryApi = contentRepositoryApi;
	}

	/**
	 *  Service finds the document's tree view relative to all subfolder related to the resource. (Resource is identified by the pair resourceName-resourceId)
	 *
	 * @param resourceName the entity class name to which documents are related
	 * @param resourceId the id of the entity to which documents are related
	 * @return the treeview document's of the resource
	 * (The tree view is represent as a list of HyperIoTDocumentNode object)
	 */
	@GET
	@Path("/treeview/resourcename/{resourceName}/resourceid/{resourceId}")
	@Produces(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/contentrepositorys/treeview/resourcename/{resourceName}/resourceid/{resourceId}", notes = "Service for find Service the document's tree view relative to all subfolder related to the resource", httpMethod = "GET", produces = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Internal error") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response findResourceDocumentTreeView(
			@ApiParam(value = "Entity resource name", required = true) @PathParam("resourceName") String resourceName,
			@ApiParam(value = "Entity resource id", required = true) @PathParam("resourceId") long resourceId) {
		getLog().debug("In Rest Service GET /hyperiot/contentrepositorys/treeview/resourcename/{}/resourceid/{}", resourceName, resourceId);
		try {
			List<HyperIoTDocumentNode> documentsTreeView = this.contentRepositoryApi.getResourceDocumentsTreeView(this.getHyperIoTContext(), resourceName, resourceId);
			return Response.ok(documentsTreeView).build();
		}catch (Throwable exc){
			return this.handleException(exc);
		}
	}

	/**
	 * Service upload document's file from resource's subfolder. (Resource is identified by the pair resourceName-resourceId)
	 *
	 * @param resourceName the entity class name to which documents are related
	 * @param resourceId the id of the entity to which documents are related
	 * @param foldersPath the absolute path of the folder in which the file will be save.  (absolute respect the jcr repository)
	 * @param uploadedFile the file that will be upload
	 * @return 200 OK if the upload is succesful.
	 */
	@POST
	@Path("/upload/resourcename/{resourceName}/resourceid/{resourceId}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/contentrepositorys/upload/resourcename/{resourceName}/resourceid/{resourceId}", notes = "Service for uploading a document's file in resource's subfolder", httpMethod = "POST", produces = "application/json",consumes = "multipart/form-data",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Entity not found") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response uploadFileOnResourceDocumentFolder(@Multipart(value = "file") Attachment uploadedFile,
												@QueryParam("foldersPath") String foldersPath,
												@ApiParam(value = "Entity resource name", required = true) @PathParam("resourceName") String resourceName,
												@ApiParam(value = "Entity resource id", required = true) @PathParam("resourceId") long resourceId) {
		getLog().debug("In Rest Service POST /hyperiot/contentrepositorys/upload/resourcename/{}/resourceid/{}?foldersPath={}", resourceName, resourceId, foldersPath);
		try {
			if( foldersPath == null || foldersPath.isEmpty() ){
				getLog().debug("FolderPath parameter : {}  , not match regex ", foldersPath);
				throw new HyperIoTValidationException(new HashSet<>());
			}
			getLog().debug("In uploadFileOnResourceDocumentFolder, file name is {} ", uploadedFile.getContentDisposition().getParameter("filename"));
			InputStream stream = uploadedFile.getObject(InputStream.class);
			String filename = uploadedFile.getContentDisposition().getParameter("filename");
			HyperIoTDocumentNode nodePath = buildPathFromCommaSeparatedFolderName(foldersPath);
			this.contentRepositoryApi.addFileToSubFolder(this.getHyperIoTContext(), resourceName, resourceId, nodePath, stream, filename);
			return Response.ok().build();
		}catch (Throwable exc){
			return this.handleException(exc);
		}
	}

	private HyperIoTDocumentNode buildPathFromCommaSeparatedFolderName(String folderPath){
		ArrayList<String> folderList = new ArrayList<>(Arrays.asList(folderPath.trim().split(",")));
		HyperIoTDocumentNode node = null;
		HyperIoTDocumentNode parent = null;
		for(String folder : folderList){
			HyperIoTDocumentNode folderNode = new HyperIoTDocumentNode();
			folderNode.setDocumentResourceName(folder);
			folderNode.setDocumentResourceType(DocumentResourceType.FOLDER_TYPE);
			if(node == null){
				node = folderNode;
				parent = folderNode;
			}else{
				parent.getDocumentResourceChild().add(folderNode);
				parent = folderNode;
			}
		}
		return node;
	}

	/**
	 * Service download document's file from resource's subfolder. (Resource is identified by the pair resourceName-resourceId)
	 *
	 * @param resourceName the entity class name to which documents are related
	 * @param resourceId the id of the entity to which documents are related
	 * @param nodePath the absolute path of the folder in which the file will be save. (absolute respect the jcr repository)
	 * @param fileName the name of the file that will be download
	 * @return the file requested
	 */
	@GET
	@Path("/download/resourcename/{resourceName}/resourceid/{resourceId}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/contentrepositorys/download/resourcename/{resourceName}/resourceid/{resourceId}", notes = "Service for download document's file from resource's subfolder. ", httpMethod = "GET", produces = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Internal error") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response downloadDocumentFileFromResourceFolder(
			@ApiParam(value = "Entity resource name", required = true) @PathParam("resourceName") String resourceName,
			@ApiParam(value = "Entity resource id", required = true) @PathParam("resourceId") long resourceId ,
			@QueryParam("filename") String fileName,
			HyperIoTDocumentNode nodePath ){
		getLog().debug("In Rest Service GET /hyperiot/contentrepositorys/download/resourcename/{}/resourceid/{}?filename={}", resourceName, resourceId,  fileName);
		try{
			InputStream stream = this.contentRepositoryApi.getResourceFile(this.getHyperIoTContext(), resourceName, resourceId, nodePath, fileName);
			return Response
					.ok(stream)
					.header("Content-Disposition", String.format("attachment; filename =\"%s\"", fileName))
					.build();
		}catch (Throwable exc){
			//Serialize error response as stream such that match ApplicatoinOctetStream content type.
			Response errorResponse = this.handleException(exc);
			HyperIoTBaseError errorInfo = (HyperIoTBaseError) errorResponse.getEntity();
			StreamingOutput jsonResponse = ((outputStream) -> {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
				try(JsonGenerator jsonGenerator = mapper.getFactory().createGenerator(outputStream)) {
					jsonGenerator.writeObject(mapper.readValue(mapper.writerWithView(HyperIoTJSONView.Public.class).writeValueAsString(errorInfo), HyperIoTBaseError.class));
					jsonGenerator.flush();
				}});
			return Response
					.status(errorResponse.getStatus())
					.entity(jsonResponse).build();
		}
	}

	/**
	 * Service add a folder to  resource's document repository. (Resource is identified by the pair resourceName-resourceId)
	 *
	 * @param resourceName the entity class name to which documents are related
	 * @param resourceId the id of the entity to which documents are related
	 * @param pathToFolder the absolute path of the folder in which the folder will be add. (absolute respect the jcr repository)
	 * @param folderName the name of the folder that will be add.
	 * @return 200 OK if the folder will be add.
	 */
	@POST
	@Path("/resourcename/{resourceName}/resourceid/{resourceId}/folder/{folderName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/contentrepositorys/resourcename/{resourceName}/resourceid/{resourceId}/folder/{folderName}", notes = "Service for add a folder to  resource's document repository.", httpMethod = "POST", consumes = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Entity not found") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response addFolderToResourceDocumentsRepository(
			@ApiParam(value = "Entity resource name", required = true) @PathParam("resourceName") String resourceName,
			@ApiParam(value = "Entity resource id", required = true) @PathParam("resourceId") long resourceId,
			HyperIoTDocumentNode pathToFolder,
			@ApiParam(value = "Folder name", required = true) @PathParam("folderName") String folderName ){
		getLog().debug("In Rest Service POST /hyperiot/contentrepositorys/resourcename/{}/resourceid/{}/folder/{}", resourceName, resourceId, folderName);
		try{
			this.contentRepositoryApi.addFolderToResourceDocumentsRepository(this.getHyperIoTContext(), resourceName, resourceId, pathToFolder, folderName);
			return Response.ok().build();
		}catch (Throwable exc){
			return this.handleException(exc);
		}

	}

	/**
	 * Service rename resource's document node name. (Resource is identified by the pair resourceName-resourceId)
	 *
	 * @param resourceName the entity class name to which documents are related
	 * @param resourceId the id of the entity to which documents are related
	 * @param pathToFolder the absolute path of the folder in which the document will be retrieve. (absolute respect the jcr repository)
	 * @param oldNodeName the current name of the document node that will be update
	 * @param newNodeName the new name of the document node that will be update
	 * @return 200 OK if the folder will be add.
	 */
	@PUT
	@Path("/resourcename/{resourceName}/resourceid/{resourceId}/documentnode/{oldNodeName}/{newNodeName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/contentrepositorys/resourcename/{resourceName}/resourceid/{resourceId}/documentnode/{oldNodeName}/{newNodeName}", notes = "Service for update the name of a resource's document node .", httpMethod = "PUT", consumes = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Entity not found") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response updateResourceDocumentNodeName(
			@ApiParam(value = "Entity resource name", required = true) @PathParam("resourceName") String resourceName,
			@ApiParam(value = "Entity resource id", required = true) @PathParam("resourceId") long resourceId,
			HyperIoTDocumentNode pathToFolder,
			@ApiParam(value = "Old document node name", required = true) @PathParam("oldNodeName") String oldNodeName,
			@ApiParam(value = "New document node name", required = true) @PathParam("newNodeName") String newNodeName) {
		getLog().debug("In Rest Service PUT /hyperiot/contentrepositorys/resourcename/{}/resourceid/{}/documentnode/{}/{}", resourceName, resourceId, oldNodeName, newNodeName);
		try{
			this.contentRepositoryApi.updateResourceDocumentNodeName(this.getHyperIoTContext(), resourceName, resourceId, pathToFolder, oldNodeName, newNodeName);
			return Response.ok().build();
		}catch (Throwable exc){
			return this.handleException(exc);
		}
	}

	/**
	 *  Service deletes all documents from resource's document repository. (Resource is identified by the pair resourceName-resourceId)
	 *
	 * @param resourceName the entity class name to which documents are related
	 * @param resourceId the id of the entity to which documents are related
	 * @return 200 OK if it has been deleted
	 */
	@DELETE
	@Path("/all/resourcename/{resourceName}/resourceid/{resourceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/contentrepositorys/all/resourcename/{resourceName}/resourceid/{resourceId}", notes = "Service for delete all documents from resource's document repository", httpMethod = "DELETE", consumes = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Entity not found") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response deleteAllResourceDocuments(
			@ApiParam(value = "Entity resource name", required = true) @PathParam("resourceName") String resourceName,
			@ApiParam(value = "Entity resource id", required = true) @PathParam("resourceId") long resourceId) {
		getLog().debug("In Rest Service DELETE /hyperiot/contentrepositorys/all/resourcename/{}/resourceid/{}", resourceName, resourceId);
		try{
			this.contentRepositoryApi.removeAllResourceDocuments(this.getHyperIoTContext(), resourceName, resourceId);
			return Response.ok().build();
		}catch (Throwable exc){
			return this.handleException(exc);
		}

	}

	/**
	 * Service deletes a folder from resource's document repository. (Resource is identified by the pair resourceName-resourceId)
	 *
	 * @param resourceName the entity class name to which documents are related
	 * @param resourceId the id of the entity to which documents are related
	 * @param nodePath the absolute path of the folder in which the folder will be search. (absolute respect the jcr repository)
	 * @param folderName the name of the folder that will be delete
	 * @return 200 OK if it has been deleted
	 */
	@DELETE
	@Path("/resourcename/{resourceName}/resourceid/{resourceId}/folder/{folderName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/contentrepositorys/resourcename/{resourceName}/resourceid/{resourceId}/folder/{folderName}", notes = "Service for delete a subFolder from entity's document repository", httpMethod = "DELETE", consumes = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Entity not found") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response deleteResourceDocumentFolder(
			@ApiParam(value = "Entity resource name", required = true) @PathParam("resourceName") String resourceName,
			@ApiParam(value = "Entity resource id", required = true) @PathParam("resourceId") long resourceId ,
			@ApiParam(value = "The name of the subfolder", required = true) @PathParam("folderName") String folderName,
			HyperIoTDocumentNode nodePath) {
		getLog().debug("In Rest Service DELETE /hyperiot/contentrepositorys/resourcename/{}/resourceid/{}/folder/{}", resourceName, resourceId, folderName);
		try{
			this.contentRepositoryApi.removeFolderFromDocumentResource(this.getHyperIoTContext(), resourceName, resourceId, nodePath, folderName);
			return Response.ok().build();
		}catch (Throwable exc) {
			return this.handleException(exc);
		}

	}

	/**
	 * Service deletes a file from folder related to resource . (Resource is identified by the pair resourceName-resourceId)
	 *
	 * @param resourceName the entity class name to which documents are related
	 * @param resourceId the id of the entity to which documents are related
	 * @param nodePath the absolute path of the folder in which the file will be search. (absolute respect the jcr repository)
	 * @param fileName the name of the file that will be delete
	 * @return 200 OK if it has been deleted
	 */
	@DELETE
	@Path("/resourcename/{resourceName}/resourceid/{resourceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LoggedIn
	@ApiOperation(value = "/hyperiot/contentrepositorys/resourcename/{resourceName}/resourceid/{resourceId}", notes = "Service for deleting a file from document's subfolder", httpMethod = "DELETE", consumes = "application/json",authorizations = @Authorization("jwt-auth"))
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
			@ApiResponse(code = 403, message = "Not authorized"),
			@ApiResponse(code = 500, message = "Entity not found") })
	@JsonView(HyperIoTJSONView.Public.class)
	public Response deleteDocumentFileFromResourceFolder(
			@ApiParam(value = "Entity resource name", required = true) @PathParam("resourceName") String resourceName,
			@ApiParam(value = "Entity resource id", required = true) @PathParam("resourceId") long resourceId ,
			@QueryParam("filename") String fileName,
			HyperIoTDocumentNode nodePath) {
		getLog().debug("In Rest Service DELETE /hyperiot/contentrepositorys/resourcename/{}/resourceid/{}?filename={}", resourceName, resourceId, fileName);
		try{
			this.contentRepositoryApi.removeFileFromDocumentResourceFolder(this.getHyperIoTContext(), resourceName, resourceId, nodePath, fileName);
			return Response.ok().build();
		}catch (Throwable exc){
			return this.handleException(exc);
		}

	}

}
