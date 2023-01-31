/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
