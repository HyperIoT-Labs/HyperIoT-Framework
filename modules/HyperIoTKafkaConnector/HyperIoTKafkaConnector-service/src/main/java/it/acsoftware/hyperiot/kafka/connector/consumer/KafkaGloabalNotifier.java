package it.acsoftware.hyperiot.kafka.connector.consumer;

import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaSystemMessageNotifier;
import it.acsoftware.hyperiot.kafka.connector.model.HyperIoTKafkaMessage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;

public class KafkaGloabalNotifier {
    private static Logger log = LoggerFactory.getLogger(KafkaGloabalNotifier.class);

    public static void notifyKafkaMessage(HyperIoTKafkaMessage message) {
        try {
            BundleContext ctx = HyperIoTUtil.getBundleContext(KafkaGloabalNotifier.class);
            Collection<ServiceReference<KafkaSystemMessageNotifier>> references =
                    ctx.getServiceReferences(KafkaSystemMessageNotifier.class, null);
            Iterator<ServiceReference<KafkaSystemMessageNotifier>> it = references.iterator();
            while (it.hasNext()) {
                KafkaSystemMessageNotifier kafkaSystemMessageNotifier = ctx.getService(it.next());
                if (kafkaSystemMessageNotifier != null && message != null) {
                    log.debug("Kafka receiver found for message on topic: {}", new Object[]{message.getTopic(), kafkaSystemMessageNotifier.getClass().getName()});
                    try {
                        kafkaSystemMessageNotifier.notifyKafkaMessage(message);
                    } catch (Throwable e) {
                        log.error(
                                "Error while executing notify on component: {} message is: {}",
                                new Object[]{kafkaSystemMessageNotifier.getClass().getName(), e.getMessage(), e});
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
