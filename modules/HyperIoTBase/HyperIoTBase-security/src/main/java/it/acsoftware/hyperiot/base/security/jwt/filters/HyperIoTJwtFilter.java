package it.acsoftware.hyperiot.base.security.jwt.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

import org.apache.cxf.rs.security.jose.common.JoseException;

/**
 * @author Aristide Cittadino Copy of JwtAuthenticationFilter, please refer to
 * AbstractHyperIoTSecurityFilter
 */
public class HyperIoTJwtFilter extends AbstractHyperIoTSecurityFilter {
    private static final String DEFAULT_AUTH_SCHEME = "JWT";
    private String expectedAuthScheme = DEFAULT_AUTH_SCHEME;

    /**
     *
     * @param requestContext
     * @return
     */
    protected String getEncodedJwtToken(ContainerRequestContext requestContext) {
        String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        String[] parts = auth == null ? null : auth.split(" ");
        if (parts == null || !expectedAuthScheme.equals(parts[0]) || parts.length != 2) {
            throw new JoseException(expectedAuthScheme + " scheme is expected");
        }
        return parts[1];
    }

    /**
     *
     * @param expectedAuthScheme
     */
    public void setExpectedAuthScheme(String expectedAuthScheme) {
        this.expectedAuthScheme = expectedAuthScheme;
    }
}
