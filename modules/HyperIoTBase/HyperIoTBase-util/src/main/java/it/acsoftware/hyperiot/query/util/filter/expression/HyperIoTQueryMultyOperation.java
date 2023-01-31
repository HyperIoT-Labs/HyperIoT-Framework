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

package it.acsoftware.hyperiot.query.util.filter.expression;

import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryField;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Abstract class which maps the concept of operation whihch needs multilpe values example: IN
 */
public abstract class HyperIoTQueryMultyOperation<T extends Collection<?>> extends HyperIoTQueryOperation {
    private T values;

    public HyperIoTQueryMultyOperation(HyperIoTQueryField field, T values) {
        super(field);
        this.values = values;
    }

    public T getValues() {
        return values;
    }

}
