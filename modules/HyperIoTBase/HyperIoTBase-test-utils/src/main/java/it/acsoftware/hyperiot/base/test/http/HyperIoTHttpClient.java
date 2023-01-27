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
