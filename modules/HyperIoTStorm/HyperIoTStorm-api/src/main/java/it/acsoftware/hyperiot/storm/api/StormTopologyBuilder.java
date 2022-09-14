package it.acsoftware.hyperiot.storm.api;

import org.apache.storm.Config;
import org.apache.storm.generated.StormTopology;

public interface StormTopologyBuilder {

    StormTopology configureTopology(Config config);

}
