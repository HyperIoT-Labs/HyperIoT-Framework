package it.acsoftware.hyperiot.base.service;

import it.acsoftware.hyperiot.base.api.HyperIoTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class HyperIoTAbstractService implements HyperIoTService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * @return the default logger for the class
     */
    protected Logger getLog() {
        return log;
    }

}
