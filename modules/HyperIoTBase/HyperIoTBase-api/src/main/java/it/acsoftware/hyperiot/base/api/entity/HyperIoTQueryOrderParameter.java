package it.acsoftware.hyperiot.base.api.entity;

/**
 * @author Francesco Salerno
 */
public interface HyperIoTQueryOrderParameter {

    /**
     *
     * @return parameter Name
     */
    String getName();

    /**
     *
     * @return true if specify AscendingOrder
     */
    boolean isAsc();
}
