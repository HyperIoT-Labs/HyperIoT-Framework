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
