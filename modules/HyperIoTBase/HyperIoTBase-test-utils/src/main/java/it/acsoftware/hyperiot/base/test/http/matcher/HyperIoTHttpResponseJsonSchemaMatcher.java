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
