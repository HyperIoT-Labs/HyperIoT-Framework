package it.acsoftware.hyperiot.base.model;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import it.acsoftware.hyperiot.base.api.HyperIoTResource;

/**
 * @author Aristide Cittadino Model class for HyperIoTAbstractResource. This
 * class implements HyperIoTResource methods to obtain the resource name
 * of the entity mapped to the database.
 */
public abstract class HyperIoTAbstractResource implements HyperIoTResource {

    /**
     * Gets the resource name of entity of HyperIoT platform
     */
    @Override
    @Transient
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public String getResourceName() {
        return this.getClass().getName();
    }
}
