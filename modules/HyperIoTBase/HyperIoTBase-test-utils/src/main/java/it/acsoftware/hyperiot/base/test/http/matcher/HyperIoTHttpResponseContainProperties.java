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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HyperIoTHttpResponseContainProperties implements HyperIoTHttpResponseValidationCriteria {

    private List<String> properties;

    private HyperIoTHttpResponseContainProperties(){

    }

    static HyperIoTHttpResponseContainProperties factory(List<String> properties){
        HyperIoTHttpResponseContainProperties criteria = new HyperIoTHttpResponseContainProperties();
        criteria.properties = new ArrayList<>();
        criteria.properties.addAll(properties);
        return criteria;
    }

    @Override
    public boolean validate(HyperIoTHttpResponse response) {
        return containsProperty(response,this.properties);
    }

    /**
     * @param response the HyperIoTHttpResponse to validate
     * @param properties the name of the properties list expected.
     * @return true if and only if the response object contain the specified properties.
     */
    private static boolean containsProperty(HyperIoTHttpResponse response, List<String> properties) {
        JsonNode jsonResponse = HyperIoTHttpValidationUtils.serializeResponseInJsonNode(response);
        if (jsonResponse instanceof ArrayNode) {
            ArrayNode nodeList = (ArrayNode) jsonResponse;
            Iterator<JsonNode> nodeIterator = nodeList.iterator();
            while (nodeIterator.hasNext()) {
                JsonNode node = nodeIterator.next();
                if(! HyperIoTHttpValidationUtils.propertiesMatches(node, properties))
                    return false;
            }
            return true;
        }
        return HyperIoTHttpValidationUtils.propertiesMatches(jsonResponse, properties);
    }

}
