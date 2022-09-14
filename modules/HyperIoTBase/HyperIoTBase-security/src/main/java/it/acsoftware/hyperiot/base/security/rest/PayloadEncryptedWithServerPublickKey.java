package it.acsoftware.hyperiot.base.security.rest;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Aristide Cittadino.
 * Annotation to use on JAX-RS methods for decrypt payload encrypted with server public key
 */
@NameBinding
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface PayloadEncryptedWithServerPublickKey {
    String cipherAlgorithm() default "RSA/NONE/PKCS1PADDING";
}
