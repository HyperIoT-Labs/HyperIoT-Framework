package it.acsoftware.hyperiot.base.security.rest;

import it.acsoftware.hyperiot.base.security.util.HyperIoTSecurityUtil;
import org.osgi.service.component.annotations.Component;

import javax.crypto.Cipher;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author Aristide Cittadino
 * This class exposes a component filter for content decryption with server private key
 */
@PayloadEncryptedWithServerPublickKey
@Provider
@Component(immediate = true, property = {"org.apache.cxf.dosgi.IntentName=decryptionFilter"})
public class HyperIoTDecryptionFilter
    implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HyperIoTDecryptionFilter.class.getName());

    @Context
    protected ResourceInfo info;

    public HyperIoTDecryptionFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        PayloadEncryptedWithServerPublickKey annotation = info.getResourceMethod().getAnnotation(PayloadEncryptedWithServerPublickKey.class);
        if (annotation != null) {
            decrypt(requestContext, annotation);
        }
    }

    private void decrypt(ContainerRequestContext requestContext, PayloadEncryptedWithServerPublickKey annotation) {
        boolean hasAllowedMethods = requestContext.getMethod().toLowerCase().equals("post") || requestContext.getMethod().toLowerCase().equals("put");
        String cipherAlgorithm = annotation.cipherAlgorithm();
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            if (cipher != null && hasAllowedMethods && requestContext.hasEntity()) {
                InputStream is = requestContext.getEntityStream();
                byte[] data = new byte[is.available()];
                is.read(data);
                byte[] decodedFromBase64 = Base64.getDecoder().decode(data);
                byte[] decrypted = HyperIoTSecurityUtil.decodeMessageWithServerPrivateKey(decodedFromBase64, cipher);
                ByteArrayInputStream bais = new ByteArrayInputStream(decrypted);
                requestContext.setEntityStream(bais);
            }
        } catch (Exception e) {
            log.error( e.getMessage(), e);
        }
        requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity("Unable to decrypt message!").build());
    }
}
