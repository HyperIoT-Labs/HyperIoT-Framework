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

public enum HyperIoTWebSocketMessageType {
    AUDIO_DATA("AUDIO"),
    VIDEO_DATA("VIDEO"),
    APPLICATION("APPLICATION"),
    CONNECTION_OK("CONNECTION_OK"),
    DISCONNECTING("DISCONNECTING"),
    OK("OK"),
    ERROR("ERROR"),
    RESULT("RESULT"),
    HEARTBEAT("HEARTBEAT"),
    INFO("INFO"),
    PARTICIPANT_ADDED("PARTICIPANT_ADDED"),
    PARTICIPANT_GONE("PARTICIPANT_GONE"),
    PARTICIPANT_KICKED("PARTICIPANT_KICKED"),
    PARTICIPANT_BANNED("PARTICIPANT_BANNED"),
    PARTICIPANT_UNBANNED("PARTICIPANT_UNBANNED"),
    PING("PING"),
    PONG("PONG"),
    WEBSOCKET_POLICY_WARNING("WEB_SOCKET_POLICY_WARNING"),
    SET_ENCRYPTION_KEY("SET_ENCRYPTION_KEY"),
    SET_CHANNEL_ENCRYPTION_KEY("SET_CHANNEL_ENCRYPTION_KEY"),
    WARNING("WARNING");
    private String type;

    private HyperIoTWebSocketMessageType(String type) {
        this.type = type;
    }
}
