package it.acsoftware.hyperiot.base.security.rest;

import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.security.jwt.filters.HyperIoTJwtFilter;
import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import org.apache.cxf.rs.security.jose.common.JoseException;
import org.apache.cxf.rs.security.jose.jws.JwsHeaders;
import org.apache.cxf.rs.security.jose.jws.JwsSignatureVerifier;
import org.apache.cxf.rs.security.jose.jws.JwsUtils;
import org.apache.cxf.rs.security.jose.jwt.JwtException;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.apache.cxf.rs.security.jose.jwt.JwtUtils;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author Aristide Cittadino
 * This class exposes a component filter for authentcated requests
 */
@LoggedIn
@Provider
@Component(immediate = true, property = {"org.apache.cxf.dosgi.IntentName=jwtAuthFilter"})
public class HyperIoTAuthenticationFilter extends HyperIoTJwtFilter
        implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(HyperIoTAuthenticationFilter.class.getName());

    @Context
    protected ResourceInfo info;

    public HyperIoTAuthenticationFilter() {
        super();
        log.debug( "HyperIoTAuthenticationFilter : Loading properties from config file");
        Properties props = HyperIoTSecurityUtil
                .getJwtProperties(HyperIoTUtil.getBundleContext(this));
        props.put("expected.claim.audience", "client");
        JwsHeaders jwsHeaders = new JwsHeaders(props);
        JwsSignatureVerifier sigVerifier = JwsUtils.loadSignatureVerifier(props, jwsHeaders);
        this.setJwsVerifier(sigVerifier);
    }

    /**
     * @param jwt
     */
    @Override
    protected void validateToken(JwtToken jwt) {
        log.debug( "HyperIoTAuthenticationFilter : validateToken");
        // Overriding to disable audience validation
        JwtUtils.validateTokenClaims(jwt.getClaims(), this.getTtl(), this.getClockOffset(), false);
        //Enforcing issuers on @LoggedIn annotation, if no issuers is given 'it.acsoftware.hyperiot.base.api.HyperIoTUser' is the only default one
        //This check is done only when used @LoggedIn annotation, if not, no issuer validation is done
        if (info != null) {
            try {
                LoggedIn annotation = (info.getResourceMethod() != null) ? info.getResourceMethod().getAnnotation(LoggedIn.class) : null;
                if (annotation != null) {
                    List<String> issuers = Arrays.asList(annotation.issuers());
                    if (!issuers.contains(jwt.getClaims().getIssuer()))
                        throw new JwtException("Issuer restriction,only users entities can access rest services!");
                }
            } catch (NullPointerException e) {
                log.debug( e.getMessage(), e);
            }
        }
    }

    /**
     * @param requestContext
     * @throws IOException
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.debug( "In HyperIoTAuthenticationFilter on class: {}.{}", new Object[]{info.getResourceClass(), info.getResourceMethod()});
        try {
            super.filter(requestContext);
        } catch (Throwable e) {
            log.debug( "Error in Authentication filter:", e);
            List<String> message = new ArrayList<String>();
            HyperIoTBaseError error = new HyperIoTBaseError();
            error.setStatusCode(401);
            error.setType(NotAuthorizedException.class.getName());
            message.add("JWT Token not valid or expired!");
            error.setErrorMessages(message);
            requestContext.abortWith(Response.status(401).entity(error)
                    .type(MediaType.APPLICATION_JSON_TYPE).build());
        }
    }

    /**
     * @param requestContext
     * @return
     */
    @Override
    protected String getEncodedJwtToken(ContainerRequestContext requestContext) {
        try {
            return super.getEncodedJwtToken(requestContext);
        } catch (JoseException e) {
            // if not present in the header param, let's search in cookies
            if (requestContext.getCookies()
                    .containsKey(HyperIoTConstants.HYPERIOT_AUTHORIZATION_COOKIE_NAME)) {
                return requestContext.getCookies()
                        .get(HyperIoTConstants.HYPERIOT_AUTHORIZATION_COOKIE_NAME).getValue();
            }
            return null;
        }
    }

}
