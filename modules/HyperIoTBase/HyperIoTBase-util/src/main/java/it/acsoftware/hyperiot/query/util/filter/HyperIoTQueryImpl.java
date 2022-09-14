package it.acsoftware.hyperiot.query.util.filter;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;
import it.acsoftware.hyperiot.query.util.filter.expression.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Implementation of Generic Query.
 * This Class is used to compose the query by invoking methods consequently.
 */
public final class HyperIoTQueryImpl implements HyperIoTQuery {

    private HyperIoTQuery query;

    public HyperIoTQueryImpl(HyperIoTQuery filter) {
        this.query = filter;
    }

    public HyperIoTQuery getQuery() {
        return query;
    }

    public HyperIoTQuery and(HyperIoTQuery operand) {
        query = query.and(operand);
        return query;
    }

    public HyperIoTQuery or(HyperIoTQuery operand) {
        query = query.or(operand);
        return query;
    }

    public HyperIoTQuery not() {
        query = query.not();
        return query;
    }


    public HyperIoTQuery equals(String name, Object value) {
        HyperIoTQueryField field = new HyperIoTQueryField(name);
        HyperIoTQueryOperation op = new HyperIoTQueryEqualOperation(field, value);
        this.query = new HyperIoTQueryExpression(op);
        return query;
    }

    
    public HyperIoTQuery like(String name, String value) {
        HyperIoTQueryField field = new HyperIoTQueryField(name);
        HyperIoTQueryOperation op = new HyperIoTQueryLikeOperation(field, value);
        this.query = new HyperIoTQueryExpression(op);
        return query;
    }

    public HyperIoTQuery notEquals(String name, Object value) {
        HyperIoTQueryField field = new HyperIoTQueryField(name);
        HyperIoTQueryOperation op = new HyperIoTQueryEqualOperation(field, value);
        this.query = new HyperIoTQueryExpression(op).not();
        return query;
    }

    public HyperIoTQuery greaterThan(String name, Object value) {
        HyperIoTQueryField field = new HyperIoTQueryField(name);
        HyperIoTQueryOperation op = new HyperIoTQueryGreaterThanOperation(field, value);
        this.query = new HyperIoTQueryExpression(op);
        return query;
    }

    public HyperIoTQuery greaterOrEqual(String name, Object value) {
        HyperIoTQueryField field = new HyperIoTQueryField(name);
        HyperIoTQueryOperation op = new HyperIoTQueryGreaterOrEqualOperation(field, value);
        this.query = new HyperIoTQueryExpression(op);
        return query;
    }

    public HyperIoTQuery lessThan(String name, Object value) {
        HyperIoTQueryField field = new HyperIoTQueryField(name);
        HyperIoTQueryOperation op = new HyperIoTLessThanOperation(field, value);
        this.query = new HyperIoTQueryExpression(op);
        return query;
    }

    public HyperIoTQuery lessOrEqual(String name, Object value) {
        HyperIoTQueryField field = new HyperIoTQueryField(name);
        HyperIoTQueryOperation op = new HyperIoTLessOrEqualOperation(field, value);
        this.query = new HyperIoTQueryExpression(op);
        return query;
    }

    public <T extends Collection<?>> HyperIoTQuery in(String name, T values) {
        HyperIoTQueryField field = new HyperIoTQueryField(name);
        HyperIoTQueryOperation op = new HyperIoTQueryInOperation(field, values);
        this.query = new HyperIoTQueryExpression(op);
        return query;
    }

    @Override
    public <T extends HyperIoTBaseEntity> Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Root<T> entityDef) {
        return this.query.buildPredicate(criteriaBuilder, entityDef);
    }
}
