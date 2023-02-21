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

package it.acsoftware.hyperiot.blockchain.ethereum.connector.api;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumBlockChain;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumSmartContract;

import java.util.List;

/**
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 * interface defines methods for additional operations.
 */
public interface EthereumBlockChainSystemApi extends HyperIoTBaseEntitySystemApi<EthereumBlockChain> {
    /**
     * @param contractClass
     * @param name
     * @param transactionReceipt
     * @param address
     * @param blockChainId
     * @param context
     */
    EthereumSmartContract addContract(String contractClass, String name, String transactionReceipt, String address, long blockChainId, HyperIoTContext context);

    /**
     *
     * @param name
     * @param context
     * @return
     */
    EthereumSmartContract removeContractByName(String name,long blockChainId,HyperIoTContext context);

    /**
     *
     * @param address
     * @param blockChainId
     * @param context
     * @return
     */
    EthereumSmartContract removeContractByAddress(String address,long blockChainId,HyperIoTContext context);

    /**
     * @param contractClass
     * @param blockChainId
     * @param context
     * @return
     */
    List<EthereumSmartContract> loadContracts(String contractClass, long blockChainId, HyperIoTContext context);

    /**
     *
     * @param name
     * @param context
     * @return
     */
    EthereumSmartContract loadContractByName(String name,long blockChainId, HyperIoTContext context);

    /**
     *
     * @param address
     * @param blockChainId
     * @param context
     * @return
     */
    EthereumSmartContract loadContractByAddress(String address,long blockChainId, HyperIoTContext context);
}