package it.acsoftware.hyperiot.base.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;

/**
 * @author Aristide Cittadino
 * This interface tells HyperIoT system that should be accessed or modified by the current logged user.
 * This prevents maliciuous users to create entities with other user's ids.
 * Only Users that have IMPERSONATION permission can do this kind of operation
 */
public interface HyperIoTOwnedResource extends HyperIoTBaseEntity {
    HyperIoTUser getUserOwner();

    void setUserOwner(HyperIoTUser user);
}
