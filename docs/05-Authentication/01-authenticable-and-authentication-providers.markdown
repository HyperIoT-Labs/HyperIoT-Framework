# Authenticable and Authentication Providers [](id=authenticable-and-authentication-providers)

As mentioned in the previous section HyperIoT Framework also allows Authentication Providers to be defined according to the needs of the individual project.

The essential feature of this approach is that Authentication Providers are bound generically to the Jaas login modules available on HyperIoT.

It will therefore be very easy to implement a JaaS plugin based on custom logics defined on the project.

Some examples:

* <b>HyperIoT Core</b> exports a first JaaS module for user authentication 
* <b>HyperIoTAuthentication</b> uses the standard service related to user login 
* The Karaf SSH module is to the HyperIoT JaaS plugin in order to validate login on karaf via users defined in HyperIoT 
* the Hawtio module is tied , again via the JaaS provider to the users defined in HyperIoT

It is very easy, then, by extending the HyperIoTJaaSAuthenticationModule class and implementing your own HyperIoTAuthenticable and HyperIoTAuthenticationProvider, to add custom security logic on custom entities.

Below is the interface that defines an Authenticable in HyperIoT Framework:

```
package it.acsoftware.hyperiot.base.api.entity;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.acsoftware.hyperiot.base.api.HyperIoTRole;

/**
 * @author Aristide Cittadino This interface marks an entity to be
 * "authenticable" this means that it must expose methods for getting
 * and setting password and passwordConfirm fields.
 * <p>
 * This interface is useful if you want exploits password validation for
 * your entity. This enables sensor, devices, users to connect to
 * HyperIoT Platform.
 */
public interface HyperIoTAuthenticable extends HyperIoTBaseEntity {
    /**
     *
     * @return the field name which contains the screenName
     */
    @JsonIgnore
    String getScreenNameFieldName();

    /**
     * @return the username or thingname
     */
    String getScreenName();

    /**
     * @return true if it is an admin user
     */
    @JsonIgnore
    boolean isAdmin();

    /**
     * @return
     */
    Collection<? extends HyperIoTRole> getRoles();

    /**
     * @return the password
     */
    @JsonIgnore
    String getPassword();

    /**
     * @param password The confirm password
     */
    void setPassword(String password);

    /**
     * @return the confirm password
     */
    @JsonIgnore
    String getPasswordConfirm();

    /**
     * @param password the confirm password
     */
    void setPasswordConfirm(String password);

    /**
     * @return true if the authenticable is activated for authentication
     */
    @JsonIgnore
    boolean isActive();
}
  
```

Here the Authentication Provider interface:

```
package it.acsoftware.hyperiot.base.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;

public interface HyperIoTAuthenticationProvider {

    boolean screeNameAlreadyExists(HyperIoTAuthenticable authenticable);

    HyperIoTAuthenticable findByUsername(String username);

    HyperIoTAuthenticable login(String username, String password);

    String[] validIssuers();
}

```

Basically, this interface defines the contract to be respected in case you want to implement an authentication provider.

In fact, the following methods must be implemented:

1. <b>screenNameAlreadyExists</b> - to expose a control logic on duplicate screen name
2. <b>findByUsername</b> - to be renamed to findByScreenName to implement a search logic on the authenticable entity via, precisely, screen name
3. <b>login</b> - method that performs the login
3. <b>validIssuers</b> - the ability to validate the login issuer so that filtering logic can also be performed (e.g., prevent user logins on a provider login on devices)