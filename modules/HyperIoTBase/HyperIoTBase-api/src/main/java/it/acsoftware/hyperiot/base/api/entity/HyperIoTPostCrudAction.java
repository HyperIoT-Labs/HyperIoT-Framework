package it.acsoftware.hyperiot.base.api.entity;

import it.acsoftware.hyperiot.base.api.HyperIoTPostAction;

/**
 * Interface which map the concept of a CRUD action which is going to be executed after another one on a particular entity
 * @param <T> Entity type
 */
public interface HyperIoTPostCrudAction<T extends HyperIoTBaseEntity> extends HyperIoTPostAction<T> {

}
