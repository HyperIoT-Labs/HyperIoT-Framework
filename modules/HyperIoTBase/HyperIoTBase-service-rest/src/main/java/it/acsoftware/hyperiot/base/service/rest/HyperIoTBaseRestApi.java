package it.acsoftware.hyperiot.base.service.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationSystemApi;
import it.acsoftware.hyperiot.base.model.authentication.context.HyperIoTContextFactory;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.exception.*;
import it.acsoftware.hyperiot.base.model.HyperIoTBaseError;
import it.acsoftware.hyperiot.base.security.rest.HyperIoTAuthenticationFilter;
import it.acsoftware.hyperiot.base.service.rest.provider.HyperIoTJacksonIntentProvider;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTErrorConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import org.apache.cxf.dosgi.common.api.IntentsProvider;
import org.apache.cxf.jaxrs.impl.tl.ThreadLocalSecurityContext;
import org.apache.cxf.rs.security.jose.jwt.JwtClaims;
import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Aristide Cittadino Model class for HyperIoTBaseRestApi. This class
 * implements the method to derive security information from the
 * HyperIoTContext.
 */
public abstract class HyperIoTBaseRestApi {
    /**
     * Injecting the SecurityContext to interact with security information
     */
    @Context
    protected SecurityContext securityContext;

    private static Logger internalLogger = LoggerFactory.getLogger(HyperIoTBaseRestApi.class.getName());
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * @return The current HyperIoTContext
     * Context is create by ThreadLocalSercurityContext which is a context wrapper
     */
    protected HyperIoTContext getHyperIoTContext() {
        if (this.securityContext instanceof ThreadLocalSecurityContext) {
            SecurityContext internal = ((ThreadLocalSecurityContext) this.securityContext).get();
            if (internal instanceof HyperIoTContext)
                return ((HyperIoTContext) internal);
        } else if (this.securityContext instanceof HyperIoTContext)
            return (HyperIoTContext) this.securityContext;

        //Empty claims with no principal means not logged in, so empty HyperIoTContext
        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setSubject("");
        JwtToken token = new JwtToken(jwtClaims);
        return HyperIoTContextFactory.createJwtContext(token, "");
    }

    /**
     * @return The current HyperIoTContext TO DO: this method can be used to
     * impersonate other users, must be secured. USED ONLY FOR TEST PURPOSE
     */
    public HyperIoTContext impersonate(HyperIoTUser user) {
        if (HyperIoTUtil.isInTestMode()) {
            if (user != null) {
                BundleContext bundleContext = HyperIoTUtil.getBundleContext(this);
                HyperIoTAuthenticationFilter hyperIoTAuthenticationFilter = null;
                AuthenticationSystemApi authSystemApi = null;
                String token = "";
                try {
                    ServiceReference serviceReference = bundleContext.getAllServiceReferences(ContainerRequestFilter.class.getName(), "(org.apache.cxf.dosgi.IntentName=jwtAuthFilter)")[0];
                    hyperIoTAuthenticationFilter = (HyperIoTAuthenticationFilter) bundleContext.getService(serviceReference);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    ServiceReference serviceReference = bundleContext.getAllServiceReferences(AuthenticationSystemApi.class.getName(), null)[0];
                    authSystemApi = (AuthenticationSystemApi) bundleContext.getService(serviceReference);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                token = authSystemApi.generateToken(user);
                this.securityContext = hyperIoTAuthenticationFilter.doApplicationFilter(token);
            } else {
                //Empty claims with no principal means not logged in
                JwtClaims jwtClaims = new JwtClaims();
                jwtClaims.setSubject("");
                JwtToken token = new JwtToken(jwtClaims);
                this.securityContext = HyperIoTContextFactory.createJwtContext(token, "");
            }
        } else {
            throw new RuntimeException(
                "Impersonification is forbidden outside the test mode, please insert property "
                    + HyperIoTConstants.HYPERIOT_PROPERTY_TEST_MODE
                    + "=true inside it.acsoftware.hyperiot.cfg file.");
        }

        return this.getHyperIoTContext();
    }

    public Response handleException(Throwable exception) {
        try {
            throw exception;
        } catch (HyperIoTScreenNameAlreadyExistsException e) {
            log.error("Save failed: screename is duplicated!");
            HyperIoTBaseError response = HyperIoTBaseError.generateValidationError(e);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTDuplicateEntityException e) {
            log.error("Save failed: entity is duplicated!");
            HyperIoTBaseError response = HyperIoTBaseError.generateEntityDuplicatedError(e);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTValidationException e) {
            log.error(e.getMessage(), e);
            HyperIoTBaseError response = HyperIoTBaseError.generateValidationError(e);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTEntityNotFound | HyperIoTNoResultException e) {
            log.error(e.getMessage(), e);
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e, null,
                HyperIoTErrorConstants.ENTITY_NOT_FOUND_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTUnauthorizedException e) {
            log.error(e.getMessage(), e);
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e, null,
                HyperIoTErrorConstants.NOT_AUTHORIZED_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTUserAlreadyActivated e) {
            log.error(e.getMessage(), e);
            List<String> message = new ArrayList<>();
            message.add("it.acsoftware.hyperiot.huser.error.already.activated");
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e, message,
                HyperIoTErrorConstants.VALIDATION_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTWrongUserActivationCode e) {
            log.error(e.getMessage(), e);
            List<String> message = new ArrayList<>();
            message.add("it.acsoftware.hyperiot.huser.error.activation.failed");
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e, message,
                HyperIoTErrorConstants.VALIDATION_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTWrongUserPasswordResetCode e) {
            log.error(e.getMessage(), e);
            List<String> message = new ArrayList<>();
            message.add("it.acsoftware.hyperiot.huser.error.password.reset.failed");
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e, message,
                HyperIoTErrorConstants.VALIDATION_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTUserNotActivated e) {
            log.error(e.getMessage(), e);
            List<String> message = new ArrayList<>();
            message.add("it.acsoftware.hyperiot.authentication.error.user.not.active");
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e, message,
                HyperIoTErrorConstants.NOT_AUTHORIZED_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (HyperIoTRuntimeException e) {
            log.error(e.getMessage(), e);
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e,
                Arrays.asList(e.getMessage()), HyperIoTErrorConstants.INTERNAL_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            HyperIoTBaseError response = HyperIoTBaseError.generateHyperIoTError(e, null,
                HyperIoTErrorConstants.INTERNAL_ERROR);
            return Response.status(response.getStatusCode()).entity(response).build();
        }
    }

    /**
     * @return default HyperIoT Json Mapper
     */
    public static ObjectMapper getHyperIoTJsonMapper() {
        BundleContext ctx = HyperIoTUtil.getBundleContext(HyperIoTBaseRestApi.class);
        try {
            ServiceReference<?>[] serviceReferences = ctx.getServiceReferences(IntentsProvider.class.getName(), "(org.apache.cxf.dosgi.IntentName=jackson)");
            if (serviceReferences != null && serviceReferences.length > 0) {
                IntentsProvider jacksonIntentProvider = (IntentsProvider) ctx.getService(serviceReferences[0]);
                //redundant but secure
                if (jacksonIntentProvider instanceof HyperIoTJacksonIntentProvider) {
                    return ((HyperIoTJacksonIntentProvider) jacksonIntentProvider).getMapper();
                }
            }
        } catch (InvalidSyntaxException e) {
            internalLogger.error(e.getMessage());
        }
        return null;
    }

    /**
     * @return class logger
     */
    protected Logger getLog() {
        return log;
    }
}
