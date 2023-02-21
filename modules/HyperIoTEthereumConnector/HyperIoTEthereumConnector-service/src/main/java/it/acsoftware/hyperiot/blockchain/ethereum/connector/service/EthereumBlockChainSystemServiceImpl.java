/*
 * Copyright 2019-2023 HyperIoT
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

package it.acsoftware.hyperiot.blockchain.ethereum.connector.service;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumBlockChainRepository;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumBlockChainSystemApi;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumBlockChain;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumSmartContract;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * @author Aristide Cittadino Implementation class of the EthereumConnectorSystemApi
 * interface. This  class is used to implements all additional
 * methods to interact with the persistence layer.
 */
@Component(service = EthereumBlockChainSystemApi.class, immediate = true)
public final class EthereumBlockChainSystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<EthereumBlockChain> implements EthereumBlockChainSystemApi {
    private EthereumBlockChainRepository repository;

    public EthereumBlockChainSystemServiceImpl() {
        super(EthereumBlockChain.class);
    }

    @Reference
    public void setRepository(EthereumBlockChainRepository repository) {
        this.repository = repository;
    }

    @Override
    public EthereumSmartContract addContract(String contractClass, String name, String transactionReceipt, String address, long blockChainId, HyperIoTContext context) {
        return repository.addContract(contractClass, name, transactionReceipt, address, blockChainId, context);
    }

    @Override
    public EthereumSmartContract removeContractByName(String name, long blockChainId, HyperIoTContext context) {
        return repository.removeContractByName(name, blockChainId, context);
    }

    @Override
    public EthereumSmartContract removeContractByAddress(String address, long blockChainId, HyperIoTContext context) {
        return repository.removeContractByAddress(address, blockChainId, context);
    }

    @Override
    public List<EthereumSmartContract> loadContracts(String contractClass, long blockChainId, HyperIoTContext context) {
        return repository.loadContracts(contractClass, blockChainId, context);
    }

    @Override
    public EthereumSmartContract loadContractByName(String name, long blockChainId, HyperIoTContext context) {
        return repository.loadContractByName(name, blockChainId, context);
    }

    @Override
    public EthereumSmartContract loadContractByAddress(String address, long blockChainId, HyperIoTContext context) {
        return repository.loadContractByAddress(address, blockChainId, context);
    }

    @Override
    protected HyperIoTBaseRepository<EthereumBlockChain> getRepository() {
        return this.repository;
    }
}
