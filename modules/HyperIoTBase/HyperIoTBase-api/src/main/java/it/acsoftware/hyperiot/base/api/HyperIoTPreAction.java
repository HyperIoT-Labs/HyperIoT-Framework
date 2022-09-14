package it.acsoftware.hyperiot.base.api;

/**
 * Interface which map the concept of an action which is going to be executed after another one on a particular entity
 * @param <T> Resource type
 */
public interface HyperIoTPreAction<T extends HyperIoTResource> extends HyperIoTActionListener{

    /**
     * Execute an action after another one
     * @param entity Entity on which the first action was executed
     */
    void execute(T entity);
}
