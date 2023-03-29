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

package it.acsoftware.hyperiot.kafka.connector.model;

/**
 * 
 * @author Aristide Cittadino
 * Class that maps the concept of "message" from Kafka exposed via Rest service.
 */
public class HyperIoTKafkaRestMessage {
	/**
	 * Kafka key
	 */
	private String key;
	
	/**
	 * Kafka payload
	 */
	private String content;
	
	public HyperIoTKafkaRestMessage(String key, String content) {
		super();
		this.key = key;
		this.content = content;
	}

	/**
	 * 
	 * @return Kafka message Key as String
	 */
	public String getKey() {
		return key;
	}

	/**
	 * 
	 * @return Kadka message payload as String
	 */
	public String getContent() {
		return content;
	}
	
	
	
}
