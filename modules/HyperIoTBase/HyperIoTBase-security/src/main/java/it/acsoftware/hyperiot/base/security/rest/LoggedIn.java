package it.acsoftware.hyperiot.base.security.rest;

import it.acsoftware.hyperiot.base.api.HyperIoTUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

/**
 * Author Aristide Cittadino.
 * Annotation to use on JAX-RS methods for identifying authenticated requests
 */
@NameBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LoggedIn {
    String[] issuers() default {"it.acsoftware.hyperiot.base.api.HyperIoTUser"};
}
