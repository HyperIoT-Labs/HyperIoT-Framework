package it.acsoftware.hyperiot.base.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;

/**
 * @author Aristide Cittadino Generic interface component for HyperIoTRole. This
 * interface defines methods for obtaining and setting the name of a
 * protected role in the HyperIoT platform.
 */
public interface HyperIoTRole extends HyperIoTProtectedEntity {

    /**
     * Gets a name of protected Role
     *
     * @return name of protected Role
     */
    public String getName();

    /**
     * Sets a name of protected Role
     *
     * @param name sets a name of protected Role
     */
    public void setName(String name);

    /**
     * @return Role descriptiono
     */
    public String getDescription();

    /**
     * @param description Role description
     */
    public void setDescription(String description);

}
