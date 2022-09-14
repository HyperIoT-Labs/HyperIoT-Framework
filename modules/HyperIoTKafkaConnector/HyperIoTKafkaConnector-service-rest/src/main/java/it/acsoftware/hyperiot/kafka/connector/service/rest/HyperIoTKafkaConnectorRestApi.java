/*
 * Copyright (C) AC Software, S.r.l. - All Rights Reserved
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Gene <generoso.martello@acsoftware.it>, March 2019
 *
 */
package it.acsoftware.hyperiot.kafka.connector.service.rest;

import io.swagger.annotations.*;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import it.acsoftware.hyperiot.base.security.rest.LoggedIn;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.kafka.connector.api.KafkaConnectorApi;
import it.acsoftware.hyperiot.kafka.connector.model.ACLConfig;
import it.acsoftware.hyperiot.kafka.connector.model.ConnectorConfig;
import it.acsoftware.hyperiot.kafka.connector.model.KafkaConnector;
import it.acsoftware.hyperiot.kafka.connector.model.TopicConfig;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteAclsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;


@SwaggerDefinition(basePath = "/kafka", info = @Info(description = "Kafka Connector API", version = "1.0.0", title = "Kafka Connector", contact = @Contact(name = "ACSoftware.it", email = "users@acsoftware.it")), securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {
        @ApiKeyAuthDefinition(key = "jwt-auth", name = "AUTHORIZATION", in = ApiKeyLocation.HEADER)}))
@Api(value = "/kafka", produces = "application/json")
@Component(service = HyperIoTKafkaConnectorRestApi.class, property = {
        "service.exported.interfaces=it.acsoftware.hyperiot.kafka.connector.service.rest.HyperIoTKafkaConnectorRestApi",
        "service.exported.configs=org.apache.cxf.rs", "org.apache.cxf.rs.address=/kafka",
        "service.exported.intents=jackson", "service.exported.intents=jwtAuthFilter",
        "service.exported.intents=swagger",
        "service.exported.intents=exceptionmapper"}, immediate = true)
@Path("")
public class HyperIoTKafkaConnectorRestApi extends HyperIoTBaseRestApi {
    private KafkaConnectorApi kafkaServiceApi;

    /**
     * @param kafkaServiceApi Injecting kafkaServiceApi
     */
    @Reference(service = KafkaConnectorApi.class)
    protected void setKafkaServiceApi(KafkaConnectorApi kafkaServiceApi) {
        getLog().debug( "invoking setKafkaMqttServiceApi, setting: {}", this.kafkaServiceApi);
        this.kafkaServiceApi = kafkaServiceApi;
    }

    @GET
    @Path("/module/status")
    @ApiOperation(value = "/hyperiot/kafka/mqtt/module/status", notes = "Simple service for checking module status", httpMethod = "GET")
    public Response checkModuleWorking() {
        getLog().debug( "In Rest Service GET /hyperiot/kafka/mqtt/module/status: ");
        return Response.ok("HyperIoT Kafka Connector Module works!").build();
    }


    /**
     * Adds a connector
     *
     * @param connectorConfig Configuration of the connector to add
     * @return The newly added MqttConnector object
     */
    @POST
    @Path("/connectors")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/storm/connectors", notes = "Add a new connector instance", httpMethod = "POST", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response add(
            @ApiParam(value = "Connector configuration", required = true)
                    ConnectorConfig connectorConfig
    ) {
        getLog().debug( "In REST Service POST /hyperiot/kafka/mqtt/connectors/add");
        /*
        // Example POST data:
        {
            "name": "Test-7",
            "topic": "streaming.8.9",
            "mqtt.server": "tcp://mqtt.hyperiot.com:1883",
            "mqtt.topic": "8/9",
            "mqtt.reconnect": true,
            "mqtt.username": "user123",
            "mqtt.password": "pass456",
            "max.poll.interval.ms": 500,
            "tasks.max": 1
        }
        */
        try {
            KafkaConnector connector = kafkaServiceApi.addNewConnector(this.getHyperIoTContext(), connectorConfig.getName(), connectorConfig);
            return Response.ok().entity(connector).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

    /**
     * Updates a connector.
     *
     * @param instanceName    Name of the connector to update
     * @param connectorConfig The new connector configuration
     * @return The MqttConnector object with the new configuration
     */
    @PUT
    @Path("/connectors/{instance_name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/storm/connectors/{instance_name}/update", notes = "Update connector configuration", httpMethod = "PUT", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response update(
            @ApiParam(value = "Connector name", required = true)
            @PathParam("instance_name")
                    String instanceName,
            @ApiParam(value = "Connector configuration", required = true)
                    ConnectorConfig connectorConfig
    ) {
        getLog().debug( "In REST Service PUT /hyperiot/kafka/mqtt/connectors/{}", instanceName);
        /*
        // Example PUT data:
        {
            "name": "Test-7",
            "topic": "streaming.8.9",
            "mqtt.server": "tcp://mqtt.hyperiot.com:1883",
            "mqtt.topic": "8/9",
            "mqtt.reconnect": true,
            "mqtt.username": "user123",
            "mqtt.password": "pass456",
            "max.poll.interval.ms": 500,
            "tasks.max": 1
        }
        */
        try {
            KafkaConnector connector = kafkaServiceApi.updateConnector(this.getHyperIoTContext(), instanceName, connectorConfig);
            return Response.ok().entity(connector).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

    /**
     * Deletes a connector
     *
     * @param instanceName Name of the connector to delete
     * @return
     */
    @DELETE
    @Path("/connectors/{instance_name}")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/kafka/connectors/{instance_name}", notes = "Delete connector", httpMethod = "GET", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response delete(
            @ApiParam(value = "name of the connector to delete", required = true)
            @PathParam("instance_name")
                    String instanceName
    ) {
        getLog().debug( "In REST Service GET /hyperiot/kafka/connectors/{}", instanceName);
        try {
            kafkaServiceApi.deleteConnector(this.getHyperIoTContext(), instanceName, true);
            return Response.ok().entity("Connector deleted.").build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

    /**
     * Creates a topic with required information
     *
     * @return
     */
    @POST
    @Path("/topic")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/kafka/topics", notes = "Creates signle topic on Kafka", httpMethod = "POST", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response createTopic(
            @ApiParam(value = "Topic config for creation", required = true) TopicConfig topicConfig

    ) {
        getLog().debug( "In REST Service POST /hyperiot/kafka/topics: {}", topicConfig);
        try {
            CreateTopicsResult result = kafkaServiceApi.adminCreateTopic(this.getHyperIoTContext(), topicConfig.getTopic(), topicConfig.getNumPartition(), topicConfig.getReplicationFactor());
            return Response.ok().entity(result).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

    /**
     * Creates a topic with required information
     *
     * @return
     */
    @POST
    @Path("/topics")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/kafka/topics", notes = "creates multiple topics on Kafka", httpMethod = "POST", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response createMultipleTopic(
            @ApiParam(value = "Topic config for creation", required = true) TopicConfig[] topicsConfig

    ) {
        getLog().debug( "In REST Service PUT /hyperiot/kafka/topics: {}", Arrays.toString(topicsConfig));
        try {
            String[] topics = new String[topicsConfig.length];
            int[] numPartitions = new int[topicsConfig.length];
            short[] numReplicas = new short[topicsConfig.length];

            for (int i = 0; i < topicsConfig.length; i++) {
                topics[i] = topicsConfig[i].getTopic();
                numReplicas[i] = topicsConfig[i].getReplicationFactor();
                numPartitions[i] = topicsConfig[i].getNumPartition();
            }
            CreateTopicsResult result = kafkaServiceApi.adminCreateTopic(this.getHyperIoTContext(), topics, numPartitions, numReplicas);
            return Response.ok().entity(result).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

    /**
     * Creates a topic with required information
     *
     * @return
     */
    @DELETE
    @Path("/topics")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/kafka/topics", notes = "Drops topics", httpMethod = "DELETE", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response dropTopics(
            @ApiParam(value = "Topic list for deletion", required = true) String[] topics
    ) {
        getLog().debug( "In REST Service DELETE /hyperiot/kafka/topics: {}", Arrays.toString(topics));
        try {
            DeleteTopicsResult result = kafkaServiceApi.adminDropTopic(this.getHyperIoTContext(), Arrays.asList(topics));
            return Response.ok().entity(result).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }


    /**
     * Method for controlling access on Kafka resources
     * @param aclConfig
     * @return
     */
    /**
     * Creates a topic with required information
     *
     * @return
     */
    @POST
    @Path("/acl")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/kafka/acl", notes = "Creates ACL on Kafka", httpMethod = "POST", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response addACLs(@ApiParam(value = "ACL definition for creation", required = true) ACLConfig aclConfig) {
        getLog().debug( "In REST Service POST /hyperiot/acl: {}", aclConfig);
        try {
            CreateAclsResult result = kafkaServiceApi.adminAddACLs(this.getHyperIoTContext(), aclConfig.getUsername(), aclConfig.getPermissions());
            return Response.ok().entity(result).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

    /**
     * @return DeleteAclsResult
     */
    @DELETE
    @Path("/acl")
    @Produces(MediaType.APPLICATION_JSON)
    @LoggedIn
    @ApiOperation(value = "/hyperiot/kafka/acl", notes = "Creates ACL on Kafka", httpMethod = "DELETE", produces = "application/json", authorizations = @Authorization("jwt-auth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 403, message = "Not authorized"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    public Response adminDeleteACLs(@ApiParam(value = "ACL definition for deletion", required = true) ACLConfig aclConfig) {
        getLog().debug( "In REST Service DELETE /hyperiot/acl: {}", aclConfig);
        try {
            DeleteAclsResult result = kafkaServiceApi.adminDeleteACLs(this.getHyperIoTContext(), aclConfig.getUsername(), aclConfig.getPermissions());
            return Response.ok().entity(result).build();
        } catch (Throwable e) {
            return handleException(e);
        }
    }

}
