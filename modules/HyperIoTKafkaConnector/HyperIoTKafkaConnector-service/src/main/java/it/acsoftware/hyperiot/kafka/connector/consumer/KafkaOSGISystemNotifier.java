package it.acsoftware.hyperiot.kafka.connector.consumer;

import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaMessageReceiver;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaSystemMessageNotifier;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;
import it.acsoftware.hyperiot.kafka.connector.util.HyperIoTKafkaConnectorConstants;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilter;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;

@Component(service = KafkaSystemMessageNotifier.class)
public class KafkaOSGISystemNotifier implements KafkaSystemMessageNotifier {
    private static Logger log = LoggerFactory.getLogger(KafkaOSGISystemNotifier.class);

    /**
     * @param message Message that must be sent
     */
    public void notifyKafkaMessage(HyperIoTKafkaMessage message) {
        try {
            BundleContext ctx = HyperIoTUtil.getBundleContext(this);
            //getting al registered message receiver that wants to receive all keys from a topic and a specific key.
            OSGiFilter specificKeyFilter = OSGiFilterBuilder.createFilter(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_OSGI_KEY_FILTER, new String(message.getKey()));
            OSGiFilter filter = OSGiFilterBuilder
                    .createFilter(HyperIoTKafkaConnectorConstants.HYPERIOT_KAFKA_OSGI_TOPIC_FILTER,
                            message.getTopic())
                    .and(specificKeyFilter);
            String topicFilter = filter.getFilter();
            log.debug("Searching for components with OSGi filter: {}", topicFilter);
            Collection<ServiceReference<KafkaMessageReceiver>> references = ctx
                    .getServiceReferences(KafkaMessageReceiver.class, topicFilter);
            Iterator<ServiceReference<KafkaMessageReceiver>> it = references.iterator();
            while (it.hasNext()) {
                KafkaMessageReceiver messageReceiver = ctx.getService(it.next());
                if (messageReceiver != null && message != null) {
                    log.debug("Kafka receiver found for message on topic: {}", new Object[]{message.getTopic(), messageReceiver.getClass().getName()});
                    try {
                        messageReceiver.receive(message);
                    } catch (Throwable e) {
                        log.error(
                                "Error while executing notify on component: {} message is: {}",
                                new Object[]{messageReceiver.getClass().getName(), e.getMessage(), e});
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }
}
