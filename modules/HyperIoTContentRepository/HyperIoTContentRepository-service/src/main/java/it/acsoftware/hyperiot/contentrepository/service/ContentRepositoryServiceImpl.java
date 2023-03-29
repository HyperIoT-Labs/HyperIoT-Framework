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

package it.acsoftware.hyperiot.contentrepository.service;

import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.contentrepository.actions.ContentRepositoryAction;
import it.acsoftware.hyperiot.contentrepository.api.ContentRepositoryApi;
import it.acsoftware.hyperiot.contentrepository.api.ContentRepositorySystemApi;
import it.acsoftware.hyperiot.contentrepository.model.HyperIoTDocumentNode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Repository;
import javax.persistence.NoResultException;
import java.io.InputStream;
import java.util.*;


/**
 * 
 * @author Aristide Cittadino Implementation class of ContentRepositoryApi interface.
 *         It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = ContentRepositoryApi.class, immediate = true)
public final class ContentRepositoryServiceImpl extends HyperIoTBaseServiceImpl implements ContentRepositoryApi {
	/**
	 * Injecting the ContentRepositorySystemApi
	 */
	private ContentRepositorySystemApi systemService;

	private Repository jcrRepository;
	
	/**
	 * Constructor for a ContentRepositoryServiceImpl
	 */
	public ContentRepositoryServiceImpl() {
		super();
	}
	
	/**
	 * 
	 * @return The current ContentRepositorySystemApi
	 */
	protected ContentRepositorySystemApi getSystemService() {
		getLog().debug("invoking getSystemService, returning: {}" , this.systemService);
		return systemService;
	}

	/**
	 * 
	 * @param contentRepositorySystemService Injecting via OSGi DS current systemService 
	 */
	@Reference
	protected void setSystemService(ContentRepositorySystemApi contentRepositorySystemService) {
		getLog().debug("invoking setSystemService, setting: {}" , systemService);
		this.systemService = contentRepositorySystemService ;
	}
	@Reference
	protected void setHPacketRepository(Repository jcrRepository) {
		this.jcrRepository = jcrRepository;
	}

	protected Repository getJcrRepository() {
		getLog().debug("invoking getJcrRepository, returning: {}" , this.jcrRepository);
		return jcrRepository;
	}


	@Override
	public void addFileToSubFolder(HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, InputStream fileResource, String fileResourceName) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		systemService.addFileToSubFolder(resourceClassName, resourceId, folderPath, fileResource, fileResourceName);
	}

	@Override
	public void addFolderToResourceDocumentsRepository(HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String folderName) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		systemService.addFolderToResourceDocumentsRepository(resourceClassName, resourceId, folderPath, folderName);
	}

	@Override
	public InputStream getResourceFile(HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String fileName) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		return systemService.getResourceFile(resourceClassName,resourceId,folderPath,fileName);
	}

	@Override
	public List<HyperIoTDocumentNode> getResourceDocumentsTreeView(HyperIoTContext ctx, String resourceClassName, long resourceId) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		return systemService.getResourceDocumentsTreeView(resourceClassName, resourceId);
	}

	@Override
	public void initializeResourceContentStore(HyperIoTContext ctx, String resourceClassName, long resourceId, Collection<String> subFolderPath) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		systemService.initializeResourceContentStore(resourceClassName,resourceId,subFolderPath);
	}

	@Override
	public void removeAllResourceDocuments(HyperIoTContext ctx, String resourceClassName, long resourceId) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		systemService.removeAllResourceDocuments(resourceClassName,resourceId);

	}

	@Override
	public void removeFolderFromDocumentResource(HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String folderName) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		systemService.removeFolderFromDocumentResource(resourceClassName, resourceId, folderPath, folderName);
	}

	@Override
	public void removeFileFromDocumentResourceFolder(HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String fileName) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		systemService.removeFileFromDocumentResourceFolder(resourceClassName, resourceId, folderPath, fileName);
	}

	@Override
	public void updateResourceDocumentNodeName(HyperIoTContext ctx, String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String oldName, String newName) {
		if (! userHasPermissionToManageResourceDocuments(ctx, resourceClassName, resourceId)){
			throw new HyperIoTUnauthorizedException();
		}
		systemService.updateResourceDocumentNodeName(resourceClassName, resourceId, folderPath, oldName, newName);
	}


	private boolean userHasPermissionToManageResourceDocuments(HyperIoTContext ctx, String resourceName, long resourceId){
		Class<?> entityClass = (resourceName != null && ! resourceName.isEmpty()) ? getEntityClass(resourceName) : null;
		if( entityClass == null){
			throw new HyperIoTUnauthorizedException();
		}
		HyperIoTBaseEntitySystemApi<? extends HyperIoTBaseEntity> systemApi = getEntitySystemService(entityClass);
		HyperIoTBaseEntity entity ;
		try {
			entity = systemApi.find(resourceId, ctx);
		} catch (NoResultException e){
			throw new HyperIoTEntityNotFound();
		}
		return HyperIoTSecurityUtil.checkPermission(ctx, entity, HyperIoTActionsUtil.getHyperIoTAction(resourceName, ContentRepositoryAction.DOCUMENT_MANAGEMENT_ACTION));
	}

	private HyperIoTBaseEntitySystemApi<? extends HyperIoTBaseEntity> getEntitySystemService(Class<?> entityClass) {
		this.getLog().debug("Get system service of entity {}", entityClass.getSimpleName());
		Class<?> systemApiClass = null;
		String systemApiClassName = entityClass.getName().replace(".model.", ".api.") + "SystemApi";
		try {
			systemApiClass = Class.forName(systemApiClassName);
		} catch (ClassNotFoundException e) {
			throw new HyperIoTRuntimeException("No class found for system service :  " + systemApiClassName);
		}
		HyperIoTBaseEntitySystemApi<? extends HyperIoTBaseEntity> systemApi = null;
		try{
			systemApi = (HyperIoTBaseEntitySystemApi<? extends HyperIoTBaseEntity>) HyperIoTUtil.getService(systemApiClass);
		} catch (Exception e){
			throw new HyperIoTRuntimeException("No such system service found for entity " + entityClass.getSimpleName());
		}
		return systemApi;
	}

	private Class<?> getEntityClass(String resourceName) {
		try {
			return Class.forName(resourceName);
		} catch (ClassNotFoundException e) {
			throw new HyperIoTRuntimeException("Entity class : " + resourceName + " not found");
		}
	}

}
