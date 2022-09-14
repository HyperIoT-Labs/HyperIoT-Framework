package it.acsoftware.hyperiot.base.api.entity;

import java.util.Collection;

/**
 * @param <T>
 * @author Aristide Cittadino
 * Abstraction for a paginable result as a query output
 */
public interface HyperIoTPaginableResult<T extends HyperIoTBaseEntity> {
    /**
     * @return num of pages
     */
    public int getNumPages();

    /**
     * @return the current page
     */
    public int getCurrentPage();

    /**
     * @return the next page
     */
    public int getNextPage();

    /**
     * @return items per page
     */
    public int getDelta();

    /**
     * @return query results
     */
    public Collection<T> getResults();
}
