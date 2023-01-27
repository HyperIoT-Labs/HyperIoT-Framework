/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.base.test.http.matcher;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HyperIoTHttpResponseContainInnerProperties implements HyperIoTHttpResponseValidationCriteria {

    private List<String> properties;

    private String innerPropertyName;

    private HyperIoTHttpResponseContainInnerProperties(){

    }

    static HyperIoTHttpResponseContainInnerProperties factory(String innerPropertyName, List<String> properties){
        HyperIoTHttpResponseContainInnerProperties criteria = new HyperIoTHttpResponseContainInnerProperties();
        criteria.properties = new ArrayList<>();
        criteria.properties.addAll(properties);
        criteria.innerPropertyName = innerPropertyName;
        return criteria;
    }


    @Override
    public boolean validate(HyperIoTHttpResponse response) {
        return containsInnerProperty(response,innerPropertyName,this.properties);
    }

    private static boolean containsInnerProperty(HyperIoTHttpResponse response, String innerPropertyName, List<String> innerPropertyFields){
        JsonNode innerProperties = HyperIoTHttpValidationUtils.retrieveInnerPropertiesFromResponse(response,innerPropertyName);
        if(innerProperties == null){
            return false;
        }
        if (innerProperties instanceof ArrayNode) {
            ArrayNode nodeList = (ArrayNode) innerProperties;
            Iterator<JsonNode> nodeIterator = nodeList.iterator();
            while (nodeIterator.hasNext()) {
                JsonNode node = nodeIterator.next();
                if(! HyperIoTHttpValidationUtils.propertiesMatches(node, innerPropertyFields))
                    return false;
            }
            return true;
        }
        return HyperIoTHttpValidationUtils.propertiesMatches(innerProperties, innerPropertyFields);
    }

    private static JsonNode retrieveInnerPropertiesFromResponse(HyperIoTHttpResponse response, String innerPropertyName){
        String[] propertyPath = innerPropertyName.split("\\.");
        JsonNode currentNode = HyperIoTHttpValidationUtils.serializeResponseInJsonNode(response);
        int i = 0 ;
        while( i < propertyPath.length){
            if( ! currentNode.has(propertyPath[i]))
                return null;
            currentNode = currentNode.get(propertyPath[i]);
            i++;
        }
        return currentNode;
    }


}
