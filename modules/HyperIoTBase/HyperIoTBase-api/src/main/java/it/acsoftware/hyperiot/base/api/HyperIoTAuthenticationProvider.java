package it.acsoftware.hyperiot.base.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;

public interface HyperIoTAuthenticationProvider {

    boolean screeNameAlreadyExists(HyperIoTAuthenticable authenticable);

    HyperIoTAuthenticable findByUsername(String username);

    HyperIoTAuthenticable login(String username, String password);

    String[] validIssuers();
}
