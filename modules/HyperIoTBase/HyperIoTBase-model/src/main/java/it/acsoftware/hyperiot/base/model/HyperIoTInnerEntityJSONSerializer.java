package it.acsoftware.hyperiot.base.model;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Author Aristide Cittadino
 * This serializer is used yo serialize inner entity in relationship.
 * In this way user can manage with Compact JSON View what kind of infomation should be visibile inside the relationship
 */
public class HyperIoTInnerEntityJSONSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithView(HyperIoTJSONView.Compact.class).writeValue(gen, value);
    }
}
