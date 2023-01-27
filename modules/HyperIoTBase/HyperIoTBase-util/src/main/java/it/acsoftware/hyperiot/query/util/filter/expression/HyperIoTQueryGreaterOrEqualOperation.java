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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

/**
 * This class maps the concept of greater or equal operation
 */
public class HyperIoTQueryGreaterOrEqualOperation extends HyperIoTQueryUnaryOperation {

    public HyperIoTQueryGreaterOrEqualOperation(HyperIoTQueryField field, Object value) {
        super(field, value);
    }

    @Override
    public <T> Predicate doOperation(CriteriaBuilder criteriaBuilder, Root<T> entityDef) {
        Object value = getValue();
        if (value == null)
            return null;
        else if (value instanceof Long) {
            return criteriaBuilder.greaterThanOrEqualTo(this.getField().getPath(entityDef), (Long) getValue());
        } else if (value instanceof Integer) {
            return criteriaBuilder.greaterThanOrEqualTo(this.getField().getPath(entityDef), (Integer) getValue());
        } else if (value instanceof Float) {
            return criteriaBuilder.greaterThanOrEqualTo(this.getField().getPath(entityDef), (Float) getValue());
        } else if (value instanceof Double) {
            return criteriaBuilder.greaterThanOrEqualTo(this.getField().getPath(entityDef), (Double) getValue());
        } else if (value instanceof Date) {
            return criteriaBuilder.greaterThanOrEqualTo(this.getField().getPath(entityDef), (Date) getValue());
        } else if (value instanceof String) {
            return criteriaBuilder.greaterThanOrEqualTo(this.getField().getPath(entityDef), (String) getValue());
        } else
            throw new RuntimeException("Invalid type for query operation: " + value.getClass().getName());
    }
}
