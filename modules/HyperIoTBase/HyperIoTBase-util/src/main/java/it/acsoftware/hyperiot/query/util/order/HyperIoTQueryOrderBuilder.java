package it.acsoftware.hyperiot.query.util.order;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTQueryOrder;

public class HyperIoTQueryOrderBuilder {

    public static HyperIoTQueryOrder createQueryOrder(){
        return new HyperIoTQueryOrderImpl();
    }

}
