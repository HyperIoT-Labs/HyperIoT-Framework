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

package it.acsoftware.hyperiot.query.util.filter;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

/**
 * This class maps the concept of Query entity field
 */
public class HyperIoTQueryField<T> {

    private String name;

    public HyperIoTQueryField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public Path<?> getPath(Root<T> entityDef) {
        String[] dottedRelationships = name.split("\\.");
        Path<?> p = entityDef.get(dottedRelationships[0]);
        for (int i = 1; i < dottedRelationships.length; i++) {
            p = p.get(dottedRelationships[i]);
        }
        return p;
    }

}
