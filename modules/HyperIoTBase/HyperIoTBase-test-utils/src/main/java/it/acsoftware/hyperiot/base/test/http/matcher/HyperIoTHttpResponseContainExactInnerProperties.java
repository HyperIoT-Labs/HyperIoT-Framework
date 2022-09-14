package it.acsoftware.hyperiot.base.test.http.matcher;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HyperIoTHttpResponseContainExactInnerProperties implements HyperIoTHttpResponseValidationCriteria {

    private List<String> properties;

    private String innerPropertyName;


    private HyperIoTHttpResponseContainExactInnerProperties(){

    }

    static HyperIoTHttpResponseContainExactInnerProperties factory(String innerPropertyName, List<String> properties){
        HyperIoTHttpResponseContainExactInnerProperties criteria = new HyperIoTHttpResponseContainExactInnerProperties();
        criteria.properties = new ArrayList<>();
        criteria.properties.addAll(properties);
        criteria.innerPropertyName = innerPropertyName;
        return criteria;
    }


    @Override
    public boolean validate(HyperIoTHttpResponse response) {
        return containsExactInnerProperty(response,innerPropertyName,this.properties);
    }

    private static boolean containsExactInnerProperty(HyperIoTHttpResponse response, String innerPropertyName, List<String> innerPropertyFields){
        JsonNode innerProperties = HyperIoTHttpValidationUtils.retrieveInnerPropertiesFromResponse(response,innerPropertyName);
        if(innerProperties == null){
            return false;
        }
        if (innerProperties instanceof ArrayNode) {
            ArrayNode nodeList = (ArrayNode) innerProperties;
            Iterator<JsonNode> nodeIterator = nodeList.iterator();
            while (nodeIterator.hasNext()) {
                JsonNode node = nodeIterator.next();
                if(! HyperIoTHttpValidationUtils.propertiesMatchesExactly(node, innerPropertyFields))
                    return false;
            }
            return true;
        }
        return HyperIoTHttpValidationUtils.propertiesMatchesExactly(innerProperties, innerPropertyFields);
    }

}
