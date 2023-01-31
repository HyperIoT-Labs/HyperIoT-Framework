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

package it.acsoftware.hyperiot.kafka.connector.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import it.acsoftware.hyperiot.kafka.connector.util.HyperIoTKafkaConnectorConstants;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaConnectorSystemApi;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaMessageReceiver;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;

/**
 * 
 * @author Aristide Cittadino
 * Example class how to register a message receiver
 */
@Component(service = KafkaMessageReceiver.class, property = {
		HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_OSGI_TOPIC_FILTER
				+ "=hyperiot_layer_microservices_1" })
public class ProvaKafkaListener implements KafkaMessageReceiver {
	private KafkaConnectorSystemApi kafkaConnectorSystemApi;
	
	@Override
	public void receive(HyperIoTKafkaMessage message) {
		System.out.println("Message received: " + message.toString());

	}

	protected KafkaConnectorSystemApi getKafkaConnectorSystemApi() {
		return kafkaConnectorSystemApi;
	}

	@Reference
	protected void setKafkaConnectorSystemApi(KafkaConnectorSystemApi kafkaConnectorSystemApi) {
		this.kafkaConnectorSystemApi = kafkaConnectorSystemApi;
	}


}
