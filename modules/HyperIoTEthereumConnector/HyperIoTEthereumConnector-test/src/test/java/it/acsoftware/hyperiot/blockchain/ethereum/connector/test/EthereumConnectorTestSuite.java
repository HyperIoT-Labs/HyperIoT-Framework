package it.acsoftware.hyperiot.blockchain.ethereum.connector.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestRunner;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class EthereumConnectorTestSuite {
    public static TestSuite suite() throws Throwable {
        return HyperIoTTestRunner.createHyperIoTTestSuite("it.acsoftware.hyperiot.blockchain.ethereum.connector.test");
    }
}
