package it.acsoftware.hyperiot.bundle.listener.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

import javax.persistence.Transient;

// TODO: move to HyperIoTBundleListener model
public class BundleTrackerItem {

    private Bundle bundle;
    private BundleEvent event; // last received event

    public BundleTrackerItem(Bundle bundle) {
        this.bundle = bundle;
    }

    public void update(BundleEvent event) {
        this.event = event;
    }

    @Transient
    @JsonIgnore
    public Bundle getBundle() {
        return this.bundle;
    }

    public String getName() {
        return this.bundle.getSymbolicName();
    }
    public String getState() {
        return stateAsString(bundle);
    }
    public String getType() {
        return typeAsString(this.event);
    }

    public static String stateAsString(Bundle bundle) {
        if (bundle == null) {
            return "null";
        }
        int state = bundle.getState();
        switch (state) {
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.STARTING:
                return "STARTING";
            case Bundle.STOPPING:
                return "STOPPING";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "unknown bundle state: " + state;
        }
    }

    private static String typeAsString(BundleEvent event) {
        if (event == null) {
            return "null";
        }
        int type = event.getType();
        switch (type) {
            case BundleEvent.INSTALLED:
                return "INSTALLED";
            case BundleEvent.LAZY_ACTIVATION:
                return "LAZY_ACTIVATION";
            case BundleEvent.RESOLVED:
                return "RESOLVED";
            case BundleEvent.STARTED:
                return "STARTED";
            case BundleEvent.STARTING:
                return "Starting";
            case BundleEvent.STOPPED:
                return "STOPPED";
            case BundleEvent.UNINSTALLED:
                return "UNINSTALLED";
            case BundleEvent.UNRESOLVED:
                return "UNRESOLVED";
            case BundleEvent.UPDATED:
                return "UPDATED";
            default:
                return "unknown event type: " + type;
        }
    }

}
