/*
 * Copyright 2019-2023 HyperIoT
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
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HyperIoTHttpValidationUtils {

    private final static ObjectMapper mapper =  new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final static String PROPERTY_SEPARATOR = "\\.";

     public static JsonNode serializeResponseInJsonNode(HyperIoTHttpResponse response){
        JsonNode jsonNode ;
        try {
            jsonNode = mapper.readTree(response.getResponseBody());
        } catch (Exception exc) {
            throw new HyperIoTRuntimeException();
        }
        return jsonNode;
    }

    public static boolean propertiesMatches(JsonNode node, List<String> properties){
        for (String property : properties) {
            if (!node.has(property)) {
                return false;
            }
        }
        return true;
    }

    public static boolean propertiesMatchesExactly(JsonNode node, List<String> properties){
            Iterator<String> it = node.fieldNames();
            int propertiesResponseCounter = 0;
            while( it.hasNext() ){
                String property = it.next();
                propertiesResponseCounter++;
                if(! properties.contains(property)){
                    return false;
                }
            }
            return propertiesResponseCounter == properties.size();
    }

    public static JsonNode retrieveInnerPropertiesFromResponse(HyperIoTHttpResponse response, String innerPropertyName){
        String[] propertyPath = innerPropertyName.split(PROPERTY_SEPARATOR);
        JsonNode currentNode = HyperIoTHttpValidationUtils.serializeResponseInJsonNode(response);
        if(currentNode.isArray())
            return retrieveInnerPropertiesFromResponse((ArrayNode) currentNode, propertyPath );
        int i = 0 ;
        while( i < propertyPath.length){
            if(currentNode.isArray()){
                String[] propertyToScan = Arrays.copyOfRange(propertyPath, i, propertyPath.length);
                return retrieveInnerPropertiesFromResponse((ArrayNode) currentNode, propertyToScan);
            }
            if( ! currentNode.has(propertyPath[i]))
                return null;
            currentNode = currentNode.get(propertyPath[i]);
            i++;
        }
        return currentNode;

    }

    private static JsonNode retrieveInnerPropertiesFromResponse(ArrayNode node,String[] propertyPath){
        Iterator<JsonNode> it = node.iterator();
        ArrayNode innerPropertiesList = mapper.createArrayNode();
        while(it.hasNext()){
            JsonNode currentNode = it.next();
            ArrayNode gerarchy = mapper.createArrayNode();
            gerarchy.add(currentNode);
            int i = 0 ;
            while(i < propertyPath.length ) {
                if (!(currentNode.isArray()) &&  !(currentNode.has(propertyPath[i]))) {
                    return null;
                }
                if((currentNode.isArray()) && !(currentNode.has(propertyPath[i]))){
                    ArrayNode newGerarchy = mapper.createArrayNode();
                    for (JsonNode child : gerarchy){
                        newGerarchy.add(child);
                    }
                    gerarchy = newGerarchy;
                    currentNode = gerarchy.get(0);
                }
                else {
                    ArrayNode newGerarchy = mapper.createArrayNode();
                    for(JsonNode child : gerarchy){
                        if(! child.has(propertyPath[i])){
                            throw new HyperIoTRuntimeException("child " + child.textValue() + " has not property " + propertyPath[i]);
                        }
                        if( child.get(propertyPath[i]).isArray()){
                            for (JsonNode childElement : child.get(propertyPath[i])) {
                                newGerarchy.add(childElement);
                            }
                        }else{
                            newGerarchy.add(child.get(propertyPath[i]));
                        }
                    }
                    gerarchy = newGerarchy;
                    currentNode = gerarchy.get(0);
                    i++;
                }
            }
            innerPropertiesList.addAll(gerarchy);
        }
        return innerPropertiesList;
    }
}
