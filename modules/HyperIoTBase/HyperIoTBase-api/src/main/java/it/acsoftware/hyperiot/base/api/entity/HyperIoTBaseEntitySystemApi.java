package it.acsoftware.hyperiot.base.api.entity;

import java.util.Collection;
import java.util.List;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTService;
import org.apache.aries.jpa.template.EmConsumer;
import org.apache.aries.jpa.template.EmFunction;
import org.apache.aries.jpa.template.TransactionType;

/**
 * @param <T> parameter that indicates a generic class
 * @author Aristide Cittadino Interface component for HyperIoTBaseEntityApi.
 * This interface defines the methods for basic CRUD operations. This
 * methods are reusable by all entities that interact with the HyperIoT
 * platform.
 */
public interface HyperIoTBaseEntitySystemApi<T extends HyperIoTBaseEntity> extends HyperIoTService {

    /**
     * Save an entity in database
     *
     * @param entity parameter that indicates a generic entity
     * @param ctx    user context of HyperIoT platform
     * @return entity saved
     */
    public T save(T entity, HyperIoTContext ctx);

    /**
     * Update an existing entity in database
     *
     * @param entity parameter that indicates a generic entity
     * @param ctx    user context of HyperIoT platform
     */
    public T update(T entity, HyperIoTContext ctx);

    /**
     * Remove an entity in database
     *
     * @param id  parameter that indicates a entity id
     * @param ctx user context of HyperIoT platform
     */
    public void remove(long id, HyperIoTContext ctx);

    /**
     *
     * @param id
     * @param ctx
     * @return
     */
    public T find(long id, HyperIoTContext ctx);


    /**
     * Find an existing entity in database
     * @param filter filter
     * @param ctx user context of HyperIoT platform
     * @return Entity if found
     */
    public T find(HyperIoTQuery filter, HyperIoTContext ctx);

    /**
     * Find all entity in database
     *
     * @param ctx user context of HyperIoT platform
     * @return Collection of entity
     */
    public Collection<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx);

    /**
     * Find all entity in database
     *
     * @param ctx user context of HyperIoT platform
     * @param queryOrder parameter that define order's criteria
     * @return Collection of entity
     */
    public Collection<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx, HyperIoTQueryOrder queryOrder);

    /**
     * Find all entity in database
     *
     * @param ctx   user context of HyperIoT platform
     * @param delta
     * @param page
     * @return Collection of entity
     */
    public HyperIoTPaginableResult<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx, int delta, int page);

    /**
     * Find all entity in database
     *
     * @param queryOrder parameter that define order's criteria
     * @param ctx   user context of HyperIoT platform
     * @param delta
     * @param page
     * @return Collection of entity
     */
    public HyperIoTPaginableResult<T> findAll(HyperIoTQuery filter, HyperIoTContext ctx, int delta, int page, HyperIoTQueryOrder queryOrder);

    /**
     *
     * @param filter filter
     * @return total number of entities based on the given filter.
     */
    public long countAll(HyperIoTQuery filter);

    /**
     * Return current entity type
     */
    public Class<T> getEntityType();

    /**
     * Executes code inside a transaction without returning  result
     *
     * @param txType
     * @param function
     */
    public void executeTransaction(TransactionType txType, EmConsumer function);

    /**
     * Executes code inside a transaction returning a result
     *
     * @param txType
     * @param function
     * @param <R>
     * @return
     */
    public <R> R executeTransactionWithReturn(TransactionType txType, EmFunction<R> function);


}
