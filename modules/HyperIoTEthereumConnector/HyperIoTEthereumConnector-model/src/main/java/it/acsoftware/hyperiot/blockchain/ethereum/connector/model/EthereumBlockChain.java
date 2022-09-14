package it.acsoftware.hyperiot.blockchain.ethereum.connector.model;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;
import org.apache.commons.lang3.Validate;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;

/**
 * @author Aristide Cittadino
 * Model class for EthereumConnector  of HyperIoT platform
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "host")})
public class EthereumBlockChain extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity {

    private String protocol;

    private String host;

    private String port;

    private List<EthereumSmartContract> contracts;

    public EthereumBlockChain(String protocol, String host, String port) {
        Validate.notNull(protocol);
        Validate.matchesPattern(protocol, "http|https");
        Validate.notNull(host);
        Validate.notEmpty(host);
        Validate.notNull(port);
        Validate.notEmpty(port);
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    private EthereumBlockChain(){

    }

    @NotEmpty
    @NotNullOnPersist
    @NoMalitiusCode
    @Column(columnDefinition = "TEXT NOT NULL")
    public String getProtocol() {
        return protocol;
    }

    @Column(columnDefinition = "TEXT NOT NULL")
    public String getHost() {
        return host;
    }

    @NotEmpty
    @NotNullOnPersist
    @NoMalitiusCode
    @Column(columnDefinition = "TEXT NOT NULL")
    public String getPort() {
        return port;
    }

    @OneToMany(mappedBy = "blockChain")
    @Cascade({org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.DELETE})
    public List<EthereumSmartContract> getContracts() {
        return contracts;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setContracts(List<EthereumSmartContract> contracts) {
        this.contracts = contracts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EthereumBlockChain that = (EthereumBlockChain) o;
        return protocol.equals(that.protocol) && host.equals(that.host) && port.equals(that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, host, port);
    }
}