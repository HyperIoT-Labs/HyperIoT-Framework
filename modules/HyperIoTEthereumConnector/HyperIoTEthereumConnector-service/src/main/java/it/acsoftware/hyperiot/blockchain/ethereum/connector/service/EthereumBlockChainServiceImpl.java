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
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.security.annotations.AllowPermissions;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntityServiceImpl;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.actions.EthereumConnectorAction;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumBlockChainApi;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumBlockChainSystemApi;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumBlockChain;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumSmartContract;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;


/**
 * @author Aristide Cittadino Implementation class of EthereumConnectorApi interface.
 * It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = EthereumBlockChainApi.class, immediate = true)
public final class EthereumBlockChainServiceImpl extends HyperIoTBaseEntityServiceImpl<EthereumBlockChain> implements EthereumBlockChainApi {
    /**
     * Injecting the EthereumConnectorSystemApi
     */
    private EthereumBlockChainSystemApi systemService;

    public EthereumBlockChainServiceImpl() {
        super(EthereumBlockChain.class);
    }

    /**
     * @return The current EthereumConnectorSystemApi
     */
    protected EthereumBlockChainSystemApi getSystemService() {
        getLog().debug("invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param ethereumConnectorSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(EthereumBlockChainSystemApi ethereumConnectorSystemService) {
        getLog().debug("invoking setSystemService, setting: {}", systemService);
        this.systemService = ethereumConnectorSystemService;
    }

    @Override
    @AllowPermissions(checkById = true, idParamIndex = 4, systemApiRef = "it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumConnectorSystemApi", actions = EthereumConnectorAction.Names.ADD_CONTRACT)
    public EthereumSmartContract addContract(String contractClass, String name, String transactionReceipt, String address, long blockChainId, HyperIoTContext context) {
        return this.systemService.addContract(contractClass, name, transactionReceipt, address, blockChainId, context);
    }

    @AllowPermissions(checkById = true, idParamIndex = 1, systemApiRef = "it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumConnectorSystemApi", actions = EthereumConnectorAction.Names.REMOVE_CONTRACT)
    @Override
    public EthereumSmartContract removeContractByName(String name, long blockChainId, HyperIoTContext context) {
        return this.systemService.removeContractByName(name, blockChainId, context);
    }

    @AllowPermissions(checkById = true, idParamIndex = 1, systemApiRef = "it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumConnectorSystemApi", actions = EthereumConnectorAction.Names.REMOVE_CONTRACT)
    @Override
    public EthereumSmartContract removeContractByAddress(String address, long blockChainId, HyperIoTContext context) {
        return this.systemService.removeContractByAddress(address, blockChainId, context);
    }

    @AllowGenericPermissions(actions = EthereumConnectorAction.Names.LOAD_CONTRACT)
    @Override
    public List<EthereumSmartContract> loadContracts(String contractClass, long blockChainId, HyperIoTContext context) {
        return this.loadContracts(contractClass, blockChainId, context);
    }

    @AllowGenericPermissions(actions = EthereumConnectorAction.Names.LOAD_CONTRACT)
    @Override
    public EthereumSmartContract loadContractByName(String name, long blockChainId, HyperIoTContext context) {
        return this.loadContractByName(name, blockChainId, context);
    }

    @AllowGenericPermissions(actions = EthereumConnectorAction.Names.LOAD_CONTRACT)
    @Override
    public EthereumSmartContract loadContractByAddress(String address, long blockChainId, HyperIoTContext context) {
        return this.systemService.loadContractByAddress(address, blockChainId, context);
    }
}
