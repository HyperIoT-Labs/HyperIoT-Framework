package it.acsoftware.hyperiot.base.api.entity;

/**
 * Interface which map the concept of a saving action which is going to be executed after another one on a particular entity
 * @param <T> Entity type
 */
public interface HyperIoTPreSaveAction<T extends HyperIoTBaseEntity> extends HyperIoTPreCrudAction<T> {

}
