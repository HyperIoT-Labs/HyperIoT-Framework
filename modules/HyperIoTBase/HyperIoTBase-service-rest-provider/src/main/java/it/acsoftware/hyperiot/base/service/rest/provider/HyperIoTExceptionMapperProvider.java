package it.acsoftware.hyperiot.base.service.rest.provider;

import it.acsoftware.hyperiot.base.exception.GenericExceptionMapperProvider;
import org.apache.cxf.dosgi.common.api.IntentsProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author Generoso Martello
 */
@Component(property = {"org.apache.cxf.dosgi.IntentName=exceptionmapper"}, immediate = true)
public class HyperIoTExceptionMapperProvider implements IntentsProvider {
    private Logger log = LoggerFactory.getLogger(HyperIoTExceptionMapperProvider.class.getName());
    private GenericExceptionMapperProvider exceptionProvider;
    private List<?> intentArray;

    public HyperIoTExceptionMapperProvider() {
        log.info( "Register HyperIoT Provider ExceptionMapper Intent");
        this.exceptionProvider = new GenericExceptionMapperProvider();
        intentArray = Arrays.asList(this.exceptionProvider);
    }

    @Override
    public List<?> getIntents() {
        return intentArray;
    }

}
