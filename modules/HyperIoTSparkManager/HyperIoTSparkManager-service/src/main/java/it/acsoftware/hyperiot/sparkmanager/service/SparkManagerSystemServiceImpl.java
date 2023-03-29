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

package it.acsoftware.hyperiot.sparkmanager.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl;
import it.acsoftware.hyperiot.sparkmanager.util.SparkManagerUtil;
import it.acsoftware.hyperiot.sparkmanager.api.SparkManagerSystemApi;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiResponse;
import it.acsoftware.hyperiot.sparkmanager.model.SparkRestApiSubmissionRequest;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;

/**
 * @author Aristide Cittadino Implementation class of the SparkManagerSystemApi
 * interface. This  class is used to implements all additional
 * methods to interact with the persistence layer.
 */
@Component(service = SparkManagerSystemApi.class, immediate = true)
public final class SparkManagerSystemServiceImpl extends HyperIoTBaseSystemServiceImpl
        implements SparkManagerSystemApi {

    private ObjectMapper objectMapper;
    private CloseableHttpClient sparkJobserverRestClient;
    private String sparkManagerUrl;

    @Activate
    public void activate() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                connectionManager.setMaxTotal(100);
                connectionManager.setDefaultMaxPerRoute(20);
                sparkJobserverRestClient = HttpClients.custom().setConnectionManager(connectionManager).build();
                SparkManagerSystemServiceImpl.this.objectMapper = new ObjectMapper();
                SparkManagerSystemServiceImpl.this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                sparkManagerUrl = SparkManagerUtil.getSparkMasterHostname() + ":"
                        + SparkManagerUtil.getSparkRestApiPort();
            }
        });
        t.start();
    }

    @Override
    public SparkRestApiResponse getStatus(String driverId) {
        try {
            String sparkManagerUrl = SparkManagerUtil.getSparkMasterHostname() + ":"
                    + SparkManagerUtil.getSparkRestApiPort();
            HttpGet httpGet = new HttpGet(sparkManagerUrl + "/v1/submissions/status/" + driverId);
            CloseableHttpResponse response = sparkJobserverRestClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            String res = EntityUtils.toString(responseEntity);
            // map json to Object
            EntityUtils.consume(responseEntity);
            return objectMapper.readValue(res, SparkRestApiResponse.class);
        } catch (IOException e) {
            throw new HyperIoTRuntimeException(e.getMessage());
        }
    }

    @Override
    public SparkRestApiResponse kill(String driverId) {
        try {
            String sparkManagerUrl = SparkManagerUtil.getSparkMasterHostname() + ":"
                    + SparkManagerUtil.getSparkRestApiPort();
            HttpPost httpPost = new HttpPost(sparkManagerUrl + "/v1/submissions/kill/" + driverId);
            CloseableHttpResponse response = sparkJobserverRestClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String res = EntityUtils.toString(responseEntity);
            // map json to Object
            EntityUtils.consume(responseEntity);
            return objectMapper.readValue(res, SparkRestApiResponse.class);
        } catch (IOException e) {
            throw new HyperIoTRuntimeException(e.getMessage());
        }
    }

    @Override
    public SparkRestApiResponse submitJob(SparkRestApiSubmissionRequest data) {
        try {
            HttpPost httpPost = new HttpPost(sparkManagerUrl + "/v1/submissions/create");
            JSONObject payload = new JSONObject(data);
            httpPost.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = sparkJobserverRestClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String res = EntityUtils.toString(responseEntity);
            // map json to Object
            EntityUtils.consume(responseEntity);
            return objectMapper.readValue(res, SparkRestApiResponse.class);
        } catch (IOException e) {
            throw new HyperIoTRuntimeException(e.getMessage());
        }
    }

}
