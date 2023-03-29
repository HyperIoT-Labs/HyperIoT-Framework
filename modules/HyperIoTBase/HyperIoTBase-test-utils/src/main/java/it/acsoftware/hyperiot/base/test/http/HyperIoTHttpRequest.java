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
