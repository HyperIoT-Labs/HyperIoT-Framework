package it.acsoftware.hyperiot.query.util.filter.expression;

import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryField;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * This class maps the concept of IN operation
 */
public class HyperIoTQueryInOperation extends HyperIoTQueryMultyOperation {

    public HyperIoTQueryInOperation(HyperIoTQueryField field, Collection values) {
        super(field, values);
    }

    @Override
    public <T> Predicate doOperation(CriteriaBuilder criteriaBuilder, Root<T> entityDef) {
        return this.getField().getPath(entityDef).in(getValues());
    }
}
