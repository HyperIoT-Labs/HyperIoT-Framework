package it.acsoftware.hyperiot.base.api.entity;

import java.util.List;

/**
 * @author Francesco Salerno
 */
public interface HyperIoTQueryOrder {

    /**
     *
     * @param name the name of the parameter which is used to order the query's result.
     * @param asc true if you want to use ascending order.
     */
    HyperIoTQueryOrder addOrderField(String name, boolean asc);

    /**
     *
     * @return parameters list.
     */
    List<HyperIoTQueryOrderParameter> getParametersList();


}
