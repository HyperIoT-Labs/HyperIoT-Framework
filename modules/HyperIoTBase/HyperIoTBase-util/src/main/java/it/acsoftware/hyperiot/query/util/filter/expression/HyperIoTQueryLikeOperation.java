package it.acsoftware.hyperiot.query.util.filter.expression;

import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryField;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * This class maps the concept of equal operation
 */
public class HyperIoTQueryLikeOperation extends HyperIoTQueryUnaryOperation {
    public HyperIoTQueryLikeOperation(HyperIoTQueryField field, Object value) {
        super(field, value);
    }

    @Override
    public <T> Predicate doOperation(CriteriaBuilder criteriaBuilder, Root<T> entityDef) {
        return criteriaBuilder.like(this.getField().getPath(entityDef), (String)getValue());
    }
}
