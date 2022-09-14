package it.acsoftware.hyperiot.query.util.filter.operators;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;
import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * This class maps the concept of "or" operator
 */
public class HyperIoTQueryOrOperator extends HyperIoTQueryBinaryLogicOperator {

    public HyperIoTQueryOrOperator(HyperIoTQuery left, HyperIoTQuery right) {
        super(left, right);
    }

    @Override
    public <T extends HyperIoTBaseEntity> Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Root<T> entityDef) {
        return criteriaBuilder.or(getLeftOp().buildPredicate(criteriaBuilder, entityDef), getRightOp().buildPredicate(criteriaBuilder, entityDef));
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
}
