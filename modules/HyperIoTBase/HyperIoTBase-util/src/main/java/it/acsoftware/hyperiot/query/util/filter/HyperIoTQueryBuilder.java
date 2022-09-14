package it.acsoftware.hyperiot.query.util.filter;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Query Builder helps to construcr query starting from maps or the create new one from scratch.
 */
public class HyperIoTQueryBuilder {

    /**
     * @return
     */
    public static HyperIoTQuery newQuery() {
        return createQueryFrom(null);
    }


    /**
     * Creates a filter from an hashmap using and condition for default
     *
     * @param filter fieldName,value pair for generating condition
     * @return
     */
    public static HyperIoTQuery fromMapInOrCondition(HashMap<String, Object> filter) {
        return createFromMap(filter, false);
    }

    /**
     * Creates a filter from an hashmap using and condition for default
     * @param filter fieldName,value pair for generating condition
     * @return
     */
    public static HyperIoTQuery fromMapInAndCondition(HashMap<String, Object> filter) {
        return createFromMap(filter, true);
    }


    /**
     * Creates query from an existing Query
     * @param filter
     * @return
     */
    private static HyperIoTQuery createQueryFrom(HyperIoTQuery filter) {
        return new HyperIoTQueryImpl(filter);
    }

    /**
     * This method iterates over the HashMap and create the composite structure of the query
     * @param filter
     * @param inAnd
     * @return
     */
    private static HyperIoTQuery createFromMap(HashMap<String, Object> filter, boolean inAnd) {
        HyperIoTQuery query = null;
        if (filter != null && filter.size() > 0) {
            Iterator<String> it = filter.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                HyperIoTQuery eqQuery = newQuery().equals(key, filter.get(key));
                if (query == null)
                    query = eqQuery;
                else if (inAnd)
                    query = query.and(eqQuery);
                else
                    query = query.or(eqQuery);
            }
        }
        return query;
    }

}
