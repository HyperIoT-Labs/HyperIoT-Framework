package it.acsoftware.hyperiot.base.test.containers;

import com.github.dockerjava.api.model.ExposedPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.DynamicPollInterval;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author Aristide Cittadino
 * Test Util class in order to startup needed containers easily
 */
public class HyperIoTTestContainerUtil {
    private static Logger logger = LoggerFactory.getLogger(HyperIoTTestContainerUtil.class);

    /**
     * @param path         path to compose file
     * @param portMappings map of serviceName and port to wait for
     * @return
     */
    public static DockerComposeContainer startDockerCompose(String path, Map<String, List<Integer>> portMappings, boolean start) {
        File composeFile = new File(path);
        DockerComposeContainer dockerComposeContainer = new DockerComposeContainer(composeFile);
        //waiting for containers to come up
        Iterator<String> it = portMappings.keySet().iterator();
        while (it.hasNext()) {
            String serviceName = it.next();
            if (portMappings.get(serviceName) != null) {
                for (Integer port : portMappings.get(serviceName)) {
                    dockerComposeContainer.withExposedService(serviceName, port);
                }
            }
            dockerComposeContainer.waitingFor(serviceName, Wait.forListeningPort());
        }

        if (start)
            dockerComposeContainer.start();

        return dockerComposeContainer;
    }

    /**
     *
     */
    public static Map<String, GenericContainer> justWaitForContainers(Map<String, String> imagesList, Map<String, List<Integer>> chosenPortMappings, long defaultStartupTimeputMs) {
        HashMap<String, GenericContainer> containersMap = new HashMap<>();
        Iterator<String> it = chosenPortMappings.keySet().iterator();
        while (it.hasNext()) {
            String containerName = it.next();
            try {
                List<Integer> portList = chosenPortMappings.get(containerName);
                //if port mappings have been defined
                if (portList != null) {
                    Integer[] ports = new Integer[portList.size()];
                    portList.toArray(ports);
                    logger.info("Waiting for container {} with port {}", new Object[]{containerName, ports});
                    GenericContainer container = new GenericContainer(DockerImageName.parse(imagesList.get(containerName)))
                            .withExposedPorts(ports)
                            .withStartupTimeout(Duration.ofMillis(defaultStartupTimeputMs))
                            .withReuse(true);
                    container.waitingFor(Wait.forListeningPort());
                    containersMap.put(containerName, container);
                    awaitForContainerStart(container);
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
        return containersMap;
    }

    private static void awaitForContainerStart(GenericContainer container) {
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(DynamicPollInterval.ofMillis(50))
                .pollInSameThread()
                .until(
                        () -> container.getContainerInfo(),
                        inspectContainerResponse -> {
                            Set<Integer> exposedAndMappedPorts = inspectContainerResponse
                                    .getNetworkSettings()
                                    .getPorts()
                                    .getBindings()
                                    .entrySet()
                                    .stream()
                                    .filter(it -> Objects.nonNull(it.getValue())) // filter out exposed but not yet mapped
                                    .map(Map.Entry::getKey)
                                    .map(ExposedPort::getPort)
                                    .collect(Collectors.toSet());

                            return exposedAndMappedPorts.containsAll(container.getExposedPorts());
                        });
    }
}
