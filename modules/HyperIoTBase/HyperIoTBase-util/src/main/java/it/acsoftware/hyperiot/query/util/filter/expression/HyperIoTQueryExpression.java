package it.acsoftware.hyperiot.query.util.filter.expression;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;
import it.acsoftware.hyperiot.query.util.filter.operators.HyperIoTQueryAndOperator;
import it.acsoftware.hyperiot.query.util.filter.operators.HyperIoTQueryNotOperator;
import it.acsoftware.hyperiot.query.util.filter.operators.HyperIoTQueryOrOperator;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * This class maps the concept of query expression <field> op <value>
 * Example: id = 3.
 */
public class HyperIoTQueryExpression implements HyperIoTQuery {

    private HyperIoTQueryOperation operation;

    public HyperIoTQueryExpression(HyperIoTQueryOperation operation) {
        this.operation = operation;
    }

    @Override
    public HyperIoTQuery and(HyperIoTQuery operand) {
        return new HyperIoTQueryAndOperator(this, operand);
    }

    @Override
    public HyperIoTQuery or(HyperIoTQuery operand) {
        return new HyperIoTQueryOrOperator(this, operand);
    }

    @Override
    public HyperIoTQuery not() {
        return new HyperIoTQueryNotOperator(this);
    }


    @Override
    public HyperIoTQuery equals(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperIoTQuery like(String name, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperIoTQuery notEquals(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperIoTQuery greaterThan(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperIoTQuery greaterOrEqual(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperIoTQuery lessThan(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperIoTQuery lessOrEqual(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C extends Collection<?>> HyperIoTQuery in(String name, C values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends HyperIoTBaseEntity> Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Root<T> entityDef) {
        return this.operation.doOperation(criteriaBuilder, entityDef);
    }
}
