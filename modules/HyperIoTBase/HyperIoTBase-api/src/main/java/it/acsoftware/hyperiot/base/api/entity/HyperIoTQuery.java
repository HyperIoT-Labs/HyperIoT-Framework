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

