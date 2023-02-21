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

package it.acsoftware.hyperiot.kafka.connector.api;

import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aristide Cittadino
 * Interfaces that must be implemented from components that wants to be notified when a message from Kafka arrives.
 * When register the component, an OSGi property (or more) must be declared to identify the topic from which the component wants to be notified.
 * <p>
 * For example: @Component(property={"it.acsoftware.hyperiot.kafka.topic=dataSaved"} will be notified only for kafka topic "/dataSaved"
 * Or if you want to filter also Kafka keys "key1" and "key2" on "dataSave" topic
 * @Component(property={"it.acsoftware.hyperiot.kafka.topic=dataSaved",it.acsoftware.hyperiot.kafka.key=dataSave:key1,it.acsoftware.hyperiot.kafka.key=dataSave:key2}
 */
public interface KafkaMessageReceiver {
    /**
     * Method invoked by System when a kafka message arrvived on the specified topic
     *
     * @param message
     */
    void receive(HyperIoTKafkaMessage message);
}
