package it.acsoftware.hyperiot.query.util.order;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTQueryOrder;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTQueryOrderParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Francesco Salerno
 */
public class HyperIoTQueryOrderImpl implements HyperIoTQueryOrder {

    private List<HyperIoTQueryOrderParameter> parametersList;

    HyperIoTQueryOrderImpl(){
        parametersList = new ArrayList<>();
    }

    @Override
    public HyperIoTQueryOrder addOrderField(String name, boolean asc){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException();
        }
        parametersList.add(new HyperIoTQueryOrderParameterImpl(name,asc));
        return this;
    }

    @Override
    public List<HyperIoTQueryOrderParameter> getParametersList(){
        return Collections.unmodifiableList(parametersList);
    }

    private final class HyperIoTQueryOrderParameterImpl implements  HyperIoTQueryOrderParameter{

        private String name ;
        private boolean asc;

        public HyperIoTQueryOrderParameterImpl(String name, boolean asc) {
            this.name = name;
            this.asc = asc;
        }

        public String getName() {
            return name;
        }

        public boolean isAsc() {
            return asc;
        }
    }

}
