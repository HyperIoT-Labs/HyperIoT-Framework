package it.acsoftware.hyperiot.bundle.listener.service;

import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.bundle.listener.model.BundleTrackerItem;
import org.osgi.service.component.annotations.Component;
import it.acsoftware.hyperiot.bundle.listener.api.BundleListenerSystemApi;

import  it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl ;

import java.util.ArrayList;

/**
 * 
 * @author Aristide Cittadino Implementation class of the BundleListenerSystemApi
 *         interface. This  class is used to implements all additional
 *         methods to interact with the persistence layer.
 */
@Component(service = BundleListenerSystemApi.class, immediate = true)
public final class BundleListenerSystemServiceImpl extends HyperIoTBaseSystemServiceImpl   implements BundleListenerSystemApi {
    private BundleTracker bundleListenerBundleTracker;

    public BundleListenerSystemServiceImpl() throws Exception {
        super();
        this.bundleListenerBundleTracker = new BundleTracker();
        this.bundleListenerBundleTracker.start(HyperIoTUtil.getBundleContext(this.getClass()));
    }

    @Override
    public ArrayList<BundleTrackerItem> list() {
        return this.bundleListenerBundleTracker.list();
    }
    @Override
    public BundleTrackerItem get(String symbolicName) {
        return this.bundleListenerBundleTracker.get(symbolicName);
    }
}
