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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author  Francesco Salerno
 * This class represent an helper to build an HttpResponse.
 */
public class HyperIoTHttpRequestBuilder {

    private final static Logger log = LoggerFactory.getLogger(HyperIoTHttpRequestBuilder.class.getName());

    private RequestBuilder requestBuilder;

    private final static ObjectMapper mapper =  new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private HyperIoTHttpRequestBuilder(RequestBuilder requestBuilder){
        this.requestBuilder= requestBuilder;
    }

    private HyperIoTHttpRequestBuilder(){

    }

    /**
     * @param method the http method used related to the request.
     * @return HyperIoTHttpRequestBuilder
     */
    public static HyperIoTHttpRequestBuilder create(final String method) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.create(method));
    }

    /**
     * @return HyperIoTHttpRequestBuilder with get method.
     */
    public static HyperIoTHttpRequestBuilder get() {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.get());
    }

    /**
     * @param uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with get method.
     */
    public static HyperIoTHttpRequestBuilder get(final URI uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.get().setUri(uri));
    }

    /**
     * @param uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with get method.
     */
    public static HyperIoTHttpRequestBuilder get(final String uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.get().setUri(uri));
    }

    /**
     * @return HyperIoTHttpRequestBuilder with head method.
     */
    public static HyperIoTHttpRequestBuilder head() {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.head());
    }

    /**
     * @param  uri the uri of the request.
     * @return HyperIoTHttpRequestBuilder with head method.
     */
    public static HyperIoTHttpRequestBuilder head(final URI uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.head().setUri(uri));
    }

    /**
     * @param  uri the uri of the request.
     * @return HyperIoTHttpRequestBuilder with head method.
     */
    public static HyperIoTHttpRequestBuilder head(final String uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.head().setUri(uri));
    }

    /**
     * @return HyperIoTHttpRequestBuilder with post method.
     */
    public static HyperIoTHttpRequestBuilder post() {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.post());
    }

    /**
     * @param uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with post method.
     */
    public static HyperIoTHttpRequestBuilder post(final URI uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.post().setUri(uri));
    }

    /**
     * @param uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with post method.
     */
    public static HyperIoTHttpRequestBuilder post(final String uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.post().setUri(uri));
    }

    /**
     * @return HyperIoTHttpRequestBuilder with put method.
     */
    public static HyperIoTHttpRequestBuilder put() {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.put());
    }

    /**
     * @param uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with put method.
     */
    public static HyperIoTHttpRequestBuilder put(final URI uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.put().setUri(uri));
    }

    /**
     * @param uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with put method.
     */
    public static HyperIoTHttpRequestBuilder put(final String uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.put().setUri(uri));
    }

    /**
     * @return HyperIoTHttpRequestBuilder with put method.
     */
    public static HyperIoTHttpRequestBuilder delete() {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.delete());
    }

    /**
     * @param  uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with put method.
     */
    public static HyperIoTHttpRequestBuilder delete(final URI uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.delete().setUri(uri));
    }

    /**
     * @param  uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with put method.
     */
    public static HyperIoTHttpRequestBuilder delete(final String uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.delete().setUri(uri));
    }

    /**
     * @return HyperIoTHttpRequestBuilder with trace method.
     */
    public static HyperIoTHttpRequestBuilder trace() {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.trace());
    }

    /**
     * @param  uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with trace method.
     */
    public static HyperIoTHttpRequestBuilder trace(final URI uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.trace().setUri(uri));
    }

    /**
     * @param  uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with trace method.
     */
    public static HyperIoTHttpRequestBuilder trace(final String uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.trace().setUri(uri));
    }

    /**
     * @return HyperIoTHttpRequestBuilder with options method.
     */
    public static HyperIoTHttpRequestBuilder options() {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.options());
    }

    /**
     * @param  uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with options method.
     */
    public static HyperIoTHttpRequestBuilder options(final URI uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.options().setUri(uri));
    }

    /**
     * @param  uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with options method.
     */
    public static HyperIoTHttpRequestBuilder options(final String uri) {
        return new HyperIoTHttpRequestBuilder(RequestBuilder.options().setUri(uri));
    }

    /**
     * @param  uri the uri of the request
     * @return HyperIoTHttpRequestBuilder with uri of the request
     */
    public  HyperIoTHttpRequestBuilder withUri(String uri) {
        this.requestBuilder.setUri(uri);
        return this;
    }

    /**
     * @param  hyperIoTUser the HyperIoTUser used to authenticate to the request.
     * @return HyperIoTHttpRequestBuilder with authorization header.
     */
    public  HyperIoTHttpRequestBuilder withAuthorization(HyperIoTUser hyperIoTUser){
        this.requestBuilder.setHeader(HttpHeaders.AUTHORIZATION,HyperIoTHttpUtils.getJwtToken(hyperIoTUser));
        return this;
    }

    /**
     * @param  username the username used of the HyperIoTUser to which authenticate the request
     * @param  password the password used of the HyperIoTUser to which authenticate the request
     * @return HyperIoTHttpRequestBuilder with authorization header.
     */
    public HyperIoTHttpRequestBuilder withAuthorization(String username, String password){
        this.requestBuilder.setHeader(HttpHeaders.AUTHORIZATION, HyperIoTHttpUtils.getJwtToken(username, password));
        return this;
    }

    /**
     * @param  authToken the token used to authenticate request
     * @return HyperIoTHttpRequestBuilder with authorization header.
     */
    public HyperIoTHttpRequestBuilder withAuthorizationHeader(String authToken){
        this.requestBuilder.setHeader(HttpHeaders.AUTHORIZATION, authToken);
        return this;
    }

    /**
     * This service build the request such that it's authorized as an HyperIoTAdmin.
     * @return HyperIoTHttpRequestBuilder with authorization header.
     */
    public HyperIoTHttpRequestBuilder withAuthorizationAsHyperIoTAdmin(){
        this.requestBuilder.setHeader(HttpHeaders.AUTHORIZATION, HyperIoTHttpUtils.getJwtTokenAsAdmin());
        return this;
    }

    /**
     * @param contentType the value of the content type header.
     * @return HyperIoTHttpRequestBuilder with ContentType header
     */
    public HyperIoTHttpRequestBuilder withContentTypeHeader(String contentType){
        this.requestBuilder.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        return this;
    }

    /**
     * @param accept the value of the Accept header.
     * @return HyperIoTHttpRequestBuilder with Accept header
     */
    public HyperIoTHttpRequestBuilder withAcceptHeader(String accept){
        this.requestBuilder.setHeader(HttpHeaders.ACCEPT, accept);
        return this;
    }

    /**
     * @param name the name of the query parameter
     * @param value the value of the query parameter.
     * @return HyperIoTHttpRequestBuilder with query parameter
     */
    public HyperIoTHttpRequestBuilder withParameter(String name, String value){
        this.requestBuilder.addParameter(name, value);
        return this;
    }

    /**
     * @param requestBody the body of the request.
     * @return HyperIoTHttpRequestBuilder with json body
     */
    public HyperIoTHttpRequestBuilder withJsonBody(String requestBody){
        StringEntity serializedEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        this.requestBuilder.setEntity(serializedEntity);
        return this;
    }

    /**
     * @param baseEntity HyperIoTBaseEntity used to create the body of the request.
     * @return HyperIoTHttpRequestBuilder with json body
     */
    public HyperIoTHttpRequestBuilder withJsonBody(HyperIoTBaseEntity baseEntity){
        try {
            return this.withJsonBody(mapper.writeValueAsString(baseEntity));
        }  catch (JsonProcessingException e) {
            log.debug(e.getMessage(), e);
            throw new HyperIoTRuntimeException();
        }
    }

    /**
     * @param baseEntity HyperIoTBaseEntity used to create the body of the request.
     * @param customMapper the ObjectMapper used to serialize the body of the request.
     * @return HyperIoTHttpRequestBuilder with json body
     */
    public HyperIoTHttpRequestBuilder withJsonBody(HyperIoTBaseEntity baseEntity, ObjectMapper customMapper){
        try {
            return this.withJsonBody(mapper.writeValueAsString(baseEntity));
        }  catch (JsonProcessingException e) {
            log.debug(e.getMessage(), e);
            throw new HyperIoTRuntimeException();
        }
    }

    /**
     * @return HyperIoTHttpRequest .
     */
    public HyperIoTHttpRequest build(){
        return HyperIoTHttpRequest.build(requestBuilder.build());
    }
}
