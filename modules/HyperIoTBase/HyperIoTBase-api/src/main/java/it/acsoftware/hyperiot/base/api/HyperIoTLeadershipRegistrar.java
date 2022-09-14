package it.acsoftware.hyperiot.base.api;

/**
 * Marker interface for components who wants to registera leadership election on
 * Cluster coordinators
 */
public interface HyperIoTLeadershipRegistrar {
    String getLeadershipPath();
}
