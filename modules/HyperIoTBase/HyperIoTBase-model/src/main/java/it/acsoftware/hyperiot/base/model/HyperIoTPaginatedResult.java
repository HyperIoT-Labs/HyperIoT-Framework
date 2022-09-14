package it.acsoftware.hyperiot.base.model;

import java.util.Collection;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTPaginableResult;

/**
 * @param <T>
 * @author Aristide Cittadino Class used to return paginated results on query
 */
public class HyperIoTPaginatedResult<T extends HyperIoTBaseEntity>
        implements HyperIoTPaginableResult<T> {
    /**
     * Num pages
     */
    public int numPages;
    /**
     * Current page
     */
    public int currentPage;
    /**
     * Next page
     */
    public int nextPage;
    /**
     * Num of items per page
     */
    public int delta;
    /**
     * Query results
     */
    public Collection<T> results;

    public HyperIoTPaginatedResult(int numPages, int currentPage, int nextPage, int delta,
                                   Collection<T> results) {
        super();
        this.numPages = numPages;
        this.currentPage = currentPage;
        this.nextPage = nextPage;
        this.delta = delta;
        this.results = results;
    }

    /**
     * @return
     */
    public int getNumPages() {
        return numPages;
    }


    /**
     * @return the current page
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * @return the next page
     */
    public int getNextPage() {
        return nextPage;
    }

    /**
     * @return items per page
     */
    public int getDelta() {
        return delta;
    }

    /**
     * @return query results
     */
    public Collection<T> getResults() {
        return results;
    }

}
