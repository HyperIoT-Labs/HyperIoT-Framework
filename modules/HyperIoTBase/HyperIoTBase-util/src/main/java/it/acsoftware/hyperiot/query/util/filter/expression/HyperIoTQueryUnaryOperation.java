package it.acsoftware.hyperiot.query.util.filter.expression;

import it.acsoftware.hyperiot.query.util.filter.HyperIoTQueryField;

/**
 * This class maps the concept of single value operation like equal or greater than
 */
public abstract class HyperIoTQueryUnaryOperation<T> extends HyperIoTQueryOperation {

    private T value;

    public HyperIoTQueryUnaryOperation(HyperIoTQueryField field, T value) {
        super(field);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

}
