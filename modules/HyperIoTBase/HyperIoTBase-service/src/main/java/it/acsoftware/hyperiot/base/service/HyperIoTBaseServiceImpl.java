package it.acsoftware.hyperiot.base.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;

/**
 * @author Aristide Cittadino Implementation class of HyperIoTBaseApi. It is
 * used to implement methods in order to interact with the system layer.
 */
public abstract class HyperIoTBaseServiceImpl extends HyperIoTBaseAbstractService implements HyperIoTBaseApi {
    /**
     * @return The current SystemService
     */
    protected abstract HyperIoTBaseSystemApi getSystemService();

}
