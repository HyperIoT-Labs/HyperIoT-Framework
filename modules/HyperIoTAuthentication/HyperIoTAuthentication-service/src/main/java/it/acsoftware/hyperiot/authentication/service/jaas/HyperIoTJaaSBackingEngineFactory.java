package it.acsoftware.hyperiot.authentication.service.jaas;

import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.karaf.jaas.modules.BackingEngineFactory;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(service = BackingEngineFactory.class)
public class HyperIoTJaaSBackingEngineFactory implements BackingEngineFactory {
    @Override
    public String getModuleClass() {
        return HyperIoTJaaSAuthenticationModule.class.getName();
    }

    @Override
    public BackingEngine build(Map<String, ?> options) {
        return new HyperIoTJaaSBackingEngine();
    }
}
