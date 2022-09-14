package it.acsoftware.hyperiot.base.api.entity;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import org.apache.aries.jpa.template.EmConsumer;
import org.apache.aries.jpa.template.EmFunction;
import org.apache.aries.jpa.template.TransactionType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @param <T> parameter that indicates a generic class
 * @author Aristide Cittadino Interface component for HyperIoTBaseRepository.
 * This interface defines the methods for basic CRUD operations. This
 * methods are reusable by all entities that interact with the HyperIoT
 * platform.
 */
public interface HyperIoTBaseRepository<T extends HyperIoTBaseEntity> {

    /**
     * Save an entity in database
     *
     * @param entity parameter that indicates a generic entity
     * @return entity saved
     */
    T save(T entity);

    /**
     * Update an existing entity in database
     *
     * @param entity parameter that indicates a generic entity
     */
    T update(T entity);

    /**
     * Remove an entity in database
     *
     * @param id parameter that indicates a entity id
     */
    void remove(long id);

    /**
     *
     * @param id entity id
     * @param ctx HyperIoTContext
     * @return Entity found or runtime Exception
     */
    T find(long id, HyperIoTContext ctx);


    /**
     * Find an existing entity in database
     * @param filter filter
     * @param ctx user context of HyperIoT platform
     * @return Entity if found
     */
    T find(HyperIoTQuery filter, HyperIoTContext ctx);


    /**
     * Find all entity in database
     *
     * @return Collection of entity
     */
    Collection<T> findAll(HyperIoTQuery filter);

    /**
     * Find all entity in database
     *
     * @param queryOrder parameter that define order's criteria
     * @return Collection of entity
     */
    public Collection<T> findAll(HyperIoTQuery filter, HyperIoTQueryOrder queryOrder);


    /**
     * Find all entity in database with paginated result
     *
     * @return Collection of entity
     */
    HyperIoTPaginableResult<T> findAll(int delta, int page, HyperIoTQuery filter);

    /**
     * Find all entity in database with paginated result
     *
     * @param queryOrder parameter that define order's criteria
     * @return Collection of entity
     */
    HyperIoTPaginableResult<T> findAll(int delta, int page, HyperIoTQuery filter, HyperIoTQueryOrder queryOrder);

    /**
     *
     * @param filter filter
     * @return total number of entities based on the given filter.
     */
     long countAll(HyperIoTQuery filter);
    /**
     * Executes code inside a transaction without returning  result
     *
     * @param txType
     * @param function
     */
    void executeTransaction(TransactionType txType, EmConsumer function);

    /**
     * Executes code inside a transaction returning a result
     *
     * @param txType
     * @param function
     * @param <R>
     * @return
     */
    <R> R executeTransactionWithReturn(TransactionType txType, EmFunction<R> function);

    /**
     * It executes an update or delete statement.
     *
     * @param query  parameter that defines the query in the hql language
     * @param params parameter that indicates the value to set within the query
     * @return the number of entities updated or deleted
     */
    int executeUpdateQuery(String query, HashMap<String, Object> params);
}
