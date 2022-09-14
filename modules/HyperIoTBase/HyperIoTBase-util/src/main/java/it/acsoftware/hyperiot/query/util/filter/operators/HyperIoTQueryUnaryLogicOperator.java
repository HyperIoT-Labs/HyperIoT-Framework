package it.acsoftware.hyperiot.query.util.filter.operators;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;

/**
 * This class maps the concept of generic unary operator like "not"
 */
public abstract class HyperIoTQueryUnaryLogicOperator extends HyperIoTQueryLogicOperator {

    private HyperIoTQuery operand;

    public HyperIoTQueryUnaryLogicOperator(HyperIoTQuery operand) {
        this.operand = operand;
    }

    public HyperIoTQuery getOperand() {
        return operand;
    }
}
