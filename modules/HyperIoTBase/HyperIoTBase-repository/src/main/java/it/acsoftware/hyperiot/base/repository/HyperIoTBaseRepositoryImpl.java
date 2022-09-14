package it.acsoftware.hyperiot.base.repository;

import it.acsoftware.hyperiot.base.api.*;
import it.acsoftware.hyperiot.base.api.entity.*;
import it.acsoftware.hyperiot.base.exception.HyperIoTDuplicateEntityException;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTNoResultException;
import it.acsoftware.hyperiot.base.model.HyperIoTPaginatedResult;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilter;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryBuilder;
import org.apache.aries.jpa.template.EmConsumer;
import org.apache.aries.jpa.template.EmFunction;
import org.apache.aries.jpa.template.JpaTemplate;
import org.apache.aries.jpa.template.TransactionType;
import org.apache.commons.lang3.ClassUtils;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @param <T> parameter that indicates a generic class
 * @author Aristide Cittadino Model class for HyperIoTBaseRepositoryImpl. This
 * class implements all methods for basic CRUD operations defined in
 * HyperIoTBaseRepository interface. This methods are reusable by all
 * entities that interact with the HyperIoT platform.
 */
public abstract class HyperIoTBaseRepositoryImpl<T extends HyperIoTBaseEntity>
        implements HyperIoTBaseRepository<T> {
    private Logger log = LoggerFactory.getLogger(HyperIoTBaseRepositoryImpl.class.getName());

    /**
     * Generic class for HyperIoT platform
     */
    protected Class<T> type;

    /**
     * Constructor for HyperIoTBaseRepositoryImpl
     *
     * @param type parameter that indicates a generic entity
     */
    public HyperIoTBaseRepositoryImpl(Class<T> type) {
        this.type = type;
    }

    /**
     * @return The current jpaTemplate
     */
    protected abstract JpaTemplate getJpa();

    /**
     * @param jpa The jpaTemplate value to interact with database
     */
    protected abstract void setJpa(JpaTemplate jpa);

    /**
     * @return HyperIoTAssetCategoryManager
     */
    public HyperIoTAssetCategoryManager getAssetCategoryManager() {
        return (HyperIoTAssetCategoryManager) HyperIoTUtil.getService(HyperIoTAssetCategoryManager.class);
    }

    /**
     * @return HyperIoTAssetTagManager
     */
    public HyperIoTAssetTagManager getAssetTagManager() {
        return (HyperIoTAssetTagManager) HyperIoTUtil.getService(HyperIoTAssetTagManager.class);
    }

    /**
     * Save an entity in database
     */
    @Override
    public T save(T entity) {
        log.debug(
                "Repository Saving entity {}: {}", new Object[]{this.type.getSimpleName(), entity});
        this.checkDuplicate(entity);
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
            log.debug("Transaction found, invoke persist");
            HyperIoTUtil.invokePreActions(entity, HyperIoTPreSaveAction.class); // execute pre actions after saving
            entityManager.persist(entity);
            manageAssets(entity, AssetManagementOperation.ADD);
            entityManager.flush();
            log.debug("Entity persisted: {}", entity);
            HyperIoTUtil.invokePostActions(entity, HyperIoTPostSaveAction.class); // execute post actions after saving
            return entity;
        });
    }

    /**
     * Update an entity in database
     */
    @Override
    public T update(T entity) {
        log.debug(
                "Repository Update entity {}: {}", new Object[]{this.type.getSimpleName(), entity});
        this.checkDuplicate(entity);
        //Enforcing the concept that the owner cannot be changed
        //TO DO: check if it is useful or not
        T entityFromDb = find(entity.getId(), null);
        if (entityFromDb instanceof HyperIoTOwnedResource) {
            HyperIoTOwnedResource ownedFromDb = (HyperIoTOwnedResource) entityFromDb;
            HyperIoTUser oldOwner = ownedFromDb.getUserOwner();
            HyperIoTOwnedResource owned = (HyperIoTOwnedResource) entity;
            owned.setUserOwner(oldOwner);
        }
        //-
        if (entity.getId() > 0) {
            return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
                log.debug("Transaction found, invoke find and merge");
                T dbEntity = (T) entityManager.find(entity.getClass(), entity.getId());
                //forcing to maintain old create date
                entity.setEntityCreateDate(dbEntity.getEntityCreateDate());
                entity.setEntityVersion(dbEntity.getEntityVersion());
                HyperIoTUtil.invokePreActions(entity, HyperIoTPreUpdateAction.class);
                invokePreUpdateDetailedAction(dbEntity, entity);
                T updateEntity = entityManager.merge(entity);
                manageAssets(entity, AssetManagementOperation.UPDATE);
                entityManager.flush();
                log.debug("Entity merged: {}", entity);
                //Invoking global post actions
                HyperIoTUtil.invokePostActions(entity, HyperIoTPostUpdateAction.class);
                //invoking detailed update actions, for who wants identify what is changed inside entity
                invokePostUpdateDetailedAction(dbEntity, entity); // execute post actions after updating
                return updateEntity;
            });
        }
        throw new HyperIoTEntityNotFound();
    }

    /**
     * Remove an entity by id
     */
    @Override
    public void remove(long id) {
        log.debug(
                "Repository Remove entity {} with id: {}", new Object[]{this.type.getSimpleName(), id});
        this.getJpa().tx(TransactionType.Required, entityManager -> {
            log.debug("Transaction found, invoke remove");
            T entity = find(id, null);
            HyperIoTUtil.invokePreActions(entity, HyperIoTPreRemoveAction.class); // execute pre actions after removing
            entityManager.remove(entity);
            manageAssets(entity, AssetManagementOperation.DELETE);
            entityManager.flush();
            log.debug(
                    "Entity {}  with id: {}  removed", new Object[]{this.type.getSimpleName(), id});
            //we can use global post actions since there's no need to pass "before" entity
            HyperIoTUtil.invokePostActions(entity, HyperIoTPostRemoveAction.class); // execute post actions after removing
        });
    }

    /**
     * @param id  parameter that indicates a entity id
     * @param ctx user context of HyperIoT platformgetCategoryIds
     * @return
     */
    @Override
    public T find(long id, HyperIoTContext ctx) {
        log.debug(
                "Repository Find entity {} with id: {}", new Object[]{this.type.getSimpleName(), id});
        HyperIoTQuery idFilter = HyperIoTQueryBuilder.newQuery().equals("id", id);
        return this.find(idFilter, ctx);
    }


    /**
     * @param filter filter
     * @param ctx    user context of HyperIoT platform
     * @return
     */
    @Override
    public T find(HyperIoTQuery filter, HyperIoTContext ctx) {
        log.debug(
                "Repository Find entity {} with filter: {}", new Object[]{this.type.getSimpleName(), filter});
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
            log.debug("Transaction found, invoke find");
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
            Root<T> entityDef = query.from(this.type);
            Predicate condition = (filter != null) ? filter.buildPredicate(criteriaBuilder, entityDef) : null;
            CriteriaQuery<T> criteriaQuery = (condition != null) ? query.select(entityDef).where(condition) : query.select(entityDef);
            Query q = entityManager.createQuery(criteriaQuery);
            try {
                T entity = (T) q.getSingleResult();
                log.debug("Found entity: {}", entity);
                return entity;
            } catch (NoResultException e) {
                throw new HyperIoTNoResultException();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        });
    }

    /**
     * Find all entity
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<T> findAll(HyperIoTQuery filter) {
        log.debug("Repository Find All entities {}", this.type.getSimpleName());
        return (Collection<T>) this.getJpa().txExpr(TransactionType.RequiresNew, entityManager -> {
            log.debug("Transaction found, invoke findAll");
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
            Root<T> entityDef = query.from(this.type);
            Predicate condition = (filter != null) ? filter.buildPredicate(criteriaBuilder, entityDef) : null;
            Query q = (condition != null) ? entityManager.createQuery(query.select(entityDef).where(condition)) : entityManager.createQuery(query.select(entityDef));
            try {
                Collection<T> results = (Collection<T>) q.getResultList();
                log.debug("Query results: {}", results);
                return results;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        });
    }

    /**
     * Find all entity
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<T> findAll(HyperIoTQuery filter,HyperIoTQueryOrder queryOrder) {
        log.debug("Repository Find All entities {}", this.type.getSimpleName());
        return (Collection<T>) this.getJpa().txExpr(TransactionType.RequiresNew, entityManager -> {
            log.debug("Transaction found, invoke findAll");
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
            Root<T> entityDef = query.from(this.type);
            Predicate condition = (filter != null) ? filter.buildPredicate(criteriaBuilder, entityDef) : null;
            query = (condition != null) ? query.select(entityDef).where(condition) : query.select(entityDef);
            ////Add Order parameter if present
            query = (queryOrder != null && queryOrder.getParametersList() != null && ! queryOrder.getParametersList().isEmpty()) ?
                    query.orderBy(getOrders(criteriaBuilder,entityDef,queryOrder)) : query;
            Query q = entityManager.createQuery(query);
            try {
                Collection<T> results = (Collection<T>) q.getResultList();
                log.debug("Query results: {}", results);
                return results;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        });
    }

    private List<Order> getOrders(CriteriaBuilder criteriaBuilder, Root<T> entityDef, HyperIoTQueryOrder queryOrder) {
        List<Order> criteriaOrderClause = new ArrayList<>();
        List<HyperIoTQueryOrderParameter> parameterList = queryOrder.getParametersList();
        if(parameterList != null && ! parameterList.isEmpty()){
            for(HyperIoTQueryOrderParameter orderParameter : parameterList){
                criteriaOrderClause.add((orderParameter.isAsc()) ? criteriaBuilder.asc(entityDef.get(orderParameter.getName())) :
                        criteriaBuilder.desc(entityDef.get(orderParameter.getName())));
            }
        }
        return criteriaOrderClause;
    }

    /**
     * Find all entity
     */
    @SuppressWarnings("unchecked")
    @Override
    public HyperIoTPaginatedResult<T> findAll(int delta, int page, HyperIoTQuery filter) {
        log.debug("Repository Find All entities {}", this.type.getSimpleName());
        return (HyperIoTPaginatedResult<T>) this.getJpa().txExpr(TransactionType.RequiresNew,
                entityManager -> {
                    log.debug("Transaction found, invoke findAll");
                    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                    //constructing query and count query
                    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
                    CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
                    Root<T> entityDef = query.from(this.type);
                    Root<T> entityDefCount = countQuery.from(this.type);
                    Predicate condition = (filter != null) ? filter.buildPredicate(criteriaBuilder, entityDef) : null;
                    Predicate conditionCount = (filter != null) ? filter.buildPredicate(criteriaBuilder, entityDefCount) : null;
                    countQuery = (condition != null) ? countQuery.select(criteriaBuilder.count(entityDefCount)).where(conditionCount) : countQuery.select(criteriaBuilder.count(entityDefCount));
                    query = (condition != null) ? query.select(entityDef).where(condition) : query.select(entityDef);
                    //Executing count query
                    Query countQueryFinal = entityManager.createQuery(countQuery);
                    Long countResults = (Long) countQueryFinal.getSingleResult();
                    int lastPageNumber = (int) (Math.ceil(countResults / (double) delta));
                    int nextPage = (page <= lastPageNumber - 1) ? page + 1 : 1;
                    //Executing paginated query
                    Query q = entityManager.createQuery(query);
                    int firstResult = (page - 1) * delta;
                    q.setFirstResult(firstResult);
                    q.setMaxResults(delta);
                    try {
                        Collection<T> results = q.getResultList();
                        HyperIoTPaginatedResult<T> paginatedResult = new HyperIoTPaginatedResult<>(
                                lastPageNumber, page, nextPage, delta, results);
                        log.debug("Query results: {}", results);
                        return paginatedResult;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw e;
                    }
                });
    }

    /**
     * Find all entity
     */
    @SuppressWarnings("unchecked")
    @Override
    public HyperIoTPaginatedResult<T> findAll(int delta, int page, HyperIoTQuery filter,HyperIoTQueryOrder queryOrder) {
        log.debug("Repository Find All entities {}", this.type.getSimpleName());
        return (HyperIoTPaginatedResult<T>) this.getJpa().txExpr(TransactionType.RequiresNew,
                entityManager -> {
                    log.debug("Transaction found, invoke findAll");
                    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                    //constructing query and count query
                    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
                    CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
                    Root<T> entityDef = query.from(this.type);
                    Root<T> entityDefCount = countQuery.from(this.type);
                    Predicate condition = (filter != null) ? filter.buildPredicate(criteriaBuilder, entityDef) : null;
                    Predicate conditionCount = (filter != null) ? filter.buildPredicate(criteriaBuilder, entityDefCount) : null;
                    countQuery = (condition != null) ? countQuery.select(criteriaBuilder.count(entityDefCount)).where(conditionCount) : countQuery.select(criteriaBuilder.count(entityDefCount));
                    query = (condition != null) ? query.select(entityDef).where(condition) : query.select(entityDef);
                    //Add Order parameter if present
                    query = (queryOrder != null && queryOrder.getParametersList() != null && ! queryOrder.getParametersList().isEmpty()) ?
                        query.orderBy(getOrders(criteriaBuilder,entityDef,queryOrder)) : query;
                    //Executing count query
                    Query countQueryFinal = entityManager.createQuery(countQuery);
                    Long countResults = (Long) countQueryFinal.getSingleResult();
                    int lastPageNumber = (int) (Math.ceil(countResults / (double) delta));
                    int nextPage = (page <= lastPageNumber - 1) ? page + 1 : 1;
                    //Executing paginated query
                    Query q = entityManager.createQuery(query);
                    int firstResult = (page - 1) * delta;
                    q.setFirstResult(firstResult);
                    q.setMaxResults(delta);
                    try {
                        Collection<T> results = q.getResultList();
                        HyperIoTPaginatedResult<T> paginatedResult = new HyperIoTPaginatedResult<>(
                                lastPageNumber, page, nextPage, delta, results);
                        log.debug("Query results: {}", results);
                        return paginatedResult;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw e;
                    }
                });
    }


    /**
     * Find an entity with a query.
     */

    public final T queryForSingleResult(String query, HashMap<String, Object> params) {
        log.debug("Repository queryForSingleResult {}", new Object[]{query, params});
        T returnResult = (T) this.getJpa().txExpr(TransactionType.RequiresNew, entityManager -> {
            log.debug("Transaction found, invoke findAll");
            Query q = entityManager.createQuery(query, this.type);
            Iterator<String> it = params.keySet().iterator();
            while (it.hasNext()) {
                String paramName = it.next();
                q.setParameter(paramName, params.get(paramName));
            }
            try {
                Object result = q.getSingleResult();
                log.debug("Query result: " + result);
                return (T) result;
            } catch (NoResultException e) {
                log.debug(e.getMessage(), e);
                return null;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw e;
            }
        });

        if (returnResult == null)
            throw new NoResultException();
        return returnResult;
    }

    /**
     * Executes code inside a transaction without returning  result
     *
     * @param txType
     * @param function
     */
    public void executeTransaction(TransactionType txType, EmConsumer function) {
        getJpa().tx(txType, function);
    }

    /**
     * Executes code inside a transaction returning  result
     *
     * @param txType
     * @param function
     */
    public <R> R executeTransactionWithReturn(TransactionType txType, EmFunction<R> function) {
        return getJpa().txExpr(txType, function);
    }

    /**
     * Based on @UniqueConstriant hibernate annotation this method tries to check if
     * the entity is already present in the database without generating rollback
     * exception
     *
     * @param entity the entity which must be persisted or updated
     */
    private void checkDuplicate(T entity) {
        log.debug("Checking duplicates for entity {}", this.type.getName());
        Table[] tableAnnotation = entity.getClass().getAnnotationsByType(Table.class);
        if (tableAnnotation != null && tableAnnotation.length > 0) {
            UniqueConstraint[] uniqueConstraints = tableAnnotation[0].uniqueConstraints();
            if (uniqueConstraints != null && uniqueConstraints.length > 0) {
                for (int i = 0; i < uniqueConstraints.length; i++) {
                    String[] columnNames = uniqueConstraints[i].columnNames();
                    log.debug("Found UniqueConstraints {}", Arrays.toString(columnNames));
                    StringBuilder sb = new StringBuilder();
                    HashMap<String, Object> params = new HashMap<>();
                    for (int j = 0; j < columnNames.length; j++) {
                        String fieldName = columnNames[j];
                        String innerField = null;
                        // Field is a relationship, so we need to do 2 invocations
                        if (fieldName.contains("_")) {
                            String temp = fieldName;
                            fieldName = fieldName.substring(0, fieldName.indexOf("_"));
                            innerField = temp.substring(temp.indexOf("_") + 1);
                        }

                        String getterMethod = "get" + fieldName.substring(0, 1).toUpperCase()
                                + fieldName.substring(1);
                        try {
                            Method m = this.type.getMethod(getterMethod);
                            Object value = null;
                            // when inner field is null, the relative getter is invoked on the
                            // target entity
                            if (innerField == null)
                                value = m.invoke(entity);
                                // when inner field is != null then, the getter method is called on the
                                // related
                                // entity
                            else {
                                String getterInnerMethod = "get"
                                        + innerField.substring(0, 1).toUpperCase()
                                        + innerField.substring(1);
                                Object innerEntity = m.invoke(entity);
                                if (innerEntity != null) {
                                    Method innerMethod = innerEntity.getClass()
                                            .getMethod(getterInnerMethod);
                                    value = innerMethod.invoke(innerEntity);
                                } else {
                                    value = null;
                                }
                            }
                            // append only if innerMethod succeed
                            if (j > 0)
                                sb.append(" and ");
                            if (innerField == null) {
                                sb.append(fieldName).append("=").append(":").append(fieldName);
                                params.put(fieldName, value);
                            } else {
                                if (value != null) {
                                    sb.append(fieldName).append(".").append(innerField).append("=:")
                                            .append(fieldName).append(innerField);
                                    params.put(fieldName + innerField, value);
                                } else {
                                    sb.append(fieldName).append(".").append(innerField).append(" is null");
                                }
                            }
                        } catch (Exception e) {
                            log.error("Impossible to find getter method {}", new Object[]{getterMethod, this.type.getName(), e});
                        }
                    }
                    // executing the query
                    String query = "from " + this.type.getSimpleName() + " where " + sb.toString();
                    log.debug("Executing the query {} with parameters: {}", new Object[]{query, params.toString()});
                    try {
                        T result = this.queryForSingleResult(query, params);
                        // if the entity has not the same id than it's duplicated
                        if (result.getId() != entity.getId())
                            throw new HyperIoTDuplicateEntityException(columnNames);
                    } catch (NoResultException e) {
                        log.debug("Entity duplicate check passed!");
                    }
                }
            }
        }
    }

    /**
     * Automatically links tags and categories passed to entity
     *
     * @param entity
     * @param operation
     */
    protected void manageAssets(HyperIoTBaseEntity entity, AssetManagementOperation operation) {
        //manage categories for current entity
        this.manageAssetCategories(entity, operation);
        //manage tags for current entity
        this.manageAssetTags(entity, operation);
        //do recursively for inner entities which have tags associated
        this.manageAssetsForInnerEntities(entity, operation);
    }

    /**
     * Inner method that manages asset tag
     *
     * @param entity
     * @param operation
     */
    private void manageAssetCategories(HyperIoTBaseEntity entity, AssetManagementOperation operation) {
        HyperIoTAssetCategoryManager assetCategoryManager = getAssetCategoryManager();
        if (entity != null && assetCategoryManager != null) {
            long[] oldCategoryIds = assetCategoryManager.findAssetCategories(entity.getResourceName(), entity.getId());
            switch (operation) {
                case ADD:
                    if (entity.getCategoryIds() != null) {
                        assetCategoryManager
                                .addAssetCategories(entity.getResourceName(), entity.getId(), entity.getCategoryIds());
                    }
                    break;
                case UPDATE:
                    assetCategoryManager
                            .removeAssetCategories(entity.getResourceName(), entity.getId(), oldCategoryIds);
                    if (entity.getCategoryIds() != null) {
                        assetCategoryManager
                                .addAssetCategories(entity.getResourceName(), entity.getId(), entity.getCategoryIds());
                    }
                    break;
                case DELETE:
                    assetCategoryManager
                            .removeAssetCategories(entity.getResourceName(), entity.getId(), oldCategoryIds);
            }
        }
    }

    /**
     * Inner method that manages asset categories
     *
     * @param entity
     * @param operation
     */
    private void manageAssetTags(HyperIoTBaseEntity entity, AssetManagementOperation operation) {
        HyperIoTAssetTagManager assetTagManager = getAssetTagManager();
        if (entity != null && assetTagManager != null) {
            long[] oldTagIds = assetTagManager.findAssetTags(entity.getResourceName(), entity.getId());
            switch (operation) {
                case ADD:
                    if (entity.getTagIds() != null) {
                        assetTagManager
                                .addAssetTags(entity.getResourceName(), entity.getId(), entity.getTagIds());
                    }
                    break;
                case UPDATE:
                    assetTagManager
                            .removeAssetTags(entity.getResourceName(), entity.getId(), oldTagIds);
                    if (entity.getTagIds() != null) {
                        assetTagManager
                                .addAssetTags(entity.getResourceName(), entity.getId(), entity.getTagIds());
                    }
                    break;
                case DELETE:
                    assetTagManager
                            .removeAssetTags(entity.getResourceName(), entity.getId(), oldTagIds);
            }
        }
    }

    void manageAssetsForInnerEntities(HyperIoTBaseEntity entity, AssetManagementOperation operation) {
        if (entity != null) {
            //do recursively for inner fields
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field f : fields) {
                if (HyperIoTBaseEntity.class.isAssignableFrom(f.getType())) {
                    //forcing to read the field by reflection even if it's private
                    f.setAccessible(true);
                    try {
                        HyperIoTBaseEntity innerEntity = (HyperIoTBaseEntity) f.get(entity);
                        manageAssets(innerEntity, operation);
                    } catch (IllegalAccessException e) {
                        getLog().error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * @param beforeCrudAction Entity before Crud Action
     * @param afterCrudAction  Entity after Crud Action
     */
    private void invokePostUpdateDetailedAction(HyperIoTBaseEntity beforeCrudAction, HyperIoTBaseEntity afterCrudAction) {
        log.debug("Fetch post actions of type: PostUpdateAction");

        OSGiFilter osgiFilter = OSGiFilterBuilder.createFilter("type", beforeCrudAction.getClass().getName());

        //include OSGi filters for all interfaces implemented by resource and its superclasses
        String filter = ClassUtils.getAllInterfaces(beforeCrudAction.getClass())
                .stream()
                .map(interfaceClass -> OSGiFilterBuilder.createFilter("type", interfaceClass.getName()))
                .reduce(osgiFilter, OSGiFilter::or)
                .getFilter();

        ServiceReference<? extends HyperIoTPostUpdateDetailedAction>[] serviceReferences =
                HyperIoTUtil.getServices(HyperIoTPostUpdateDetailedAction.class, filter);
        if (serviceReferences == null)
            log.debug("There are not post actions of type post update action");
        else {
            log.debug("{} post actions fetched", serviceReferences.length);
            for (ServiceReference<? extends HyperIoTPostUpdateDetailedAction> serviceReference : serviceReferences)
                try {
                    log.debug("Executing post action: {}", serviceReference);
                    HyperIoTPostUpdateDetailedAction hyperIoTPostDetailedAction = HyperIoTUtil.getBundleContext(HyperIoTPostUpdateDetailedAction.class).getService(serviceReference);
                    hyperIoTPostDetailedAction.execute(beforeCrudAction, afterCrudAction);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
        }
    }

    /**
     * @param beforeCrudAction Entity before Crud Action
     * @param afterCrudAction  Entity after Crud Action
     */
    private void invokePreUpdateDetailedAction(HyperIoTBaseEntity beforeCrudAction, HyperIoTBaseEntity afterCrudAction) {
        log.debug("Fetch pre actions of type: PreUpdateAction");

        OSGiFilter osgiFilter = OSGiFilterBuilder.createFilter("type", beforeCrudAction.getClass().getName());

        //include OSGi filters for all interfaces implemented by resource and its superclasses
        String filter = ClassUtils.getAllInterfaces(beforeCrudAction.getClass())
                .stream()
                .map(interfaceClass -> OSGiFilterBuilder.createFilter("type", interfaceClass.getName()))
                .reduce(osgiFilter, OSGiFilter::or)
                .getFilter();

        ServiceReference<? extends HyperIoTPreUpdateDetailedAction>[] serviceReferences =
                HyperIoTUtil.getServices(HyperIoTPreUpdateDetailedAction.class, filter);
        if (serviceReferences == null)
            log.debug("There are not pre actions of type pre update action");
        else {
            log.debug("{} pre actions fetched", serviceReferences.length);
            for (ServiceReference<? extends HyperIoTPreUpdateDetailedAction> serviceReference : serviceReferences)
                try {
                    log.debug("Executing pre action: {}", serviceReference);
                    HyperIoTPreUpdateDetailedAction hyperIoTPreDetailedAction = HyperIoTUtil.getBundleContext(HyperIoTPreUpdateDetailedAction.class).getService(serviceReference);
                    hyperIoTPreDetailedAction.execute(beforeCrudAction, afterCrudAction);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
        }
    }

    @Override
    public int executeUpdateQuery(String query, HashMap<String, Object> params) {
        log.debug("Repository executeUpdateQuery {} {}", query, params);
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
            int result = -1;
            try {
                log.debug("Transaction found, invoke update query");
                Query q = entityManager.createQuery(query);
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    q.setParameter(entry.getKey(), entry.getValue());
                }
                result = q.executeUpdate();
                log.debug("Records affected: {}", result);
                return result;
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
                throw t;
            }
        });
    }

    @Override
    public long countAll(HyperIoTQuery filter) {
        log.debug("Repository countAll entities {}", this.type.getSimpleName());
        return this.getJpa().txExpr(TransactionType.RequiresNew,
                entityManager -> {
                    log.debug("Transaction found, invoke countAll");
                    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                    //constructing query and count query
                    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
                    Root<T> entityDefCount = countQuery.from(this.type);
                    Predicate conditionCount = (filter != null) ? filter.buildPredicate(criteriaBuilder, entityDefCount) : null;
                    countQuery = (conditionCount != null) ? countQuery.select(criteriaBuilder.count(entityDefCount)).where(conditionCount) : countQuery.select(criteriaBuilder.count(entityDefCount));
                    //Executing count query
                    Query countQueryFinal = entityManager.createQuery(countQuery);
                    return (Long) countQueryFinal.getSingleResult();
                });
    }

    /**
     * @return log of this class
     */
    protected Logger getLog() {
        return log;
    }

    protected enum AssetManagementOperation {
        ADD, DELETE, UPDATE;
    }

}
