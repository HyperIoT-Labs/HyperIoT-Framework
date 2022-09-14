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
