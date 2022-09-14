package it.acsoftware.hyperiot.query.util.filter.operators;


import it.acsoftware.hyperiot.base.api.entity.HyperIoTQuery;

/**
 * This class maps the concept of generic binary operator like and,or...
 */
public abstract class HyperIoTQueryBinaryLogicOperator extends HyperIoTQueryLogicOperator {
    private HyperIoTQuery leftOp;
    private HyperIoTQuery rightOp;

    public HyperIoTQueryBinaryLogicOperator(HyperIoTQuery leftOp, HyperIoTQuery rightOp) {
        this.leftOp = leftOp;
        this.rightOp = rightOp;
    }

    public HyperIoTQuery getLeftOp() {
        return leftOp;
    }

    public HyperIoTQuery getRightOp() {
        return rightOp;
    }
}
