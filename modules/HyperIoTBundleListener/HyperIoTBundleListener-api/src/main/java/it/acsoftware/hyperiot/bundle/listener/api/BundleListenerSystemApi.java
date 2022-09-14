package it.acsoftware.hyperiot.bundle.listener.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;
import it.acsoftware.hyperiot.bundle.listener.model.BundleTrackerItem;

import java.util.ArrayList;

/**
 * 
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 *         interface defines methods for additional operations.
 *
 */
public interface BundleListenerSystemApi extends HyperIoTBaseSystemApi {
    ArrayList<BundleTrackerItem> list();
    BundleTrackerItem get(String symbolicName);
}