package it.acsoftware.hyperiot.base.service.rest.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.dosgi.common.api.IntentsProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author Aristide Cittadino.
 */
@Component(property = {"org.apache.cxf.dosgi.IntentName=jackson"}, immediate = true)
public class HyperIoTJacksonIntentProvider implements IntentsProvider {
    private static Logger log = LoggerFactory.getLogger(HyperIoTJacksonIntentProvider.class.getName());
    private JacksonJsonProvider jsonProvider;
    private ObjectMapper mapper;
    private List<?> intentList;

    public HyperIoTJacksonIntentProvider() {
        mapper = new ObjectMapper();
        this.jsonProvider = new JacksonJsonProvider(mapper);
        this.intentList = Arrays.asList(jsonProvider);
        log.info( "Register HyperIoT Provider Jackson Intent");
    }

    @Override
    public List<?> getIntents() {
        return intentList;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
