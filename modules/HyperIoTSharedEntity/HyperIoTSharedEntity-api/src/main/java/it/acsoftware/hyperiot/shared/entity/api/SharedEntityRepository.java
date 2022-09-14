package it.acsoftware.hyperiot.shared.entity.api;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;
import it.acsoftware.hyperiot.shared.entity.model.SharedEntity;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Aristide Cittadino Interface component for SharedEntity Repository.
 *         It is used for CRUD operations,
 *         and to interact with the persistence layer.
 *
 */
public interface SharedEntityRepository extends HyperIoTBaseRepository<SharedEntity> {
	SharedEntity findByPK(String entityResourceName, long entityId, long userId, HashMap<String, Object> filter);
	void removeByPK(String entityResourceName, long entityId, long userId);
	List<SharedEntity> findByEntity(String entityResourceName, long entityId, HashMap<String, Object> filter);
	List<SharedEntity> findByUser(long userId, HashMap<String, Object> filter);
	List<HyperIoTUser> getSharingUsers(String entityResourceName, long entityId);
	List<Long> getEntityIdsSharedWithUser(String entityResourceName, long userId);
}
