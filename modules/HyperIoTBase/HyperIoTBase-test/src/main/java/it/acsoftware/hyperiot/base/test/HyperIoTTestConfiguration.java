/*
 * Copyright (C) AC Software, S.r.l. - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 */
package it.acsoftware.hyperiot.base.test;

import org.apache.karaf.itests.KarafTestSupport;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

/**
 * Author Aristide Cittadino
 * Helper class for tests
 */
public class HyperIoTTestConfiguration {
    private static final String ACS_MAVEN_REPO = "https://nexus.acsoftware.it/nexus/repository/maven-hyperiot";
    private static final String JACOCO_PATH = "jacoco/jacoco-0.8.7";
    private static final String JACOCO_JAR_PATH = JACOCO_PATH + "/lib/jacocoagent.jar";
    private static Logger log = LoggerFactory.getLogger(HyperIoTTestConfiguration.class.getName());
    public static final String MIN_RMI_SERVER_PORT = "44444";
    public static final String MAX_RMI_SERVER_PORT = "65534";
    public static final String MIN_HTTP_PORT = "9080";
    public static final String MAX_HTTP_PORT = "9999";
    public static final String MIN_RMI_REG_PORT = "1099";
    public static final String MAX_RMI_REG_PORT = "9999";
    public static final String MIN_SSH_PORT = "8101";
    public static final String MAX_SSH_PORT = "8888";

    private String testSuiteName;
    private String codeCoverageReportPath;
    private String codeCoverageClassesPath;

    private String distributionGroupId;
    private String distributionArtifactId;
    private String distributionVersion;
    private String distributionRepo;

    private String httpPort;
    private String rmiRegistryPort;
    private String rmiServerPort;
    private String sshPort;
    private String hyperiotRuntimeTestVersion;
    private String karafVersion;

    private Option[] options;

    public HyperIoTTestConfiguration(String karafVersion, String hyperIoTRuntimeTestVersion, String testSuiteName) {
        httpPort = Integer.toString(KarafTestSupport.getAvailablePort(
                Integer.parseInt(MIN_HTTP_PORT), Integer.parseInt(MAX_HTTP_PORT)));
        rmiRegistryPort = Integer.toString(KarafTestSupport.getAvailablePort(
                Integer.parseInt(MIN_RMI_REG_PORT), Integer.parseInt(MAX_RMI_REG_PORT)));
        rmiServerPort = Integer.toString(KarafTestSupport.getAvailablePort(
                Integer.parseInt(MIN_RMI_SERVER_PORT), Integer.parseInt(MAX_RMI_SERVER_PORT)));
        sshPort = Integer.toString(KarafTestSupport.getAvailablePort(Integer.parseInt(MIN_SSH_PORT),
                Integer.parseInt(MAX_SSH_PORT)));
        this.hyperiotRuntimeTestVersion = hyperIoTRuntimeTestVersion;
        this.karafVersion = karafVersion;
        this.options = getBaseConfig();
        this.testSuiteName = testSuiteName;
        //default saves in target folder
        this.codeCoverageClassesPath = "../../../build/jacoco/classes";
        this.codeCoverageReportPath = "../../../build/jacoco";
        this.distributionGroupId = "it.acsoftware.hyperiot.container";
        this.distributionArtifactId = "hyperiot-karaf-distribution-test";
        this.distributionRepo = ACS_MAVEN_REPO;
        this.distributionVersion = this.hyperiotRuntimeTestVersion;
        log.info("SSH PORT: {}", new Object[]{sshPort});
    }

    public HyperIoTTestConfiguration(String karafVersion, String hyperIoTRuntimeTestVersion) {
        this(karafVersion, hyperIoTRuntimeTestVersion, "HyperIoTTest");
    }

    public HyperIoTTestConfiguration withDistribution(String groupId, String artifactId, String version) {
        this.distributionGroupId = groupId;
        this.distributionArtifactId = artifactId;
        this.distributionVersion = version;
        return this;
    }

    public HyperIoTTestConfiguration withDistributionRepo(String distributionRepoUrl) {
        this.distributionRepo = distributionRepoUrl;
        return this;
    }

    public HyperIoTTestConfiguration withDebug(String port, boolean hold) {
        Option[] debugConfig = {debugConfiguration(port, hold)};
        return append(debugConfig);
    }

    public HyperIoTTestConfiguration keepRuntime() {
        Option opt = KarafDistributionOption.keepRuntimeFolder();
        return append(new Option[]{opt});
    }

    public HyperIoTTestConfiguration withLogLevel(LogLevelOption.LogLevel level) {
        Option opt = KarafDistributionOption.logLevel(level);
        return append(new Option[]{opt});
    }

    public HyperIoTTestConfiguration withCodeCoverageReportPath(String reportPath) {
        this.codeCoverageReportPath = reportPath;
        return this;
    }

    public HyperIoTTestConfiguration withCodeCoverageClassesPath(String classesPath) {
        this.codeCoverageClassesPath = classesPath;
        return this;
    }

    public HyperIoTTestConfiguration withPropertyUpdated(String configFile, String propName, String value) {
        append(new Option[]{editConfigurationFilePut(configFile,
                propName, value)});
        return this;
    }

    public HyperIoTTestConfiguration withEnvironmentVariable(String name, String value) {
        append(new Option[]{environment(name + "=" + value)});
        return this;
    }

    public HyperIoTTestConfiguration withCodeCoverage(String... packageFilter) {
        StringBuilder sb = new StringBuilder();
        Arrays.asList(packageFilter).stream().forEach(packageStr -> {
            if (sb.length() > 0)
                sb.append(":");
            sb.append(packageStr);
        });
        return append(new Option[]{createCodeCoverageOption(this.testSuiteName, sb.toString(), this.codeCoverageReportPath, this.codeCoverageClassesPath)});
    }

    public HyperIoTTestConfiguration withXms(String xms) {
        return append(new Option[]{vmOption("-Xms" + xms)});
    }

    public HyperIoTTestConfiguration withXmx(String xmx) {
        return append(new Option[]{vmOption("-Xmx" + xmx)});
    }

    public HyperIoTTestConfiguration withSkipContainerCreation() {
        append(new Option[]{vmOption("-Dskip-container-creation=true")});
        return this;
    }

    public HyperIoTTestConfiguration append(Option[] customOptions) {
        Option[] config = new Option[this.options.length + customOptions.length];
        System.arraycopy(this.options, 0, config, 0, this.options.length);
        System.arraycopy(customOptions, 0, config, options.length, customOptions.length);
        this.options = config;
        return this;
    }

    public Option[] build() {
        //propagating prop to skip container creation if it is passed to test runner
        if (Boolean.parseBoolean(System.getProperty("skip-container-creation", "false"))) {
            withSkipContainerCreation();
        }

        System.setProperty("org.ops4j.pax.url.mvn.repositories", this.distributionRepo);
        MavenArtifactUrlReference karafUrl = maven().groupId(distributionGroupId)
                .artifactId(distributionArtifactId).version(distributionVersion).type("tar.gz");
        Option[] distributionOption = new Option[]{
                karafDistributionConfiguration().frameworkUrl(karafUrl)
                        .name("HyperIoT Karaf Distribution")
                        .unpackDirectory(new File("target/exam"))
                        .useDeployFolder(false)
        };
        append(distributionOption);
        append(configureVmOptions());
        return this.options;
    }

    //Method used only for java version > 9 in order to avoid pax exam freezing on startup
    protected Option[] configureVmOptions() {
        int javaVersion = Integer.parseInt(System.getProperty("java.version").split("\\.")[0]);
        if (javaVersion >= 9) {
            return options(
                    systemProperty("pax.exam.osgi.`unresolved.fail").value("true"),
                    vmOption("--add-reads=java.xml=java.logging"),
                    vmOption("--add-exports=java.base/org.apache.karaf.specs.locator=java.xml,ALL-UNNAMED"),
                    vmOption("--patch-module"),
                    vmOption(
                            "java.base=lib/endorsed/org.apache.karaf.specs.locator-"
                                    + System.getProperty("karafVersion", karafVersion)
                                    + ".jar"),
                    vmOption("--patch-module"),
                    vmOption(
                            "java.xml=lib/endorsed/org.apache.karaf.specs.java.xml-"
                                    + System.getProperty("karafVersion", karafVersion)
                                    + ".jar"),
                    vmOption("--add-opens"),
                    vmOption("java.base/java.security=ALL-UNNAMED"),
                    vmOption("--add-opens"),
                    vmOption("java.base/java.net=ALL-UNNAMED"),
                    vmOption("--add-opens"),
                    vmOption("java.base/java.lang=ALL-UNNAMED"),
                    vmOption("--add-opens"),
                    vmOption("java.base/java.util=ALL-UNNAMED"),
                    vmOption("--add-opens"),
                    vmOption("java.base/jdk.internal.reflect=ALL-UNNAMED"),
                    vmOption("--add-opens"),
                    vmOption("java.naming/javax.naming.spi=ALL-UNNAMED"),
                    vmOption("--add-opens"),
                    vmOption("java.rmi/sun.rmi.transport.tcp=ALL-UNNAMED"),
                    vmOption("--add-exports=java.base/sun.net.www.protocol.http=ALL-UNNAMED"),
                    vmOption("--add-exports=java.base/sun.net.www.protocol.https=ALL-UNNAMED"),
                    vmOption("--add-exports=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"),
                    vmOption("--add-exports=jdk.naming.rmi/com.sun.jndi.url.rmi=ALL-UNNAMED"),
                    vmOption("-classpath"),
                    vmOption("lib/jdk9plus/*" + File.pathSeparator + "lib/boot/*"),
                    // avoid integration tests stealing focus on OS X
                    vmOption("-Djava.awt.headless=true"),
                    vmOption("-Dfile.encoding=UTF8"));
        }
        return new Option[]{};
    }


    /**
     * @deprecated Replaced by {@link #append(Option[])}
     */
    @Deprecated
    public HyperIoTTestConfiguration addCustomTestConfiguration(Option[] customOptions) {
        return append(customOptions);
    }

    /**
     * @param name
     * @param packageFilter
     * @param reportFolderPath
     * @param classesFolderPath
     * @return
     */
    private Option createCodeCoverageOption(String name, String packageFilter, String reportFolderPath, String classesFolderPath) {
        reportFolderPath = (reportFolderPath.endsWith("/") || reportFolderPath.endsWith("\\")) ? reportFolderPath : reportFolderPath + "/";
        classesFolderPath = (classesFolderPath.endsWith("/") || classesFolderPath.endsWith("\\")) ? classesFolderPath : classesFolderPath + "/";
        packageFilter = (packageFilter.endsWith(".*")) ? packageFilter : packageFilter + ".*";
        Option opt = new VMOption("-javaagent:" + JACOCO_JAR_PATH + "=destfile=" + reportFolderPath + name + ".exec,includes=" + packageFilter + ",classdumpdir=" + classesFolderPath);
        return opt;
    }

    private Option[] getBaseConfig() {
        return new Option[]{
                propagateSystemProperties("org.ops4j.pax.url.mvn.repositories"),
                // enable JMX RBAC security, thanks to the KarafMBeanServerBuilder
                configureSecurity().disableKarafMBeanServerBuilder(),
                // Setting test mode ON
                mavenBundle().groupId("org.apache.karaf.itests").artifactId("common")
                        .version(this.karafVersion),
                editConfigurationFilePut("etc/it.acsoftware.hyperiot.cfg",
                        "it.acsoftware.hyperiot.testMode", "true"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port",
                        httpPort),
                editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort",
                        rmiRegistryPort),
                editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort",
                        rmiServerPort),
                editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort)};
    }

}
