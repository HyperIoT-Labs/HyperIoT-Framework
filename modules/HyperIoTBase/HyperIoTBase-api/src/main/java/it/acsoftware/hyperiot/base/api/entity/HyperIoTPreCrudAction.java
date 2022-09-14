package it.acsoftware.hyperiot.base.api.entity;

import it.acsoftware.hyperiot.base.api.HyperIoTPostAction;
import it.acsoftware.hyperiot.base.api.HyperIoTPreAction;

/**
 * Interface which map the concept of a CRUD action which is going to be executed after another one on a particular entity
 * @param <T> Entity type
 */
public interface HyperIoTPreCrudAction<T extends HyperIoTBaseEntity> extends HyperIoTPreAction<T> {

}
