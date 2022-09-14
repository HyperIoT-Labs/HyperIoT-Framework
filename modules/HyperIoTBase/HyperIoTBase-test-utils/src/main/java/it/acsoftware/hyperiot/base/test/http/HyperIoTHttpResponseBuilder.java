package it.acsoftware.hyperiot.base.test.http;

/**
 * @author Francesco Salerno
 * This classe represente a builder of an http response.
 * This is used by HyperIoTHttpClient as internal utility to build an HttpResponse.
 */
public class HyperIoTHttpResponseBuilder {

    private HyperIoTHttpResponse response;

    private HyperIoTHttpResponseBuilder(){

    }

    static HyperIoTHttpResponseBuilder hyperIoTHttpResponseBuilder(){
        HyperIoTHttpResponseBuilder builder = new HyperIoTHttpResponseBuilder();
        builder.response = new HyperIoTHttpResponse();
        return builder;
    }

    HyperIoTHttpResponseBuilder status(int status){
        this.response.setResponseStatus(status);
        return this;
    }

    HyperIoTHttpResponseBuilder body(String body){
        this.response.setResponseBody(body);
        return this;
    }

    HyperIoTHttpResponse build(){
        return this.response;
    }
}
