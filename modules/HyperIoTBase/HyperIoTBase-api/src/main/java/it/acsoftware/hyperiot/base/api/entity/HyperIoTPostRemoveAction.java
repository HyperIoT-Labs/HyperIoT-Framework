package it.acsoftware.hyperiot.base.api.entity;

/**
 * Interface which map the concept of a removing action which is going to be executed after another one on a particular entity
 * @param <T> Entity type
 */
public interface HyperIoTPostRemoveAction<T extends HyperIoTBaseEntity> extends HyperIoTPostCrudAction<T> {

}
