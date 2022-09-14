package it.acsoftware.hyperiot.query.util.filter.expression;

import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryField;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * This class maps the concept of Query operation like equal, greater than, ecc...
 */
public abstract class HyperIoTQueryOperation {
    private HyperIoTQueryField field;

    public HyperIoTQueryOperation(HyperIoTQueryField field) {
        this.field = field;
    }

    public HyperIoTQueryField getField() {
        return field;
    }

    public abstract <T> Predicate doOperation(CriteriaBuilder criteriaBuilder, Root<T> entityDef);
}
