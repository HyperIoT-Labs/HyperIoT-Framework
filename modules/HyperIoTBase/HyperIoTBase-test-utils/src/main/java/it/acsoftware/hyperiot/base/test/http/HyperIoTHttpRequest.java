package it.acsoftware.hyperiot.base.test.http;

import org.apache.http.client.methods.HttpUriRequest;

import java.net.URI;

/**
 * @author Francesco Salerno
 * This class represent an HttpRequest.
 */
public class HyperIoTHttpRequest {

    private HttpUriRequest httpUriRequest;

    static HyperIoTHttpRequest build(HttpUriRequest httpUriRequest){
        HyperIoTHttpRequest request = new HyperIoTHttpRequest();
        request.httpUriRequest = httpUriRequest;
        return request;
    }

    private HyperIoTHttpRequest(){

    }

    /**
     * @return the http method of the request
     */
    public String getMethod(){
        return this.httpUriRequest.getMethod();
    }

    /**
     * @return the uri of the request
     */
    public URI getURI(){
        return this.httpUriRequest.getURI();
    }

    /**
     * Such method is used for internal scope by HyperIoTHttpClient.
     * @return the HttpUriRequest
     */
    HttpUriRequest getRequest(){
        return this.httpUriRequest;
    }


}
