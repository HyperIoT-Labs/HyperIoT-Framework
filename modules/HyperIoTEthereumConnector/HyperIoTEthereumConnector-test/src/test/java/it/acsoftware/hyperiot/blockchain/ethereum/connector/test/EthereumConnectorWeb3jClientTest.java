package it.acsoftware.hyperiot.blockchain.ethereum.connector.test;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumBlockChainSystemApi;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumClient;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumClientFactory;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumTransactionReceipt;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.client.EthereumWeb3JClient;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumBlockChain;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumSmartContract;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.sample.DataRegistry;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.util.EthereumConnectorUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * @author Aristide Cittadino
 * This class test some basic behaviours of ethereum connector.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EthereumConnectorWeb3jClientTest extends KarafTestSupport {
    private static final String FACTORY_TYPE_WEB3J_FILTER = "(" + EthereumConnectorUtil.ETH_CONNECTOR_CLIENT_FACTORY + "=" + EthereumConnectorUtil.ETH_CONNECTOR_CLIENT_FACTORY_WEB3J + ")";
    private static final String CONTRACT_DEFAULT_NAME = "MY_DATA_CERTIFICATION";
    private static final String ACCOUNT_PRIVATE_KEY = "5c7a050c7b0e3a6896e9667a6dff3a6b389c665aaed218c352071890c05520ee";
    private static final String GANACHE_MNEMONIC = "stereo consider quality wild fat farm symptom bundle laundry side one lemon";
    private static final String GANACHE_PORT = "7547";
    private static final long CHAIN_ID = 1337;
    private static final long GAS_PRICE = 20000000000l;
    private static final long GAS_LIMIT = 6721975l;
    private static EthereumClientFactory ethereumClientFactory;
    private static EthereumClient ethereumClient;
    private static Process ganacheProcess;
    private static long localBlockChainId = 0L;

    //forcing global configuration
    @Override
    public Option[] config() {
        return null;
    }

    public HyperIoTContext impersonateUser(HyperIoTBaseRestApi restApi, HyperIoTUser user) {
        return restApi.impersonate(user);
    }

    private HyperIoTAction getHyperIoTAction(String resourceName,
                                             HyperIoTActionName action, long timeout) {
        String actionFilter = OSGiFilterBuilder
                .createFilter(HyperIoTConstants.OSGI_ACTION_RESOURCE_NAME, resourceName)
                .and(HyperIoTConstants.OSGI_ACTION_NAME, action.getName()).getFilter();
        return getOsgiService(HyperIoTAction.class, actionFilter, timeout);
    }

    @Test
    public void hyperIoTFrameworkShouldBeInstalled() throws Exception {
        // assert on an available service
        assertServiceAvailable(FeaturesService.class, 0);
        String features = executeCommand("feature:list -i");
        assertContains("HyperIoTBase-features ", features);
        assertContains("HyperIoTPermission-features ", features);
        assertContains("HyperIoTHUser-features ", features);
        assertContains("HyperIoTAuthentication-features ", features);
        assertContains("HyperIoTEthereumConnector-features ", features);
        String datasource = executeCommand("jdbc:ds-list");
        assertContains("hyperiot", datasource);
    }

    /**
     * This method initializes ganache and waits for it
     */
    @Test
    public void test0000_initGanache() {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("ganache-cli", "-m", GANACHE_MNEMONIC, "-p", GANACHE_PORT);
        try {
            ganacheProcess = pb.start();
            Assert.assertTrue(ganacheProcess.isAlive());
            System.out.println("Waiting server to start...");
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println("Error during ganache init, please install ganache with npm install -g ganache-cli");
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    /**
     * This method connects one client to the local blockchain.
     * It creates a new blockchain on localhost and save it.
     */
    @Test
    public void test0001_ConnectionToBlockChainShouldWork() {
        EthereumBlockChainSystemApi ethSystemApi = getOsgiService(EthereumBlockChainSystemApi.class, 0);
        EthereumBlockChain localBlockChain = new EthereumBlockChain("http", "localhost", GANACHE_PORT);
        localBlockChain = ethSystemApi.save(localBlockChain, null);
        Assert.assertTrue(localBlockChain != null);
        Assert.assertTrue(localBlockChain.getId() > 0);
        localBlockChainId = localBlockChain.getId();
        ethereumClientFactory = getOsgiService(EthereumClientFactory.class, FACTORY_TYPE_WEB3J_FILTER, 0);
        Assert.assertNotNull(ethereumClientFactory);
        ethereumClient = ethereumClientFactory.withEthereumBlockChain(localBlockChain).build();
        Assert.assertNotNull(ethereumClient);
    }

    /**
     * This methods retrieves the account list
     */
    @Test
    public void test0002_getAccountListShouldWork() {
        List<String> accounts = ethereumClient.listAccounts();
        Assert.assertTrue(accounts.size() > 0);
    }

    /**
     * This methods tries to transfer funds from first account to the second
     */
    @Test
    public void test0003_transferFundsShouldWork() {
        List<String> accounts = ethereumClient.listAccounts();
        try {
            String account1 = accounts.get(1);
            int etherAmount = 1;
            ethereumClient.setCredentials(ACCOUNT_PRIVATE_KEY);
            long oldBalanceAccount1 = ethereumClient.getBalanceOf(accounts.get(1)).longValue();
            EthereumTransactionReceipt receipt = ethereumClient.transferEther(account1, new BigDecimal(etherAmount));
            Assert.assertNotNull(receipt);
            Assert.assertEquals(receipt.getFrom(), accounts.get(0));
            Assert.assertEquals(receipt.getTo(), accounts.get(1));
            long newBalanceAccount1 = ethereumClient.getBalanceOf(accounts.get(1)).longValue();
            //creating the checkbalance variable equal to old value plus the relative wei amount
            long checkBalance = oldBalanceAccount1 + (etherAmount * 1000000000000000000l);
            Assert.assertTrue(newBalanceAccount1 == checkBalance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the balance of account 1
     */
    @Test
    public void test0004_getBalanceShouldWork() {
        List<String> accounts = ethereumClient.listAccounts();
        String account1 = accounts.get(1);
        ethereumClient.setCredentials(ACCOUNT_PRIVATE_KEY);
        BigInteger balance = ethereumClient.getBalanceOf(account1);
        Assert.assertTrue(balance.longValue() > 0);
    }

    /**
     * Tries to deploy solidity contract and saving the address result to local database
     * @throws Exception
     */
    @Test
    public void test0005_deployContractShouldWork() throws Exception {
        EthereumBlockChainSystemApi systemApi = getOsgiService(EthereumBlockChainSystemApi.class, 0);
        EthereumWeb3JClient web3jClient = (EthereumWeb3JClient) ethereumClient;
        Web3j web3j = web3jClient.getWeb3j();
        BigInteger gasLimit = BigInteger.valueOf(GAS_LIMIT);
        BigInteger gasPrice = BigInteger.valueOf(GAS_PRICE);
        TransactionManager transactionManager = web3jClient.createNewTransactionManager(CHAIN_ID);
        ContractGasProvider gasProvider = web3jClient.createContractGasProvider(gasPrice, gasLimit, null, null);
        DataRegistry dataRegistryContract = DataRegistry.deploy(web3j, transactionManager, gasProvider).send();
        String contractAddress = dataRegistryContract.getContractAddress();
        Assert.assertNotNull(dataRegistryContract);
        Assert.assertNotNull(contractAddress);
        Assert.assertTrue(contractAddress.length() > 0);
        //save smart contract data
        EthereumSmartContract contract = systemApi.addContract(DataRegistry.class.getName(),CONTRACT_DEFAULT_NAME, dataRegistryContract.getTransactionReceipt().get().toString(), contractAddress, localBlockChainId, null);
        Assert.assertNotNull(contract);
        Assert.assertTrue(contract.getId() > 0);
    }

    /**
     * Loads smart contract address from local database and tries to load the real contract from the blockchain
     */
    @Test
    public void test0006_loadContractShouldWork() {
        EthereumBlockChainSystemApi systemApi = getOsgiService(EthereumBlockChainSystemApi.class, 0);
        EthereumWeb3JClient web3jClient = (EthereumWeb3JClient) ethereumClient;
        Web3j web3j = web3jClient.getWeb3j();
        TransactionManager transactionManager = web3jClient.createNewTransactionManager(CHAIN_ID);
        BigInteger gasLimit = BigInteger.valueOf(GAS_LIMIT);
        BigInteger gasPrice = BigInteger.valueOf(GAS_PRICE);
        ContractGasProvider gasProvider = web3jClient.createContractGasProvider(gasPrice, gasLimit, null, null);
        EthereumSmartContract contract = systemApi.loadContractByName(CONTRACT_DEFAULT_NAME, localBlockChainId, null);
        DataRegistry dataRegistryContract = DataRegistry.load(contract.getAddress(), web3j, transactionManager, gasProvider);
        Assert.assertNotNull(dataRegistryContract);
    }

    /**
     * Loads the contract and invoke a transaction on it
     */
    @Test
    public void test0007_InteractWithContractShouldWork() {
        EthereumBlockChainSystemApi systemApi = getOsgiService(EthereumBlockChainSystemApi.class, 0);
        List<String> accounts = ethereumClient.listAccounts();
        EthereumWeb3JClient web3jClient = (EthereumWeb3JClient) ethereumClient;
        Web3j web3j = web3jClient.getWeb3j();
        BigInteger gasLimit = BigInteger.valueOf(GAS_LIMIT);
        BigInteger gasPrice = BigInteger.valueOf(GAS_PRICE);
        TransactionManager transactionManager = web3jClient.createNewTransactionManager(CHAIN_ID);
        ContractGasProvider gasProvider = web3jClient.createContractGasProvider(gasPrice, gasLimit, null, null);
        TransactionReceipt receipt = null;
        try {
            EthereumSmartContract contract = systemApi.loadContractByName(CONTRACT_DEFAULT_NAME, localBlockChainId, null);
            DataRegistry dataRegistryContract = DataRegistry.load(contract.getAddress(), web3j, transactionManager, gasProvider);
            String dataToSign = "This message must be signed and registered to the blockchain";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(dataToSign.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();
            receipt = dataRegistryContract.notarizeDocument(hash).send();
            Assert.assertTrue(receipt.getFrom().equals(accounts.get(0)));
            List<DataRegistry.NotarizedEventResponse> notarizedEvents = dataRegistryContract.getNotarizedEvents(receipt);
            Assert.assertTrue(new String(notarizedEvents.get(0)._dataHash).equals(new String(hash)));
        } catch (Exception e) {
            Assert.assertTrue(false);
            e.printStackTrace();
        }
    }

    @Test
    public void test9999_closeGanache() {
        ganacheProcess.destroy();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertFalse(ganacheProcess.isAlive());
    }

}