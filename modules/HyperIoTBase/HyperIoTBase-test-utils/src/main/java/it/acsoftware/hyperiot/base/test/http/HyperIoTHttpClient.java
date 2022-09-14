package it.acsoftware.hyperiot.base.test.http;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * @author Francesco Salerno
 * This class represent an HttpClient.
 */
public class HyperIoTHttpClient {

    private final static Logger log = LoggerFactory.getLogger(HyperIoTHttpClient.class.getName());

    private HyperIoTHttpClient(){

    }

    /**
     * Factory method used to create an HyperIoTHttpClient
     * @return HyperIoTHttpClient
     */
    public static HyperIoTHttpClient hyperIoTHttpClient(){
        return new HyperIoTHttpClient();
    }

    /**
     * @param request the HyperIoTHttp request to execute.
     * @return The HyperIoTHttpResponse
     */
    public HyperIoTHttpResponse execute(HyperIoTHttpRequest request){
        try (final CloseableHttpClient client = httpClientWithBasicConfiguration()) {
            try (final CloseableHttpResponse response =  client.execute(request.getRequest())){
                return HyperIoTHttpResponseBuilder
                        .hyperIoTHttpResponseBuilder()
                        .status(response.getStatusLine().getStatusCode())
                        .body(EntityUtils.toString(response.getEntity()))
                        .build();
            }
        } catch (Exception exc){
            log.debug(exc.getMessage(), exc);
            throw new HyperIoTRuntimeException();
        }
    }

    /**
     *
     * @return a CloseableHttpClient with the default configuration.
     */
    private static CloseableHttpClient httpClientWithBasicConfiguration(){
        return HttpClients.createDefault();
    }



}
