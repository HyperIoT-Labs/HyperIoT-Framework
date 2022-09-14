package it.acsoftware.hyperiot.base.api.entity;

/**
 * Interface which map the concept of an updating action which is going to be executed after another one on a particular entity
 * @param <T> Entity type
 */
public interface HyperIoTPostUpdateAction<T extends HyperIoTBaseEntity> extends HyperIoTPostCrudAction<T> {

}
