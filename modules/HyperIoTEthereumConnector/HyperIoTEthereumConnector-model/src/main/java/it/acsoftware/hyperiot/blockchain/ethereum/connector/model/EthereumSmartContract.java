/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.blockchain.ethereum.connector.model;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * @Author Aristide Cittadino
 * Entity which maps a contract registration on a ethereum blockchain.
 * This entity saves the address on which deployed contracts responds.
 */
@Entity
@Table(name = "ethereum_smart_contract", uniqueConstraints = {@UniqueConstraint(columnNames = {"name","blockchain_id"}), @UniqueConstraint(columnNames = {"contractClass", "address", "blockchain_id"})})
public class EthereumSmartContract extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity {

    private String contractClass;

    private String name;

    private String address;

    private String transactionReceipt;

    private EthereumBlockChain blockChain;

    public EthereumSmartContract(String contractClass, String name, String transactionReceipt, String address, EthereumBlockChain blockChain) {
        this.contractClass = contractClass;
        this.name = name;
        this.transactionReceipt = transactionReceipt;
        this.address = address;
        this.blockChain = blockChain;
    }

    private EthereumSmartContract() {
    }

    @Column(columnDefinition = "TEXT NOT NULL")
    @NotEmpty
    @NotNullOnPersist
    @NoMalitiusCode
    public String getContractClass() {
        return contractClass;
    }

    @Column(columnDefinition = "TEXT NOT NULL")
    @NotEmpty
    @NotNullOnPersist
    @NoMalitiusCode
    public String getName() {
        return name;
    }

    @Column(columnDefinition = "TEXT NOT NULL")
    @NotEmpty
    @NotNullOnPersist
    @NoMalitiusCode
    public String getTransactionReceipt() {
        return transactionReceipt;
    }

    @Size(max = 20, min = 20)
    @Length(min = 20, max = 20)
    @NotEmpty
    @NotNullOnPersist
    @NoMalitiusCode
    public String getAddress() {
        return address;
    }

    @NotNullOnPersist
    @ManyToOne
    @JoinColumn(name = "blockchain_id")
    public EthereumBlockChain getBlockChain() {
        return blockChain;
    }

    public void setContractClass(String contractClass) {
        this.contractClass = contractClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTransactionReceipt(String transactionReceipt) {
        this.transactionReceipt = transactionReceipt;
    }

    public void setBlockChain(EthereumBlockChain blockChain) {
        this.blockChain = blockChain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EthereumSmartContract that = (EthereumSmartContract) o;
        return contractClass.equals(that.contractClass) && name.equals(that.name) && address.equals(that.address) && blockChain.equals(that.blockChain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractClass, name, address, blockChain);
    }
}
