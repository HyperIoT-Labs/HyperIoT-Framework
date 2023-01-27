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

package it.acsoftware.hyperiot.kafka.connector.producer;

import it.acsoftware.hyperiot.base.util.HyperIoTThreadFactoryBuilder;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaConnectorSystemApi;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaProducerPool;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @Author Aristide Cittadino
 */
public class KafkaProducerPoolImpl implements KafkaProducerPool {
    private static Logger log = LoggerFactory.getLogger(KafkaProducerPoolImpl.class);

    private Map<String, KafkaProducer<byte[], byte[]>> producerPool;
    private Iterator<KafkaProducer<byte[], byte[]>> producerIterator;
    private KafkaProducer<byte[], byte[]> currentProducer;
    private ExecutorService producerPoolExecutor;
    private KafkaConnectorSystemApi kafkaConnectorSystemApi;

    public KafkaProducerPoolImpl(int poolSize, KafkaConnectorSystemApi kafkaConnectorSystemApi) {
        this.kafkaConnectorSystemApi = kafkaConnectorSystemApi;
        producerPool = Collections.synchronizedMap(new HashMap<>());
        for (int i = 0; i < poolSize; i++) {
            String uuid = UUID.randomUUID().toString();
            producerPool.put(uuid, this.kafkaConnectorSystemApi.getNewProducer(uuid));
        }
        ThreadFactory producerPoolFac = HyperIoTThreadFactoryBuilder.build("hyperiot-kafka-system-thread-pool-%d", false);
        producerPoolExecutor = Executors.newFixedThreadPool(poolSize, producerPoolFac);
        resetIterator();
        nextProducer();
    }

    private synchronized void resetIterator() {
        producerIterator = this.producerPool.values().iterator();
    }

    private synchronized void nextProducer() {
        if (!producerIterator.hasNext())
            resetIterator();
        else
            currentProducer = producerIterator.next();
    }

    public synchronized void send(HyperIoTKafkaMessage message) {
        this.send(message, null);
    }

    public synchronized void send(HyperIoTKafkaMessage message, Callback callback) {
        Runnable r = () -> {
            this.kafkaConnectorSystemApi.produceMessage(message, currentProducer, null);
        };
        producerPoolExecutor.execute(r);
        nextProducer();
    }

    @Override
    public void shutdown() {
        producerPool.values().stream().forEach(producer -> {
            try {
                producer.close();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        });
    }
}
