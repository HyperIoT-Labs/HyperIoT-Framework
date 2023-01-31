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

package it.acsoftware.hyperiot.base.api.entity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Interface which maps the concept of a Domain Query.
 * Use HyperIoTQuery for fast query single entity with multiple conditions.
 * Do not use in case of articulate query or joins.
 */
public interface HyperIoTQuery {

    HyperIoTQuery and(HyperIoTQuery operand);

    HyperIoTQuery or(HyperIoTQuery operand);

    HyperIoTQuery not();

    HyperIoTQuery equals(String name, Object value);

    HyperIoTQuery like(String name, String value);

    HyperIoTQuery notEquals(String name, Object value);

    HyperIoTQuery greaterThan(String name, Object value);

    HyperIoTQuery greaterOrEqual(String name, Object value);

    HyperIoTQuery lessThan(String name, Object value);

    HyperIoTQuery lessOrEqual(String name, Object value);

    <C extends Collection<?>> HyperIoTQuery in(String name, C values);

    <T extends HyperIoTBaseEntity> Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Root<T> entityDef);
}

