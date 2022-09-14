package it.acsoftware.hyperiot.base.test.containers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author Aristide Cittadino
 * this class has the specific goal to create dynamically docker compose files.
 * Here we have all supported technologies at now. In the future this can be trasformed in a more complex form like
 * Factory.
 * This component is based upon the assumption that all files will be deployed inside the OSGi container
 * inside the etc/containers/fragments/ folders like it is for HyperIoT test container based on karaf.
 */
public class HyperIoTDynamicContainersConfigurationBuilder {
    //Property used to skip container creation without commenting part of test code
    public static final String SKIP_CONTAINER_CREATION = "skip-container-creation";
    public static final String SKIP_CONTAINER_AWAIT = "skip-container-await";

    public static Map<String, File> paths;
    public static Map<String, Map<String, List<Integer>>> availablePortMappings;

    public static final int CONTAINER_DEFAULT_STARTUP_TIMEOUT = 12000;
    public static String ZOOKEEPER = "zookeeper";
    public static String KAFKA = "kafka";
    public static String HADOOP = "hadoop";
    public static String NAMENODE = "namenode";
    public static String DATANODE = "datanode1";
    public static String HBASE = "hbase";
    public static String HBASE_MASTER = "hbase-master";
    public static String HBASE_REGION = "hbase-region";
    public static String STORM = "storm";
    public static String STORM_SUPERVISOR = "supervisor";
    public static String STORM_NIMBUS = "nimbus";
    public static String STORM_UI = "storm-ui";
    public static String SPARK = "spark";
    public static String SPARK_MASTER = "spark-master";
    public static String SPARK_WORKER = "spark-worker-1";
    private static final String COMPOSE_SERVICE_PLACEHOLDER = "$$fragments$$";

    public static Logger logger = LoggerFactory.getLogger(HyperIoTDynamicContainersConfigurationBuilder.class);

    private static HyperIoTDynamicContainersConfigurationBuilder instance;

    static {
        paths = new HashMap<>();
        availablePortMappings = new HashMap<>();
        Map<String, List<Integer>> currentPortMappings = new HashMap<>();

        paths.put(ZOOKEEPER, new File("etc/containers/fragments/zookeeper.fragment.yml"));
        currentPortMappings.put(ZOOKEEPER, new ArrayList<>(Arrays.asList(2181)));
        availablePortMappings.put(ZOOKEEPER, currentPortMappings);

        currentPortMappings = new HashMap<>();
        paths.put(KAFKA, new File("etc/containers/fragments/kafka.fragment.yml"));
        currentPortMappings.put(KAFKA, new ArrayList<>(Arrays.asList(9092)));
        availablePortMappings.put(KAFKA, currentPortMappings);

        currentPortMappings = new HashMap<>();
        paths.put(HADOOP, new File("etc/containers/fragments/hadoop.fragment.yml"));
        currentPortMappings.put(NAMENODE, new ArrayList<>(Arrays.asList(8020, 50070)));
        currentPortMappings.put(DATANODE, new ArrayList<>(Arrays.asList(50075, 50010, 50020)));
        availablePortMappings.put(HADOOP, currentPortMappings);

        currentPortMappings = new HashMap<>();
        paths.put(HBASE, new File("etc/containers/fragments/hbase.fragment.yml"));
        currentPortMappings.put(HBASE_MASTER, new ArrayList<>(Arrays.asList(16000, 16010)));
        currentPortMappings.put(HBASE_REGION, new ArrayList<>(Arrays.asList(16020, 16030)));
        availablePortMappings.put(HBASE, currentPortMappings);

        currentPortMappings = new HashMap<>();
        paths.put(STORM, new File("etc/containers/fragments/storm.fragment.yml"));
        currentPortMappings.put(STORM_NIMBUS, new ArrayList<>(Arrays.asList(6627)));
        currentPortMappings.put(STORM_SUPERVISOR, null);
        currentPortMappings.put(STORM_UI, null);
        availablePortMappings.put(STORM, currentPortMappings);

        currentPortMappings = new HashMap<>();
        paths.put(SPARK, new File("etc/containers/fragments/spark.fragment.yml"));
        currentPortMappings.put(SPARK_MASTER, new ArrayList<>(Arrays.asList(8080, 7077, 6066)));
        currentPortMappings.put(SPARK_WORKER, new ArrayList<>(Arrays.asList(8081)));
        availablePortMappings.put(SPARK, currentPortMappings);

        paths = Collections.unmodifiableMap(paths);
        availablePortMappings = Collections.unmodifiableMap(availablePortMappings);
    }

    private Map<String, String> chosenFragments;
    private Map<String, List<Integer>> chosenPortMappings;
    private Map<String, GenericContainer> containersDefinitions;
    private String composeFileName;
    private boolean autoStart;
    private boolean alreadyBuilt;
    private DockerComposeContainer dynamicCompose;

    public static synchronized HyperIoTDynamicContainersConfigurationBuilder getInstance() {
        if (instance == null)
            instance = new HyperIoTDynamicContainersConfigurationBuilder();
        return instance;
    }

    private static Map<String, String> loadDockerImagesList() {
        Map<String, String> imagesMap = new HashMap<>();
        try {
            FileInputStream basicDockerComposeFile = new FileInputStream("etc/containers/docker-compose-svil-basic.yml");
            Yaml basicContainersDefinitions = new Yaml();
            Map<String, Object> basicContainersComposeMap = basicContainersDefinitions.load(basicDockerComposeFile);
            HashMap<String, Object> servicesList = (HashMap<String, Object>) basicContainersComposeMap.get("services");
            Iterator<String> it = servicesList.keySet().iterator();
            while (it.hasNext()) {
                String serviceName = it.next();
                HashMap<String, Object> serviceInfo = (HashMap<String, Object>) servicesList.get(serviceName);
                String image = (String) serviceInfo.get("image");
                imagesMap.put(serviceName, image);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        return imagesMap;
    }

    private HyperIoTDynamicContainersConfigurationBuilder() {
        this.reset();
    }

    public void reset() {
        this.chosenFragments = new HashMap<>();
        this.chosenPortMappings = new HashMap<>();
        this.containersDefinitions = new HashMap<>();
        this.composeFileName = UUID.randomUUID().toString();
        this.autoStart = false;
        this.alreadyBuilt = false;
        this.dynamicCompose = null;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withComposeFileName(String fileName) {
        if (alreadyBuilt)
            return this;
        this.composeFileName = fileName;
        return this;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withAutoStart() {
        if (alreadyBuilt)
            return this;
        this.autoStart = true;
        return this;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withNOAutoStart() {
        if (alreadyBuilt)
            return this;
        this.autoStart = false;
        return this;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withZookeeperContainer() {
        if (alreadyBuilt)
            return this;
        this.chosenFragments.put(ZOOKEEPER, readFileContent(paths.get(ZOOKEEPER)));
        this.chosenPortMappings.putAll(availablePortMappings.get(ZOOKEEPER));
        return this;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withKafkaContainer() {
        if (alreadyBuilt)
            return this;
        this.chosenFragments.put(KAFKA, readFileContent(paths.get(KAFKA)));
        this.chosenPortMappings.putAll(availablePortMappings.get(KAFKA));
        return this;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withHadoopContainer() {
        if (alreadyBuilt)
            return this;
        this.chosenFragments.put(HADOOP, readFileContent(paths.get(HADOOP)));
        this.chosenPortMappings.putAll(availablePortMappings.get(HADOOP));
        return this;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withHBaseContainer() {
        if (alreadyBuilt)
            return this;
        this.chosenFragments.put(HBASE, readFileContent(paths.get(HBASE)));
        this.chosenPortMappings.putAll(availablePortMappings.get(HBASE));
        return this;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withStormContainer() {
        if (alreadyBuilt)
            return this;
        this.chosenFragments.put(STORM, readFileContent(paths.get(STORM)));
        this.chosenPortMappings.putAll(availablePortMappings.get(STORM));
        return this;
    }

    public HyperIoTDynamicContainersConfigurationBuilder withSparkContainer() {
        if (alreadyBuilt)
            return this;
        this.chosenFragments.put(SPARK, readFileContent(paths.get(SPARK)));
        this.chosenPortMappings.putAll(availablePortMappings.get(SPARK));
        return this;
    }

    public DockerComposeContainer build() {
        boolean skipContainerCreation = Boolean.parseBoolean(System.getProperty(SKIP_CONTAINER_CREATION, "false"));
        boolean skipContainerAwait = Boolean.parseBoolean(System.getProperty(SKIP_CONTAINER_AWAIT, "false"));
        if (!alreadyBuilt) {
            //if skipped, the object is built but it is not started
            if (!skipContainerCreation) {
                File composeFile = createComposeTempFile();
                this.dynamicCompose = HyperIoTTestContainerUtil.startDockerCompose(composeFile.getPath(), this.chosenPortMappings, this.autoStart);
            } else if (!skipContainerAwait) {
                this.containersDefinitions = HyperIoTTestContainerUtil.justWaitForContainers(loadDockerImagesList(), this.chosenPortMappings, CONTAINER_DEFAULT_STARTUP_TIMEOUT);
            }
            alreadyBuilt = true;
            return dynamicCompose;
        }
        return dynamicCompose;
    }

    private File createComposeTempFile() {
        try {
            StringBuilder sb = new StringBuilder();
            this.chosenFragments.keySet().stream().forEach(key -> sb.append(this.chosenFragments.get(key)).append("\n\r"));
            String composeContent = readFileContent(new File("etc/containers/test-compose.yml"));
            composeContent = composeContent.replace(COMPOSE_SERVICE_PLACEHOLDER, sb.toString());
            logger.debug("COMPOSE FILE IS: \n {}", composeContent);
            File composeTemp = new File("etc/containers/" + this.composeFileName);
            FileUtils.writeByteArrayToFile(composeTemp, composeContent.getBytes(StandardCharsets.UTF_8));
            return composeTemp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readFileContent(File file) {
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
