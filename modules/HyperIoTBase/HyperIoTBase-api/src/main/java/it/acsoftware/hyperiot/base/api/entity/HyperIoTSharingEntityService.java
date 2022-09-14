package it.acsoftware.hyperiot.base.api.entity;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;

import java.util.List;

public interface HyperIoTSharingEntityService {
    List<Long> getEntityIdsSharedWithUser(String entityResourceName, long userId, HyperIoTContext context);
    List<HyperIoTUser> getSharingUsers(String entityResourceName, long entityId, HyperIoTContext context);
}
