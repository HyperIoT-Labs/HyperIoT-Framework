package it.acsoftware.hyperiot.base.model.authentication.context;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTJwtContext;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;

import java.security.Principal;
import java.util.Set;

public class HyperIoTContextFactory {

    public static HyperIoTJwtContext createJwtContext(JwtToken jwt, String roleClaim) {
        return new HyperIoTJwtContextImpl(jwt, roleClaim);
    }

    public static HyperIoTContext createBasicContext(Set<Principal> principals) {
        return new HyperIoTBasicContext(principals);
    }
}
