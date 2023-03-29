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

package it.acsoftware.hyperiot.websocket.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HyperIoTWebSocketMessage {
    public static final String WS_MESSAGE_SENDER_PARAM_NAME = "sender";

    @JsonIgnore
    private static Logger log = LoggerFactory.getLogger("it.acsofware.hyperiot");
    @JsonIgnore
    private static ObjectMapper mapper = new ObjectMapper();

    private String cmd;
    private byte[] payload;
    private String contentType;
    private Date timestamp;
    private HyperIoTWebSocketMessageType type;
    private HashMap<String, String> params;

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public HyperIoTWebSocketMessage() {
        this.params = new HashMap<>();
        this.contentType = "text/plain";
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public HyperIoTWebSocketMessageType getType() {
        return type;
    }

    public void setType(HyperIoTWebSocketMessageType type) {
        this.type = type;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public static HyperIoTWebSocketMessage createMessage(String cmd, byte[] payload, HyperIoTWebSocketMessageType type) {
        HyperIoTWebSocketMessage m = new HyperIoTWebSocketMessage();
        m.setTimestamp(new Date());
        m.setCmd(cmd);
        m.setPayload(payload);
        m.setType(type);
        return m;
    }

    public static HyperIoTWebSocketMessage fromString(String message) {
        try {
            return mapper.readValue(message, HyperIoTWebSocketMessage.class);
        } catch (Throwable t) {
            log.debug("Error while parsing websocket message: {}", new Object[]{t.getMessage()});
        }
        return null;
    }

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return "{}";
    }
}
