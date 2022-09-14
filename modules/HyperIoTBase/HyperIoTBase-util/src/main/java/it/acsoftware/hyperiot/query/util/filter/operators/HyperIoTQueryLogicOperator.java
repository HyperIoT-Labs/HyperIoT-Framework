package it.acsoftware.hyperiot.query.util.filter.operators;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;

import java.util.Collection;

/**
 * This class maps the concept of generic logic operator
 */
public abstract class HyperIoTQueryLogicOperator implements HyperIoTQuery {
    @Override
    public HyperIoTQuery and(HyperIoTQuery operand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperIoTQuery or(HyperIoTQuery operand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HyperIoTQuery not() {
        throw new UnsupportedOperationException();
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
}
