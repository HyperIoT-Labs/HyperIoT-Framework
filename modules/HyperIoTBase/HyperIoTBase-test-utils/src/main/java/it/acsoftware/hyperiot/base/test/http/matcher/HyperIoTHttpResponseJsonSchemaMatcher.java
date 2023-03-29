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
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.test.http.HyperIoTHttpResponse;

import java.util.Set;

public class HyperIoTHttpResponseJsonSchemaMatcher implements HyperIoTHttpResponseValidationCriteria{

    private Class<? extends HyperIoTBaseEntity> baseEntity;

    private HyperIoTHttpResponseJsonSchemaMatcher(){

    }

    static HyperIoTHttpResponseJsonSchemaMatcher factory(Class<? extends HyperIoTBaseEntity> baseEntity){
        HyperIoTHttpResponseJsonSchemaMatcher matcher = new HyperIoTHttpResponseJsonSchemaMatcher();
        matcher.baseEntity = baseEntity;
        return matcher;

    }

    @Override
    public boolean validate(HyperIoTHttpResponse response) {
        return validateHttpResponseBodyRespectBaseEntitySchema(response);
    }

    private boolean validateHttpResponseBodyRespectBaseEntitySchema(HyperIoTHttpResponse response){
        JsonSchema schema = generateJsonSchemaFromHyperIoTBaseEntity();
        JsonNode jsonResponseBody = HyperIoTHttpValidationUtils.serializeResponseInJsonNode(response);
        Set<ValidationMessage> validationMessageSet = schema.validate(jsonResponseBody);
        return validationMessageSet.size() == 0 ;
    }

    private JsonSchema generateJsonSchemaFromHyperIoTBaseEntity(){
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);
        JsonNode jsonSchema = jsonSchemaGenerator.generateJsonSchema(this.baseEntity);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        return factory.getSchema(jsonSchema);

    }

}
