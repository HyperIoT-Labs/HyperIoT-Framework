package it.acsoftware.hyperiot.query.util.filter.expression;

import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryField;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

/**
 * This class maps the concept of Less operation
 */
public class HyperIoTLessThanOperation extends HyperIoTQueryUnaryOperation {

    public HyperIoTLessThanOperation(HyperIoTQueryField field, Object value) {
        super(field, value);
    }

    @Override
    public <T> Predicate doOperation(CriteriaBuilder criteriaBuilder, Root<T> entityDef) {
        Object value = getValue();
        if(value == null){
            return null;
        } else if (value instanceof Long) {
            return criteriaBuilder.lessThan(this.getField().getPath(entityDef), (Long) getValue());
        } else if (value instanceof Integer) {
            return criteriaBuilder.lessThan(this.getField().getPath(entityDef), (Integer) getValue());
        } else if (value instanceof Float) {
            return criteriaBuilder.lessThan(this.getField().getPath(entityDef), (Float) getValue());
        } else if (value instanceof Double) {
            return criteriaBuilder.lessThan(this.getField().getPath(entityDef), (Double) getValue());
        } else if (value instanceof Date) {
            return criteriaBuilder.lessThan(this.getField().getPath(entityDef), (Date) getValue());
        } else if (value instanceof String) {
            return criteriaBuilder.lessThan(this.getField().getPath(entityDef), (String) getValue());
        }
        throw new RuntimeException("Invalid type for query operation!");
    }
}
