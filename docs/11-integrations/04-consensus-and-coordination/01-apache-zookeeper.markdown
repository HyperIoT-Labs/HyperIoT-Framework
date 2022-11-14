# Apache Zookeeper [](id=apache-zookeeper)

Apache ZooKeeper is a centralized service for maintaining information related to configurations, naming, which also provides distributed synchronization services between systems. These capabilities are enjoyed by distributed applications/systems. 
Such technology enables the ability to deploy one's own distributed application while minimizing the code for any cluster coordination or information store.
HyperIoT Framework provides a connector for ZooKeeper called precisely HyperIoTZookeeperConnector. This connector is not only leveraged by the core itself for cluster coordination but you can leverage it in your own applications to store information or develop leader election logic for eventual operations to be performed in distributed mode.

Translated with www.DeepL.com/Translator (free version)


![Hadoop System Api Interface](../../images/zookeeper_uml.png)

The Zookeeper Connector has a very simple setup: it provides the classic two interfaces and currently defines basically two methods:

* isLeader: allows a HyperIoT node to know whether it is currently the leader of a given path on zookeeper
* addListener: allows a leadership listener to be added to a given path present on zookeeper

The developer can inject these two services without having to know any further details.

## HyperIot Cluster Coordination

l Coordination of the cluster of HyperIoT nodes is coordinated by the zookeeper connector. Currently, there are no complex operations to manage other than, for example, job scheduling.
The mechanism adopted for coordination involves the election of a cluster leader.
Each node before performing an operation can verify that he is the leader and eventually initiate or not initiate the operation to be done.
Upon activation of the Zookeeper Connector component it will register to the ZooKeeper cluster at the following mutext path:

```
/hyperiot/layers/<layer>/<nodeId>
```

Then defining in the file en.acsoftware.hyperiot.cgf the following variables :

```
it.acsoftware.hyperiot.layer=${env:HYPERIOT_LAYER:-microservices}
it.acsoftware.hyperiot.nodeId=${env:HYPERIOT_NODE_ID:-1}
```
It is possible to specify and customize the reference path.
It is very important to understand that this path will be used internally by the server to manage internal components.
It is also possible to register listeners on other zookeeper paths as we will see later.

## Developing Custom Distributed Application

If you want to have a custom application on a specific zookeeper path and manage the leadership on that path just register a component that implements the Leadership Registrar interface. 
Basically such a component will be identified in the startup phase and the connector will register it on zookepeer also launching the leader election.
Below is an explanatory example used to register a custom component for job scheduling:

```
@Component(immediate = true,property = {
        HyperIoTZookeeperConstants.ZOOKEEPER_LEADERSHIP_REGISTRAR_OSGI_FILTER+"="+JobSchedulerLeadershipRegistrar.JOB_SCHEDULER_LEADRSHIP_REGISTRAR_OSGI_FITLER_VALUE
})
public class JobSchedulerLeadershipRegistrar implements HyperIoTLeadershipRegistrar {
    private static Logger logger = LoggerFactory.getLogger(JobSchedulerLeadershipRegistrar.class);
    private static final String LEADERSHIP_PATH = "/"+HyperIoTUtil.getLayer() + "/jobs/quartz/executor";
    public static final String JOB_SCHEDULER_LEADRSHIP_REGISTRAR_OSGI_FITLER_VALUE = "hyperiot-quartz-leadership-registrar";

    @Override
    public String getLeadershipPath() {
        logger.info("*** HYPERIOT JOB SCHEUDLER CLUSTER SCHEDULER LEADERSHIP PATH: {}***",LEADERSHIP_PATH);
        return LEADERSHIP_PATH;
    }

}
```

**_NOTE:_** The path returned by getLeadershipPath will always be "appended" to zookeeper's base path <strong>/hyperiot/layers/....</strong>

Once the Leadership Registrar is registered, it is possible to figure out whether the current node is a path leader by using the SystemApi's isLeader(String mutextPath) method. To get the mutex path you can do an OSGi query to retrieve the component.

Note that the leadership registrar component has been registered with a property that then allows it to be filtered and injected into contexts where it is needed.

Below is an example:

```
...

@Component(service = JobSchedulerSystemApi.class, immediate = true)
public final class JobSchedulerSystemServiceImpl extends HyperIoTBaseSystemServiceImpl implements JobSchedulerSystemApi {
    ...
    private HyperIoTLeadershipRegistrar jobSchedulerLeadershipRegistrar;
    ...

    /**
     * This method adds a LeaderLatchListener
     */
    private void addLeaderLatchListener() {
        String leadershipPath = jobSchedulerLeadershipRegistrar.getLeadershipPath();
        zookeeperConnectorSystemApi.addListener(new LeaderLatchListener() {

            @Override
            public void isLeader() {
                getLog().info("This node has became a zk leader, start scheduler");
                try {
                    scheduler.start();
                } catch (SchedulerException e) {
                    getLog().error("Scheduler has not been started: {}", e.getMessage());
                }
            }

            @Override
            public void notLeader() {
                getLog().info("This node is not a zk leader anymore, standby scheduler");
                try {
                    scheduler.standby();
                } catch (SchedulerException e) {
                    getLog().error("Scheduler has not been paused: {}", e.getMessage());
                }
            }

        }, leadershipPath);
    }

    ...

    /**
     * Injection of the related leadership registrar using basic osgi filter
     * @param jobSchedulerLeadershipRegistrar
     */
    @Reference(target = "(" + HyperIoTZookeeperConstants.ZOOKEEPER_LEADERSHIP_REGISTRAR_OSGI_FILTER + "=" + JobSchedulerLeadershipRegistrar.JOB_SCHEDULER_LEADRSHIP_REGISTRAR_OSGI_FITLER_VALUE + ")")
    public void setJobSchedulerLeadershipRegistrar(HyperIoTLeadershipRegistrar jobSchedulerLeadershipRegistrar) {
        this.jobSchedulerLeadershipRegistrar = jobSchedulerLeadershipRegistrar;
    }

    @Reference
    public void setZookeeperConnectorSystemApi(ZookeeperConnectorSystemApi zookeeperConnectorSystemApi) {
        this.zookeeperConnectorSystemApi = zookeeperConnectorSystemApi;
    }

}

```

addLeaderLatchListener is invoked during the activation of the OSGi component. 
This registers a listener on the leadership of the mutex path associated precisely with job scheduling. As soon as a node becomes a leader, the scheduler that will manage the execution of jobs is triggered. 
Similarly when there is a change in leadership the scheduler is put on standby.

## Configurations

The connector configuration currently provides only one property, which is precisely the list of zk nodes to be contacted for connection:

```
####################################################################
############## ZOOKEEPER CONNECTOR CONFIGURATION ###################
####################################################################


it.acsoftware.hyperiot.zookeeper.connector.url=${env:HYPERIOT_ZOOKEEPER_URL:-localhost:2181}
```
