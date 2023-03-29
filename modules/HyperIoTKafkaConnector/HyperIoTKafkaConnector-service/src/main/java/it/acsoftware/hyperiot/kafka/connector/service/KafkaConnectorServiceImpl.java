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

package it.acsoftware.hyperiot.kafka.connector.service;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;
import it.acsoftware.hyperiot.kafka.connector.actions.KafkaConnectorAction;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaConnectorApi;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaConnectorSystemApi;
import it.acsoftware.hyperiot.kafka.connector.model.ConnectorConfig;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaPermission;
import it.acsoftware.hyperiot.kafka.connector.model.KafkaConnector;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteAclsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Aristide Cittadino
 * Implementation class of KafkaConnectorApi
 */
@Component(service = KafkaConnectorApi.class, immediate = true)
public class KafkaConnectorServiceImpl extends HyperIoTBaseServiceImpl implements KafkaConnectorApi {
    public static final String KAFKA_CONNECTOR_RESOURCE_NAME = "it.acsoftware.hyperiot.kafka.connector.model.KafkaConnector";
    private KafkaConnectorSystemApi systemApi;

    public KafkaConnectorServiceImpl() {
        super();
    }

    @Override
    protected KafkaConnectorSystemApi getSystemService() {
        return systemApi;
    }

    @Reference
    public void setSystemApi(KafkaConnectorSystemApi systemApi) {
        this.systemApi = systemApi;
    }

    /**
     * TO DO: manage reactive consumer. Kafka authorization should be managed on Kafka ACLs
     *
     * @param kafkaGroupId           Kafka group identifier
     * @param topics                 List of topic names to consume from
     * @param keyDeserializerClass   Key deserializer class
     * @param valueDeserializerClass Value deserializer class
     * @return Flux
     * @throws ClassNotFoundException In case deserializer not found
     */
    @SuppressWarnings("rawtypes")
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.CONSUME_FROM_TOPIC, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, List<String> topics, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException {
        return systemApi.consumeReactive(kafkaGroupId, topics, pollTime, keyDeserializerClass, valueDeserializerClass);
    }

    /**
     * TO DO: manage reactive consumer. Kafka authorization should be managed on Kafka ACLs
     *
     * @param kafkaGroupId           kafka GroupId
     * @param topic                  Kafka Topic
     * @param partition              Partition from which data must be consumed
     * @param keyDeserializerClass   Key Deserializer class
     * @param valueDeserializerClass Value Deserializer class
     * @return Flux
     * @throws ClassNotFoundException In case deserializer not found
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.CONSUME_FROM_TOPIC, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public Flux<ReceiverRecord<byte[], byte[]>> consumeReactive(String kafkaGroupId, String topic, long pollTime, int partition, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException {
        return systemApi.consumeReactive(kafkaGroupId, topic, partition, pollTime, keyDeserializerClass, valueDeserializerClass);
    }

    /**
     * @param kafkaGroupId           kafka GroupId
     * @param topicPattern           Kafka Topic Pattern
     * @param keyDeserializerClass   Key Deserializer class
     * @param valueDeserializerClass Value Deserializer class
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.CONSUME_FROM_TOPIC, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public void consumeReactiveAsSystem(String kafkaGroupId, Pattern topicPattern, long pollTime, Class keyDeserializerClass, Class valueDeserializerClass) throws ClassNotFoundException {
        systemApi.consumeReactiveAsSystem(kafkaGroupId, topicPattern, pollTime, keyDeserializerClass, valueDeserializerClass);
    }


    /**
     * @param topic         Topic name which must be created
     * @param numPartitions Number of partitions to assign to that topic
     * @param numReplicas   Number of replicas to assing to that topic
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_TOPICS_ADD, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public CreateTopicsResult adminCreateTopic(HyperIoTContext context, String topic, int numPartitions, short numReplicas) {
        return this.getSystemService().adminCreateTopic(topic, numPartitions, numReplicas);
    }


    /**
     * @param topics        Array of topic that must be created
     * @param numPartitions Array in which in the same position in topic array it must be preset the relative numPartitions
     * @param numReplicas   Array in which in the same position in topic array it must be present the relative numReplicas
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_TOPICS_ADD, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public CreateTopicsResult adminCreateTopic(HyperIoTContext context, String[] topics, int[] numPartitions, short[] numReplicas) {
        return this.getSystemService().adminCreateTopic(topics, numPartitions, numReplicas);
    }

    /**
     * @param topics Topic to be dropped
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_TOPICS_DELETE, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public DeleteTopicsResult adminDropTopic(HyperIoTContext context, List<String> topics) {
        return this.getSystemService().adminDropTopic(topics);
    }

    /**
     * @param username
     * @param permissions
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_ACL_ADD, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public CreateAclsResult adminAddACLs(HyperIoTContext context, String username, Map<String, HyperIoTKafkaPermission> permissions) {
        return this.getSystemService().adminAddACLs(username, permissions);
    }

    /**
     * @param username
     * @param permissions
     * @return
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_ACL_DELETE, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public DeleteAclsResult adminDeleteACLs(HyperIoTContext context, String username, Map<String, HyperIoTKafkaPermission> permissions) {
        return this.getSystemService().adminDeleteACLs(username, permissions);
    }

    /**
     * @param instanceName
     * @param config
     * @return
     * @throws IOException
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_CONNECTOR_NEW, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public KafkaConnector addNewConnector(HyperIoTContext context, String instanceName, ConnectorConfig config) throws IOException {
        return this.getSystemService().addNewConnector(instanceName, config);
    }

    /**
     * @param instanceName
     * @param deleteKafkaTopic
     * @throws IOException
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_CONNECTOR_DELETE, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public void deleteConnector(HyperIoTContext context, String instanceName, boolean deleteKafkaTopic) throws IOException {
        this.getSystemService().deleteConnector(instanceName, deleteKafkaTopic);
    }

    /**
     * @param instanceName
     * @return
     * @throws IOException
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_CONNECTOR_VIEW, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public KafkaConnector getConnector(HyperIoTContext context, String instanceName) throws IOException {
        return this.getSystemService().getConnector(instanceName);
    }

    /**
     * @return
     * @throws IOException
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_CONNECTOR_LIST, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public List<String> listConnectors(HyperIoTContext context) throws IOException {
        return this.getSystemService().listConnectors();
    }

    /**
     * @param instanceName
     * @param config
     * @return
     * @throws IOException
     */
    @Override
    @AllowGenericPermissions(actions = KafkaConnectorAction.Names.ADMIN_KAFKA_CONNECTOR_UPDATE, resourceName = KAFKA_CONNECTOR_RESOURCE_NAME)
    public KafkaConnector updateConnector(HyperIoTContext context, String instanceName, ConnectorConfig config) throws IOException {
        return this.getSystemService().updateConnector(instanceName, config);
    }
}
