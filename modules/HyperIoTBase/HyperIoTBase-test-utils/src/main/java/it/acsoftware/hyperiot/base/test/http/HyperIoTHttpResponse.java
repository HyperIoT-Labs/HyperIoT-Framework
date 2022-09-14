package it.acsoftware.hyperiot.base.test.http;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author FrancescoSalerno
 * This class represent  a model of an HttpResponse.
 */
public class HyperIoTHttpResponse {

    private int responseStatus;

    private String responseBody;

    private final static ObjectMapper mapper =  new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    HyperIoTHttpResponse(){

    }

    public int getResponseStatus() {
        return responseStatus;
    }

    void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public <T> T serializeResponse(Class<T> classToSerialize) throws JsonProcessingException {
        return mapper.readValue(responseBody, classToSerialize);
    }

    public <T> T serializeResponse(Class<T> classToSerialize, ObjectMapper customMapper) throws JsonProcessingException {
        return customMapper.readValue(responseBody,classToSerialize);
    }
}

