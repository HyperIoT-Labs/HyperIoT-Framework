package it.acsoftware.hyperiot.bundle.listener.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.bundle.listener.model.BundleTrackerItem;
import java.util.ArrayList;


/**
 * 
 * @author Aristide Cittadino Interface component for BundleListenerApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface BundleListenerApi extends HyperIoTBaseApi {
    ArrayList<BundleTrackerItem> list();
    BundleTrackerItem get(String symbolicName);
}
