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
