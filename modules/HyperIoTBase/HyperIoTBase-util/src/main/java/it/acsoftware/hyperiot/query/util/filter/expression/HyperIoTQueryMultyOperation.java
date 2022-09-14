package it.acsoftware.hyperiot.query.util.filter.expression;

import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryField;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Abstract class which maps the concept of operation whihch needs multilpe values example: IN
 */
public abstract class HyperIoTQueryMultyOperation<T extends Collection<?>> extends HyperIoTQueryOperation {
    private T values;

    public HyperIoTQueryMultyOperation(HyperIoTQueryField field, T values) {
        super(field);
        this.values = values;
    }

    public T getValues() {
        return values;
    }

}
