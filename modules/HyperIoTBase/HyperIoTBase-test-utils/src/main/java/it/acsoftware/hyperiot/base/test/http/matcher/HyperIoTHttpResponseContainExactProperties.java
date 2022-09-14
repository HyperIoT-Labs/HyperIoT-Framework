package it.acsoftware.hyperiot.base.test.http.matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HyperIoTHttpResponseContainExactProperties implements HyperIoTHttpResponseValidationCriteria {

    private List<String> properties;

    private HyperIoTHttpResponseContainExactProperties(){

    }

    static HyperIoTHttpResponseContainExactProperties factory(List<String> properties){
        HyperIoTHttpResponseContainExactProperties criteria = new HyperIoTHttpResponseContainExactProperties();
        criteria.properties = new ArrayList<>();
        criteria.properties.addAll(properties);
        return criteria;
        }

    @Override
    public boolean validate(HyperIoTHttpResponse response) {
        return containExactProperty(response,this.properties);
    }

    /**
     * @param response the HyperIoTHttpResponse to validate
     * @param properties the name of the properties list expected.
     * @return true if and only if the response object contain only the specified properties.
     */
    private  boolean containExactProperty(HyperIoTHttpResponse response , List<String> properties){
        JsonNode jsonResponse = HyperIoTHttpValidationUtils.serializeResponseInJsonNode(response);
        if (jsonResponse instanceof ArrayNode) {
            ArrayNode nodeList = (ArrayNode) jsonResponse;
            Iterator<JsonNode> nodeIterator = nodeList.iterator();
            while (nodeIterator.hasNext()) {
                JsonNode node = nodeIterator.next();
                if(! HyperIoTHttpValidationUtils.propertiesMatchesExactly(node,properties)){
                    return false;
                }
            }
            return true;
        }
        return HyperIoTHttpValidationUtils.propertiesMatchesExactly(jsonResponse, properties);
    }

}
