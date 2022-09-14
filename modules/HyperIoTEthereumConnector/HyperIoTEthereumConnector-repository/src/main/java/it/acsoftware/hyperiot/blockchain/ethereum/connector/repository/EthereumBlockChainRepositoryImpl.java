package it.acsoftware.hyperiot.blockchain.ethereum.connector.repository;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.repository.HyperIoTBaseRepositoryImpl;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumBlockChainRepository;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumBlockChain;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumSmartContract;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Aristide Cittadino Implementation class of the EthereumConnector. This
 * class is used to interact with the persistence layer.
 */
@Component(service = EthereumBlockChainRepository.class, immediate = true)
public class EthereumBlockChainRepositoryImpl extends HyperIoTBaseRepositoryImpl<EthereumBlockChain> implements EthereumBlockChainRepository {
    /**
     * Injecting the JpaTemplate to interact with database
     */
    private JpaTemplate jpa;

    /**
     * Constructor for a EthereumConnectorRepositoryImpl
     */
    public EthereumBlockChainRepositoryImpl() {
        super(EthereumBlockChain.class);
    }

    /**
     * @return The current jpaTemplate
     */
    @Override
    protected JpaTemplate getJpa() {
        getLog().debug("invoking getJpa, returning: {}", jpa);
        return jpa;
    }

    /**
     * @param jpa Injection of JpaTemplate
     */
    @Override
    @Reference(target = "(osgi.unit.name=hyperiot-ethereumConnector-persistence-unit)")
    protected void setJpa(JpaTemplate jpa) {
        getLog().debug("invoking setJpa, setting: " + jpa);
        this.jpa = jpa;
    }

    @Override
    public EthereumSmartContract addContract(String contractClass, String name, String transactionReceipt, String address, long blockChainId, HyperIoTContext context) {
        return this.jpa.txExpr(entityManager -> {
            EthereumBlockChain blockChain = entityManager.find(EthereumBlockChain.class, blockChainId);
            EthereumSmartContract contract = new EthereumSmartContract(contractClass, name, transactionReceipt, address, blockChain);
            blockChain.getContracts().add(contract);
            entityManager.persist(contract);
            entityManager.merge(blockChain);
            entityManager.flush();
            return contract;
        });
    }

    @Override
    public EthereumSmartContract removeContractByName(String name, long blockChainId, HyperIoTContext context) {
        return this.jpa.txExpr(entityManager -> {
            EthereumSmartContract contract = findByNameBlockChainId(entityManager, name, blockChainId);
            entityManager.remove(contract);
            entityManager.flush();
            return contract;
        });
    }

    @Override
    public EthereumSmartContract removeContractByAddress(String address, long blockChainId, HyperIoTContext context) {
        return this.jpa.txExpr(entityManager -> {
            EthereumSmartContract contract = findByAddressBlockChainId(entityManager, address, blockChainId);
            entityManager.remove(contract);
            entityManager.flush();
            return contract;
        });
    }

    @Override
    public EthereumSmartContract loadContractByName(String name, long blockChainId, HyperIoTContext context) {
        return this.jpa.txExpr(entityManager -> {
            return findByNameBlockChainId(entityManager, name, blockChainId);
        });
    }

    @Override
    public EthereumSmartContract loadContractByAddress(String address, long blockChainId, HyperIoTContext context) {
        return this.jpa.txExpr(entityManager -> {
            return findByAddressBlockChainId(entityManager, address, blockChainId);
        });
    }


    @Override
    public List<EthereumSmartContract> loadContracts(String contractClass, long blockChainId, HyperIoTContext context) {
        return this.jpa.txExpr(entityManager -> {
            List<EthereumSmartContract> contracts = findByContractsClassBlockChainId(entityManager, contractClass, blockChainId);
            return contracts;
        });
    }

    private List<EthereumSmartContract> findByContractsClassBlockChainId(EntityManager entityManager, String contractClass, long blockChainId) {
        List contracts = entityManager.createQuery("Select ethC from EthereumSmartContract as ethC where ethC.contractClass = :contractClass and ethC.blockChain.id = :id")
                .setParameter("contractClass", contractClass)
                .setParameter("id", blockChainId)
                .getResultList();
        return contracts;
    }

    private EthereumSmartContract findByNameBlockChainId(EntityManager entityManager, String name, long blockChainId) {
        EthereumSmartContract contract = (EthereumSmartContract) entityManager.createQuery("Select ethC from EthereumSmartContract as ethC where ethC.name = :name and ethC.blockChain.id = :id")
                .setParameter("name", name)
                .setParameter("id", blockChainId)
                .getSingleResult();
        return contract;
    }

    private EthereumSmartContract findByAddressBlockChainId(EntityManager entityManager, String address, long blockChainId) {
        EthereumSmartContract contract = (EthereumSmartContract) entityManager.createQuery("Select ethC from EthereumSmartContract as ethC where ethC.address = :address and ethC.blockChain.id = :id")
                .setParameter("address", address)
                .setParameter("id", blockChainId)
                .getSingleResult();
        return contract;
    }
}
