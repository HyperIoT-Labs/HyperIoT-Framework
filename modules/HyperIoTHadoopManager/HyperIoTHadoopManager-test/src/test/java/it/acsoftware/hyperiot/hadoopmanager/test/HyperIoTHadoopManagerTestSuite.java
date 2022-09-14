package it.acsoftware.hyperiot.hadoopmanager.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestRunner;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class HyperIoTHadoopManagerTestSuite {

    public static TestSuite suite() throws Throwable {
        return HyperIoTTestRunner.createHyperIoTTestSuite("it.acsoftware.hyperiot.hadoopmanager.test");
    }

}
