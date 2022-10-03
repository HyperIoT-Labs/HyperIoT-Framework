# HyperIoTResource[](id=hyperiot-resource)

The first concept represented through the interface are resources, which are any type of entity (even those that are not then saved to the database) that a service might manage.

Resources are important because they can be of different types:

* Database entities
* Pages
* Abstract Entities or Concepts

```
package it.acsoftware.hyperiot.base.api;

/**
 * @author Aristide Cittadino Generic interface component for HyperIoTResource.
 * This interface defines the method for gets the resource name of
 * entity of HyperIoT platform.
 */
public interface HyperIoTResource {
    /**
     * Gets the resource name of HyperIoT platform
     *
     * @return resource name of HyperIoT platform
     */
    public String getResourceName();
}


```
The interface defines only a fundamental <b>getResourceName()</b> method that is a unique name of the resource. Typically this can be the full class name of the class implementing that interface.

By implementing such an interface The framework can use the single instance as a system resource and then hook in additional more complex concepts such as:

* Saving to the database
* Managing permissions