package it.acsoftware.hyperiot.base.api.entity;

/**
 * @param <T> Object rapresenting a HyperIoTBaseEntity
 *            <p>
 *            This interface is used for being notified for detailed update.
 *            Use this interface if you want receive the entity before the update and the entity after.
 *            Otherwise, if you are interested only in the last value, please use HyperIoTPostUpdateAction
 * @author Aristide Cittadino
 */
public interface HyperIoTPostUpdateDetailedAction<T extends HyperIoTBaseEntity> extends HyperIoTPostCrudAction<T> {
    /**
     * Execute update passing before update value and after update value.
     * This method invokes execute method as default.
     *
     * @param beforeCrudActionEntity Entity before crud action
     * @param afterCrudActionEntity  Entity after crud action
     */
    void execute(T beforeCrudActionEntity, T afterCrudActionEntity);

    default void execute(T entity) {
        throw new RuntimeException("This post actions uses executeOnUpdateMethod, please use that method");
    }
}
