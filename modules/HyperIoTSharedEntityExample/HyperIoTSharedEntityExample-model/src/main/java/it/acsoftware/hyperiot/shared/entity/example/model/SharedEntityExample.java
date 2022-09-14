package it.acsoftware.hyperiot.shared.entity.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTSharedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;
import it.acsoftware.hyperiot.huser.model.HUser;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;

/**
 * @author Aristide Cittadino Model class for SharedEntityExample of HyperIoT platform. This
 * class is used to map SharedEntityExample with the database.
 */

@Entity
public class SharedEntityExample extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity, HyperIoTSharedEntity {

    /**
     * SharedEntityExample name
     */
    private String name;

    /**
     * SharedEntityExample description
     */
    private String description;

    /**
     * SharedEntityExample's user
     */
    private HUser user;


    /**
     * @return field name
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    public String getName() {
        return name;
    }

    /**
     * @param name field name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return field description
     */
    @NoMalitiusCode
    @Length(max = 3000)
    public String getDescription() {
        return description;
    }

    /**
     * @param description field description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return SharedEntityExample's user
     */
    @NotNullOnPersist
    @ManyToOne(targetEntity = HUser.class)
    public HUser getUser() {
        return user;
    }

    public void setUser(HUser user) {
        this.user = user;
    }

    @Override
    @Transient
    @JsonIgnore
    public HyperIoTUser getUserOwner() {
        return user;
    }

    @Override
    public void setUserOwner(HyperIoTUser hyperIoTUser) {
        this.setUser((HUser) hyperIoTUser);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SharedEntityExample other = (SharedEntityExample) obj;

        if (this.getId() > 0 && other.getId() > 0)
            return this.getId() == other.getId();

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

}
