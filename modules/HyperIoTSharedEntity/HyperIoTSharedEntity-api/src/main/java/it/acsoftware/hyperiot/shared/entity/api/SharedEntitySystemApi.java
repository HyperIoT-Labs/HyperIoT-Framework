package it.acsoftware.hyperiot.shared.entity.api;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTSharedEntity;
import it.acsoftware.hyperiot.shared.entity.model.SharedEntity;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 *         interface defines methods for additional operations.
 *
 */
public interface SharedEntitySystemApi extends HyperIoTBaseEntitySystemApi<SharedEntity> {
    SharedEntity findByPK(String entityResourceName, long entityId, long userId, HashMap<String, Object> filter, HyperIoTContext context);
    void removeByPK(String entityResourceName, long entityId, long userId, HyperIoTContext context);
    List<SharedEntity> findByEntity(String entityResourceName, long entityId, HashMap<String, Object> filter, HyperIoTContext context);
    List<SharedEntity> findByUser(long userId, HashMap<String, Object> filter, HyperIoTContext context);
    List<HyperIoTUser> getSharingUsers(String entityResourceName, long entityId, HyperIoTContext context);
    List<Long> getEntityIdsSharedWithUser(String entityResourceName, long userId, HyperIoTContext context);
}
