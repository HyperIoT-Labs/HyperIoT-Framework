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

package it.acsoftware.hyperiot.shared.entity.service;

import java.util.HashMap;
import java.util.List;


import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTSharedEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTSharingEntityService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.shared.entity.api.SharedEntitySystemApi;
import it.acsoftware.hyperiot.shared.entity.api.SharedEntityRepository;
import it.acsoftware.hyperiot.shared.entity.model.SharedEntity;

import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl;

/**
 *
 * @author Aristide Cittadino Implementation class of the SharedEntitySystemApi
 *         interface. This  class is used to implements all additional
 *         methods to interact with the persistence layer.
 */
@Component(service = {SharedEntitySystemApi.class,HyperIoTSharingEntityService.class}, immediate = true)
public final class SharedEntitySystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<SharedEntity>  implements SharedEntitySystemApi, HyperIoTSharingEntityService {

	/**
	 * Injecting the SharedEntityRepository to interact with persistence layer
	 */
	private SharedEntityRepository repository;

	/**
	 * Constructor for a SharedEntitySystemServiceImpl
	 */
	public SharedEntitySystemServiceImpl() {
		super(SharedEntity.class);
	}

	/**
	 * Return the current repository
	 */
	protected SharedEntityRepository getRepository() {
		getLog().debug( "invoking getRepository, returning: {}" , this.repository);
		return repository;
	}

	/**
	 * @param sharedEntityRepository The current value of SharedEntityRepository to interact with persistence layer
	 */
	@Reference
	protected void setRepository(SharedEntityRepository sharedEntityRepository) {
		getLog().debug( "invoking setRepository, setting: {}" , sharedEntityRepository);
		this.repository = sharedEntityRepository;
	}

	@Override
	public SharedEntity update(SharedEntity entity, HyperIoTContext ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(long id, HyperIoTContext ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SharedEntity find(long id, HyperIoTContext ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeByPK(String entityResourceName, long entityId, long userId, HyperIoTContext ctx) {
		this.getLog().debug(
				"System Service Removing entity " + this.getEntityType().getSimpleName() + " with primary key: (entityResourceName: {}, entityId: {}, userId: {})",
				new Object[]{entityResourceName, entityId, userId});
		this.getRepository().removeByPK(entityResourceName, entityId, userId);
	}

	@Override
	public SharedEntity findByPK(String entityResourceName, long entityId, long userId, HashMap<String, Object> filter, HyperIoTContext ctx) {
		this.getLog().debug(
				"System Service Finding entity " + this.getEntityType().getSimpleName() + " with primary key: (entityResourceName: {}, entityId: {}, userId: {})",
				new Object[]{entityResourceName, entityId, userId});
		return this.getRepository().findByPK(entityResourceName, entityId, userId, filter);
	}

	@Override
	public List<SharedEntity> findByUser(long userId, HashMap<String, Object> filter, HyperIoTContext ctx) {
		this.getLog().debug( "System Service Finding entity " + this.getEntityType().getSimpleName() + " with userId: {}", userId);
		return this.getRepository().findByUser(userId, filter);
	}

	@Override
	public List<SharedEntity> findByEntity(String entityResourceName, long entityId, HashMap<String, Object> filter, HyperIoTContext ctx) {
		this.getLog().debug( "System Service Finding entity " + this.getEntityType().getSimpleName() + " with entityResourceName: {} and entityId: {}",
				new Object[]{entityId, entityResourceName});
		return this.getRepository().findByEntity(entityResourceName, entityId, filter);
	}

	@Override
	public List<HyperIoTUser> getSharingUsers(String entityResourceName, long entityId, HyperIoTContext context) {
		this.getLog().debug( "System Service getSharingUsers with entityResourceName: {} and entityId: {}",
				new Object[]{entityResourceName, entityId});
		return this.getRepository().getSharingUsers(entityResourceName, entityId);
	}

	@Override
	public List<Long> getEntityIdsSharedWithUser(String entityResourceName, long userId, HyperIoTContext context) {
		this.getLog().debug( "System Service getEntityIdsSharedWithUser with entityResourceName: {} and userId: {}",
				new Object[]{entityResourceName, userId});
		return this.getRepository().getEntityIdsSharedWithUser(entityResourceName, userId);
	}
}
