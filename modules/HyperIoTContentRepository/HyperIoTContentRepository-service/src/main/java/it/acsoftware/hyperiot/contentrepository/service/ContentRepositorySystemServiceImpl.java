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

package it.acsoftware.hyperiot.contentrepository.service;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl;
import it.acsoftware.hyperiot.contentrepository.api.ContentRepositorySystemApi;
import it.acsoftware.hyperiot.contentrepository.api.ContentRepositoryUtil;
import it.acsoftware.hyperiot.contentrepository.model.DocumentResourceType;
import it.acsoftware.hyperiot.contentrepository.model.HyperIoTDocumentNode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import javax.jcr.*;
import java.io.InputStream;
import java.util.*;

import static it.acsoftware.hyperiot.contentrepository.service.ContentRepositoryUtils.*;
import static it.acsoftware.hyperiot.contentrepository.service.ContentRepositoryUtils.JCR_DATA;

/**
 * 
 * @author Aristide Cittadino Implementation class of the ContentRepositorySystemApi
 *         interface. This  class is used to implements all additional
 *         methods to interact with the persistence layer.
 */
@Component(service = ContentRepositorySystemApi.class, immediate = true)
public final class ContentRepositorySystemServiceImpl extends HyperIoTBaseSystemServiceImpl implements ContentRepositorySystemApi {

	private Repository jcrRepository;

	/**
	 * Inject the ContentRepositoryUtil
	 */
	private ContentRepositoryUtil contentRepositoryUtil;

	/**
	 * Constructor for a ContentRepositorySystemServiceImpl
	 */
	public ContentRepositorySystemServiceImpl() {
		super();
	}

	@Reference
	protected void setJcrRepository(Repository jcrRepository) {
		getLog().debug("invoking setJcrRepository, setting: {}" , jcrRepository);
		this.jcrRepository = jcrRepository;
	}

	protected Repository getJcrRepository() {
		getLog().debug("invoking getJcrRepository, returning: {}" , this.jcrRepository);
		return jcrRepository;
	}

	@Reference
	protected void setContentRepositoryUtil(ContentRepositoryUtil contentRepositoryUtil) {
		getLog().debug("invoking setContentRepositoryUtil, setting: {}" , contentRepositoryUtil);
		this.contentRepositoryUtil = contentRepositoryUtil;
	}

	protected ContentRepositoryUtil getContentRepositoryUtil() {
		getLog().debug("invoking getContentRepositoryUtil, returning: {}" , this.contentRepositoryUtil);
		return this.contentRepositoryUtil;
	}

	public void addFileToSubFolder(String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, InputStream fileResource, String fileResourceName){
		Session session = openContentRepositorySession();
		try {
			Binary file = session.getValueFactory().createBinary(fileResource);
			Node resourceRootFolder = getOrCreateResourceRootFolder(session, resourceClassName, resourceId);
			HyperIoTDocumentNode currentNode = folderPath;
			Node currentSubFolder = getOrCreateSubFolder(resourceRootFolder, folderPath.getDocumentResourceName());
			while(currentNode != null && ! currentNode.getDocumentResourceChild().isEmpty()){
					currentNode = currentNode.getDocumentResourceChild().get(0);
					currentSubFolder = getOrCreateSubFolder(currentSubFolder, currentNode.getDocumentResourceName());
			}
			Node fileNode = (currentSubFolder.hasNode(fileResourceName))?
					currentSubFolder.getNode(fileResourceName):
					currentSubFolder.addNode(fileResourceName, NT_FILE);
			if(fileNode.hasNode(JCR_CONTENT)){
				//If file has a content, override content of the file.
				session.getNode(fileNode.getPath()).remove();
				fileNode = currentSubFolder.addNode(fileResourceName,NT_FILE);
			}
			Node fileContent = fileNode.addNode(JCR_CONTENT, NT_RESOURCE);
			fileContent.setProperty(JCR_DATA,file);
			session.save();
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
	}

	@Override
	public void addFolderToResourceDocumentsRepository(String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String folderName) {
		Session session = openContentRepositorySession();
		try {
			Node resourceRootFolder = getOrCreateResourceRootFolder(session, resourceClassName, resourceId);
			HyperIoTDocumentNode currentNode = folderPath;
			Node currentSubFolder = getOrCreateSubFolder(resourceRootFolder, folderPath.getDocumentResourceName());
			while(currentNode != null && ! currentNode.getDocumentResourceChild().isEmpty()){
				currentNode = currentNode.getDocumentResourceChild().get(0);
				currentSubFolder = getOrCreateSubFolder(currentSubFolder, currentNode.getDocumentResourceName());
			}
			getOrCreateSubFolder(currentSubFolder, folderName);
			session.save();
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
	}


	public List<HyperIoTDocumentNode> getResourceDocumentsTreeView(String resourceClassName, long resourceId){
		Session session = openContentRepositorySession();
		HyperIoTDocumentNode rootDocumentTreeView = null;
		try {
			Node resourceRootFolder = getOrCreateResourceRootFolder(session, resourceClassName, resourceId);
			LinkedList<Node> documentGerarchy = new LinkedList<>();
			HashMap<String, HyperIoTDocumentNode> gerarchyMap  = new HashMap<>();
			documentGerarchy.add(resourceRootFolder);
			rootDocumentTreeView = buildHyperIoTDocumentNodeFromJcrNode(resourceRootFolder);
			gerarchyMap.put(resourceRootFolder.getIdentifier(), rootDocumentTreeView);
			while(! documentGerarchy.isEmpty()){
				Node currentNode = documentGerarchy.removeFirst();
				HyperIoTDocumentNode currentPath = gerarchyMap.get(currentNode.getIdentifier());
				NodeIterator iterator = currentNode.getNodes();
				while(iterator.hasNext()){
					Node childNode = iterator.nextNode();
					HyperIoTDocumentNode childPath = buildHyperIoTDocumentNodeFromJcrNode(childNode);
					if(childPath != null){
						currentPath.getDocumentResourceChild().add(childPath);
					}
					if(jcrNodeRepresentFolder(childNode)){
						documentGerarchy.add(childNode);
						gerarchyMap.put(childNode.getIdentifier(), childPath);
					}
				}
			}
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
		return  rootDocumentTreeView.getDocumentResourceChild();
	}

	public InputStream getResourceFile(String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String fileName){
		Session session = openContentRepositorySession();
		InputStream resource = null;
		try {
			String exceptionMessage = "No resource associated to path";
			Node resourceRootFolder = getOrCreateResourceRootFolder(session, resourceClassName, resourceId);
			HyperIoTDocumentNode currentNode = folderPath;
			Node currentSubFolder = resourceRootFolder.getNode(folderPath.getDocumentResourceName());
			while(currentNode != null && ! currentNode.getDocumentResourceChild().isEmpty()){
				currentNode = currentNode.getDocumentResourceChild().get(0);
				if(! currentSubFolder.hasNode(currentNode.getDocumentResourceName())) {
					throw new HyperIoTRuntimeException(exceptionMessage);
				}
				currentSubFolder = currentSubFolder.getNode(currentNode.getDocumentResourceName());
			}
			Node fileNode = currentSubFolder.getNode(fileName);
			if(! fileNode.hasNode(JCR_CONTENT)){
				throw new HyperIoTRuntimeException(exceptionMessage);
			}
			Node fileContent = fileNode.getNode(JCR_CONTENT);
			if(! fileContent.hasProperty(JCR_DATA)){
				throw new HyperIoTRuntimeException(exceptionMessage);
			}
			resource= fileContent.getProperty(JCR_DATA).getBinary().getStream();
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
		return resource;
	}

	@Override
	public void initializeResourceContentStore(String resourceClassName, long resourceId, Collection<String> subFolderPath) {
		Session session = openContentRepositorySession();
		String rootResourceFolderName = getResourceRootFolderPath(resourceClassName,resourceId);
		try {
			Node rootNode = session.getRootNode();
			if(! rootNode.hasNode(rootResourceFolderName)){
				Node resourceRootFolder =rootNode.addNode(rootResourceFolderName, NT_FOLDER);
				for(String subPath : subFolderPath){
					resourceRootFolder.addNode(subPath,NT_FOLDER);
				}
				session.save();
			}
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
	}

	@Override
	public void removeAllResourceDocuments(String resourceClassName, long resourceId) {
		Session session = openContentRepositorySession();
		String resourceRootFolderName = getResourceRootFolderPath(resourceClassName,resourceId);
		try {
			Node rootNode = session.getRootNode();
			if(rootNode.hasNode(resourceRootFolderName)){
				rootNode.getNode(resourceRootFolderName).remove();
				session.save();
			}
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
	}

	@Override
	public void removeFolderFromDocumentResource(String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String folderName) {
		Session session = openContentRepositorySession();
		try {
			String resourceRootFolderName = getResourceRootFolderPath(resourceClassName,resourceId);
			Node rootNode = session.getRootNode();
			if(! rootNode.hasNode(resourceRootFolderName)){
				closeContentRepositorySession(session);
				return;
			}
			Node resourceRootFolder = rootNode.getNode(resourceRootFolderName);
			HyperIoTDocumentNode currentNode = folderPath;
			Node currentSubFolder = resourceRootFolder;
			while(currentNode != null && ! currentNode.getDocumentResourceChild().isEmpty()){
				currentNode = currentNode.getDocumentResourceChild().get(0);
				if(! currentSubFolder.hasNode(currentNode.getDocumentResourceName())) {
					throw new HyperIoTRuntimeException("Path specificied is wrong");
				}
				currentSubFolder = currentSubFolder.getNode(currentNode.getDocumentResourceName());
			}

			Node subFolderNode = resourceRootFolder.getNode(folderName);
			if(! jcrNodeRepresentFolder(subFolderNode)){
				throw new HyperIoTRuntimeException("Not exist folder " + folderName + "in the specified path");
			}
			subFolderNode.remove();
			session.save();
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
	}

	@Override
	public void removeFileFromDocumentResourceFolder(String resourceClassName, long resourceId , HyperIoTDocumentNode folderPath, String fileName){
		Session session = openContentRepositorySession();
		String resourceRootFolderName = getResourceRootFolderPath(resourceClassName,resourceId);
		try {
			Node rootNode = session.getRootNode();
			if(! rootNode.hasNode(resourceRootFolderName)){
				closeContentRepositorySession(session);
				return;
			}
			Node resourceRootFolder = rootNode.getNode(resourceRootFolderName);
			HyperIoTDocumentNode currentNode = folderPath;
			Node currentSubFolder = resourceRootFolder;
			while(currentNode != null && !currentNode.getDocumentResourceChild().isEmpty()){
				currentNode = currentNode.getDocumentResourceChild().get(0);
				if(! currentSubFolder.hasNode(currentNode.getDocumentResourceName())) {
					throw new HyperIoTRuntimeException("Path specificied is wrong");
				}
				currentSubFolder = currentSubFolder.getNode(currentNode.getDocumentResourceName());
			}

			Node fileNode = currentSubFolder.getNode(fileName);
			if(! jcrNodeRepresentFile(fileNode)){
				throw new HyperIoTRuntimeException("Not exist file :  " + fileName + " , in the specified path");
			}
			fileNode.remove();
			session.save();
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
	}

	@Override
	public void updateResourceDocumentNodeName(String resourceClassName, long resourceId, HyperIoTDocumentNode folderPath, String oldName, String newName) {
		Session session = openContentRepositorySession();
		try {
			Node resourceRootFolder = getOrCreateResourceRootFolder(session, resourceClassName, resourceId);
			HyperIoTDocumentNode currentNode = folderPath;
			Node currentSubFolder = getOrCreateSubFolder(resourceRootFolder, folderPath.getDocumentResourceName());
			while(currentNode != null && ! currentNode.getDocumentResourceChild().isEmpty()){
				currentNode = currentNode.getDocumentResourceChild().get(0);
				currentSubFolder = getOrCreateSubFolder(currentSubFolder, currentNode.getDocumentResourceName());
			}
			if( !currentSubFolder.hasNode(oldName)){
				throw new HyperIoTRuntimeException("Not exist folder " + oldName + "in the specified path");
			}
			Node documentNode = currentSubFolder = currentSubFolder.getNode(oldName);
			if(! jcrNodeRepresentFile(documentNode) && ! jcrNodeRepresentFolder(documentNode)){
				throw new HyperIoTRuntimeException("Error, document node type cannot be renamed");
			}
			renameJcrNode(currentSubFolder, newName);
			session.save();
		}catch(Throwable exc) {
			closeContentRepositorySessionAbruptly(session,exc);
		}
		closeContentRepositorySession(session);
	}

	//The Session represent the entry point of the ContentRepository's Interface.
	private Session openContentRepositorySession() {
		try {
			String defaultUserId = this.contentRepositoryUtil.getContentRepositoryDefaultUserId();
			String defaultUserPassword = this.contentRepositoryUtil.getContentRepositoryDefaultUserPassword();
			String defaultWorkspaceName = this.contentRepositoryUtil.getContentRepositoryDefaultWorkspaceName();
			Session session= jcrRepository.login(new SimpleCredentials(defaultUserId, defaultUserPassword.toCharArray()), defaultWorkspaceName);
			getLog().debug("Logged in as {} to a {} repository.", session.getUserID(), jcrRepository.getDescriptor(Repository.REP_NAME_DESC));
			return session;
		}catch (Exception exc){
			throw new HyperIoTRuntimeException(exc);
		}
	}

	private void closeContentRepositorySession(Session session) {
		session.logout();
	}

	private void closeContentRepositorySessionAbruptly(Session session,Throwable exc) {
		session.logout();
		throw new HyperIoTRuntimeException(exc);
	}

	private void renameJcrNode(Node jcrNode, String newName) throws RepositoryException {
		getLog().debug("Move document node from  path : {} , to path : {}", jcrNode.getPath(), jcrNode.getParent().getPath() + "/" + newName);
		jcrNode.getSession().move(jcrNode.getPath(), jcrNode.getParent().getPath() + "/" + newName);
	}

	private Node getOrCreateResourceRootFolder(Session session, String resourceClassName, long resourceId) throws RepositoryException {
		Node rootNode = session.getRootNode();
		String rootResourceFolderName = getResourceRootFolderPath(resourceClassName,resourceId);
		return getOrCreateSubFolder(rootNode, rootResourceFolderName);
	}

	private Node getOrCreateSubFolder(Node node, String folderName) throws RepositoryException {
		return node.hasNode(folderName) ?
				node.getNode(folderName) :
				node.addNode(folderName, NT_FOLDER);
	}

	private boolean jcrNodeRepresentFolder(Node jcrNode) {
		try {
			return jcrNode.getPrimaryNodeType().getName().equals(NT_FOLDER);
		} catch (Exception e){
			getLog().debug(e.getMessage(), e);
		}
		return  false;
	}

	private boolean jcrNodeRepresentFile(Node jcrNode) {
		try {
			return jcrNode.getPrimaryNodeType().getName().equals(NT_FILE);
		} catch (Exception e){
			getLog().debug(e.getMessage(), e);
		}
		return false;
	}

	private HyperIoTDocumentNode buildHyperIoTDocumentNodeFromJcrNode(Node jcrNode) throws RepositoryException {
		if(this.jcrNodeRepresentFolder(jcrNode)){
			HyperIoTDocumentNode jcrPath  = new HyperIoTDocumentNode();
			jcrPath.setDocumentResourceName(jcrNode.getName());
			jcrPath.setDocumentResourceType(DocumentResourceType.FOLDER_TYPE);
			return jcrPath;
		} else if (this.jcrNodeRepresentFile(jcrNode)) {
			HyperIoTDocumentNode jcrPath = new HyperIoTDocumentNode();
			jcrPath.setDocumentResourceName(jcrNode.getName());
			jcrPath.setDocumentResourceType(DocumentResourceType.FILE_TYPE);
			return jcrPath;
		}
		return null;
	}
}
