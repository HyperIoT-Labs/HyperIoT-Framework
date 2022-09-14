package it.acsoftware.hyperiot.company.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.acsoftware.hyperiot.base.api.HyperIoTOwnedResource;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTInnerEntityJSONSerializer;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;
import it.acsoftware.hyperiot.huser.model.HUser;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author Aristide Cittadino Model class for Company of HyperIoT platform. This
 * class is used to map Company with the database.
 */

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "vatNumber"))
public class Company extends HyperIoTAbstractEntity
        implements HyperIoTProtectedEntity, HyperIoTOwnedResource {

    /**
     * Company business name
     */
    @JsonView({HyperIoTJSONView.Public.class})
    private String businessName;

    /**
     * Company invoice address
     */
    @JsonView({HyperIoTJSONView.Public.class})
    private String invoiceAddress;

    /**
     * Company city
     */
    @JsonView({HyperIoTJSONView.Public.class})
    private String city;

    /**
     * Company city postal code
     */
    @JsonView({HyperIoTJSONView.Public.class})
    private String postalCode;

    /**
     * Company nation
     */
    @JsonView({HyperIoTJSONView.Public.class})
    private String nation;

    /**
     * Company vat number
     */
    @JsonView({HyperIoTJSONView.Public.class})
    private String vatNumber;

    /**
     * Company creator
     */
    @JsonView({HyperIoTJSONView.Public.class})
    @JsonSerialize(using = HyperIoTInnerEntityJSONSerializer.class)
    private HUser hUserCreator;

    /**
     * Get company business name
     *
     * @return Company business name
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @Size( max = 255)
    public String getBusinessName() {
        return businessName;
    }

    /**
     * Set company business name
     *
     * @param businessName New company business name
     */
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    /**
     * Get company invoice address
     *
     * @return
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @Size( max = 255)
    public String getInvoiceAddress() {
        return invoiceAddress;
    }

    /**
     * Set company invoice address
     *
     * @param invoiceAddress New company invoice address
     */
    public void setInvoiceAddress(String invoiceAddress) {
        this.invoiceAddress = invoiceAddress;
    }

    /**
     * Get company city
     *
     * @return Company city
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @Size( max = 255)
    public String getCity() {
        return city;
    }

    /**
     * Set company city
     *
     * @param city New company city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get company postal code
     *
     * @return Company postal code
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @Size( max = 255)
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Set company postal code
     *
     * @param postalCode New company postal code
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Get company nation
     *
     * @return Company nation
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @Size( max = 255)
    public String getNation() {
        return nation;
    }

    /**
     * Set company nation
     *
     * @param nation New company nation
     */
    public void setNation(String nation) {
        this.nation = nation;
    }

    /**
     * Get company vat number
     *
     * @return Company vat number
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @Size( max = 255)
    public String getVatNumber() {
        return vatNumber;
    }

    /**
     * Set company vat number
     *
     * @param vatNumber New company vat number
     */
    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    /**
     * Get company creator
     *
     * @return Company creator
     */
    @ManyToOne
    @JsonIgnore
    public HUser getHUserCreator() {
        return hUserCreator;
    }

    /**
     * Set company creator
     *
     * @param hUserCreator New company creator
     */
    public void setHUserCreator(HUser hUserCreator) {
        this.hUserCreator = hUserCreator;
    }


    @Transient
    @JsonIgnore
    public HUser getUser() {
        return this.getHUserCreator();
    }

    /**
     * @return
     */
    @Override
    @Transient
    @JsonIgnore
    public HyperIoTUser getUserOwner() {
        return this.getUser();
    }

    /**
     * @return
     */
    @Override
    @Transient
    public void setUserOwner(HyperIoTUser user) {
        this.setUser((HUser) user);
    }

    public void setUser(HUser user) {
        this.setHUserCreator(user);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((businessName == null) ? 0 : businessName.hashCode());
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((invoiceAddress == null) ? 0 : invoiceAddress.hashCode());
        result = prime * result + ((nation == null) ? 0 : nation.hashCode());
        result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
        result = prime * result + ((vatNumber == null) ? 0 : vatNumber.hashCode());
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
        Company other = (Company) obj;
        if (this.getId() > 0 && other.getId() > 0)
            return this.getId() == other.getId();
        if (businessName == null) {
            if (other.businessName != null)
                return false;
        } else if (!businessName.equals(other.businessName))
            return false;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (invoiceAddress == null) {
            if (other.invoiceAddress != null)
                return false;
        } else if (!invoiceAddress.equals(other.invoiceAddress))
            return false;
        if (nation == null) {
            if (other.nation != null)
                return false;
        } else if (!nation.equals(other.nation))
            return false;
        if (postalCode == null) {
            if (other.postalCode != null)
                return false;
        } else if (!postalCode.equals(other.postalCode))
            return false;
        if (vatNumber == null) {
            if (other.vatNumber != null)
                return false;
        } else if (!vatNumber.equals(other.vatNumber))
            return false;
        return true;
    }

}
