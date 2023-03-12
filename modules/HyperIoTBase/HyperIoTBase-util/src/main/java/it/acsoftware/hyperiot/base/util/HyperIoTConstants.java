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

package it.acsoftware.hyperiot.base.util;

/**
 * @author Aristide Cittadino Model class for HyperIoTConstants. It is used to
 * define all OSGi constants, authorization constant, and JWT token
 * constant of HyperIoT platform.
 */
public class HyperIoTConstants {
    public static final String OSGI_ACTION_RESOURCE_NAME = "it.acsoftware.hyperiot.action.resource";
    public static final String OSGI_ACTION_RESOURCE_CATEGORY = "it.acsoftware.hyperiot.action.category";
    public static final String OSGI_ACTION_NAME = "it.acsoftware.hyperiot.action.name";
    public static final String OSGI_PERMISSION_MANAGER_IMPLEMENTATION = "it.acsoftware.hyperiot.permissionManager.implementation";
    public static final String OSGI_PERMISSION_MANAGER_IMPLEMENTATION_DEFAULT = "default";
    public static final String OSGI_AUTH_PROVIDER_RESOURCE = "it.acsoftware.hyperiot.authentication.provider.resource";
    public static final String OSGI_WEBSOCKET_CHANNEL_ROLE_NAME = "it.acsoftware.hyperiot.websocket.channel.model.role.name";
    public static final String HYPERIOT_PROPERTY_TEST_MODE = "it.acsoftware.hyperiot.testMode";
    public static final String HYPERIOT_PROPERTY_NODE_ID = "it.acsoftware.hyperiot.nodeId";
    public static final String HYPERIOT_PROPERTY_LAYER = "it.acsoftware.hyperiot.layer";
    public static final String HYPERIOT_PROPERTY_SERVICES_URL = "it.acsoftware.hyperiot.services.url";
    public static final String HYPERIOT_PROPERTY_FRONTEND_URL = "it.acsoftware.hyperiot.frontend.url";
    public static final String HYPERIOT_PROPERTY_ACCOUNT_ACTIVATION_ENABLED = "it.acsoftware.hyperiot.activateAccount.enable";
    public static final String HYPERIOT_PROPERTY_ACTIVATE_ACCOUNT_URL = "it.acsoftware.hyperiot.activateAccount.url";
    public static final String HYPERIOT_PROPERTY_PASSWORD_RESET_URL = "it.acsoftware.hyperiot.resetPassword.url";
    public static final String HYPERIOT_PROPERTY_BASE_REST_CONTEXT = "it.acsoftware.hyperiot.base.rest.context";
    public static final String HYPERIOT_PROPERTY_WEB_SOCKER_SERVICE_ON_OPEN_DISPATCH_THREADS = "it.acsoftware.hyperiot.websocket.onopen.dispatch.threads";
    public static final String HYPERIOT_PROPERTY_WEB_SOCKER_SERVICE_ON_CLOSE_DISPATCH_THREADS = "it.acsoftware.hyperiot.websocket.onclose.dispatch.threads";
    public static final String HYPERIOT_PROPERTY_WEB_SOCKER_SERVICE_ON_MESSAGE_DISPATCH_THREADS = "it.acsoftware.hyperiot.websocket.onmessage.dispatch.threads";
    public static final String HYPERIOT_PROPERTY_WEB_SOCKER_SERVICE_ON_ERROR_DISPATCH_THREADS = "it.acsoftware.hyperiot.websocket.onerror.dispatch.threads";
    public static final String HYPERIOT_AUTHORIZATION_COOKIE_NAME = "HIT-AUTH";
    public static final String HYPERIOT_BRIDGED_WEB_SOCKET_EXPECTED_KEY_PARAM = "hit-ws-bridge-expected-key";
    public static final String HYPERIOT_BRIDGED_WEB_SOCKET_KEY_PARAM = "hit-ws-bridge-key";

    public static final String HYPERIOT_WEB_SOCKET_CHANNEL_ID_PARAM = "hyt-ws-channel-id";
    public static final String HYPERIOT_WEB_SOCKET_CHANNEL_COMMAND_PARAM = "hyt-ws-channel-command";

    public static final String HYPERIOT_JWT_TOKEN_ROLES_PROPERTY_NAME = "roles";

    public static final String HYPERIOT_CONFIG_FILE_NAME = "it.acsoftware.hyperiot";
    public static final String HYPERIOT_KAFKA_CONNECTOR_CONFIG_FILE_NAME = "it.acsoftware.hyperiot.kafka.connector";
    public static final String HYPERIOT_KAFKA_CONNECTOR_WS_CHANNEL_CLUSTER_MESSAGE_BROKER_CONFIG_FILE_NAME = "it.acsoftware.hyperiot.kafka.connector.ws.channel.cluster.message.broker";
    public static final String HYPERIOT_ZOOKEEPER_CONNECTOR_CONFIG_FILE_NAME = "it.acsoftware.hyperiot.zookeeper.connector";
    public static final String HYPERIOT_MAIL_CONFIG_FILE_NAME = "it.acsoftware.hyperiot.mail";
    public static final String HYPERIOT_JWT_CONFIG_FILE_NAME = "it.acsoftware.hyperiot.jwt";

    public static final int HYPERIOT_DEFAULT_PAGINATION_DELTA = 10;
}
