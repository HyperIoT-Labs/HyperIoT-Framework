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