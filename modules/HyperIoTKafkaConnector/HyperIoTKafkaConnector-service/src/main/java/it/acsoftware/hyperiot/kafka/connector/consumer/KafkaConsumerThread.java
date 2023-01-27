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

package it.acsoftware.hyperiot.kafka.connector.consumer;

import it.acsoftware.hyperiot.kafka.connector.api.KafkaMessageReceiver;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;
import it.acsoftware.hyperiot.kafka.connector.util.HyperIoTKafkaConnectorConstants;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilter;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author Aristide Cittadino Consumer Thread which receives message from Kafka
 * and notify OSGi components for the received messages. OSGi components
 * will be notified looking at their OSGi properties.
 */
public class KafkaConsumerThread implements Runnable {
    private static Logger log = LoggerFactory.getLogger(KafkaConsumerThread.class.getName());
    private boolean consume;
    private final Consumer<byte[], byte[]> consumer;
    private BundleContext ctx;
    private List<String> topics;
    private Properties systemConsumerProperties;

    private ExecutorService notifierExecutor;

    /**
     * @param props Kafka Connection Properties
     * @param ctx   OSGi bundle context
     */
    public KafkaConsumerThread(Properties props, Properties systemConsumerProperties, BundleContext ctx, List<String> topics, ExecutorService notifierExecutor) {
        super();
        log.debug("Setting consumer properties {}", props.toString());
        this.ctx = ctx;
        this.consume = true;
        this.systemConsumerProperties = systemConsumerProperties;
        this.notifierExecutor = notifierExecutor;
        consumer = new KafkaConsumer<>(props);
        this.topics = topics;
    }

    /**
     * Run method for thread
     */
    @Override
    public void run() {
        if (topics != null && topics.size() > 0) {
            log.debug("Registering to these topics: {}", topics.toString());
            consumer.subscribe(topics);
            log.debug("Start polling Data...");
            //default duration, overridable from it.acsoftware.hyperiot.kafka.system.consumer.poll.ms prop
            long pollDurationMillis = 500;
            try {
                pollDurationMillis = Long.parseLong((String) systemConsumerProperties.get(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_SYSTEM_CONSUMER_POLL_MS));
            } catch (Exception e) {
                log.error("Error while setting custom poll duration milliseconds, please check kafka connector cfg file: {}", e.getMessage());
            }
            while (consume) {
                try {
                    final ConsumerRecords<byte[], byte[]> consumerRecords = consumer
                            .poll(Duration.ofMillis(pollDurationMillis));
                    for (TopicPartition partition : consumerRecords.partitions()) {
                        List<ConsumerRecord<byte[], byte[]>> partitionRecords = consumerRecords
                                .records(partition);
                        for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {
                            String topic = record.topic();
                            byte[] key = record.key();
                            byte[] payload = record.value();
                            log.debug("Got message from Kafka on topic: {}", topic);
                            HyperIoTKafkaMessage message = HyperIoTKafkaMessage.from(topic, key, payload);
                            this.notifyKafkaMessage(message);
                        }
                        long lastOffset = partitionRecords.get(partitionRecords.size() - 1)
                                .offset();
                        consumer.commitSync(Collections.singletonMap(partition,
                                new OffsetAndMetadata(lastOffset + 1)));
                    }
                } catch (WakeupException e) {
                    log.info("Waking up Kafka consumer, for other use...");
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                }
            }
            this.consumer.close();
            log.info("Kafka Consumer stopped.");
        } else {
            log.warn("ATTENTION: no topic defined for Kafka consumer");
        }

    }

    /**
     * @param message Message that must be sent
     */
    public void notifyKafkaMessage(HyperIoTKafkaMessage message) {
        Runnable notifyKafkaMessage = () -> {
            KafkaGloabalNotifier.notifyKafkaMessage(message);
        };
        notifierExecutor.execute(notifyKafkaMessage);
    }


    /**
     * Stop message consumption from Kafka
     */
    public void stop() {
        log.info("Shutting down kafka Consumer...");
        this.consumer.wakeup();
        this.consume = false;
    }

}
