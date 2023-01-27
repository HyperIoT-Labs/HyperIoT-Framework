/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.websocket.test;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseRestApi;
import it.acsoftware.hyperiot.base.test.containers.HyperIoTDynamicContainersConfigurationBuilder;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketBasicCommandType;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterCoordinator;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterMessageBroker;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.channel.HyperIoTWebSocketChannelType;
import it.acsoftware.hyperiot.websocket.model.message.HyperIoTWebSocketMessageType;
import it.acsoftware.hyperiot.websocket.test.client.HyperIoTChannelParticipanPlain;
import it.acsoftware.hyperiot.websocket.test.client.HyperIoTChannelParticipant;
import it.acsoftware.hyperiot.websocket.test.client.HyperIoTChannelWebSocketClient;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.itests.KarafTestSupport;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import java.util.concurrent.TimeUnit;

/**
 * @author Aristide Cittadino Interface component for WebSocket System Service.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebSocketChannelTest extends KarafTestSupport {
    public static final String WS_LOCAL_URL = "ws://localhost:8182/hyperiot/ws/test-channel";

    //forcing global configuration
    @Override
    public Option[] config() {
        Awaitility.setDefaultPollInterval(10, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.TWO_SECONDS);
        Awaitility.setDefaultTimeout(Duration.ONE_MINUTE);
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

    private HyperIoTWebSocketChannel getChannel(String channelName) {
        HyperIoTWebSocketChannelClusterCoordinator coordinator = getOsgiService(HyperIoTWebSocketChannelClusterCoordinator.class);
        HyperIoTWebSocketChannelManager channelManager = coordinator.getRegisteredWebSocketChannelManager();
        Assert.assertTrue(channelManager.channelExists(channelName));
        HyperIoTWebSocketChannel channel = channelManager.getAvailableChannels().stream().filter(curChannel -> curChannel.getChannelId().equals(channelName)).findAny().orElse(null);
        Assert.assertNotNull(channel);
        return channel;
    }

    private void assertChannelSize(String channelName, int size) {
        HyperIoTWebSocketChannel channel = getChannel(channelName);
        Assert.assertTrue(channel.getPartecipantsInfo().size() == size);
    }

    private void assertChannelContainsUser(String channelName, String username) {
        HyperIoTWebSocketChannel channel = getChannel(channelName);
        Assert.assertTrue(channel.getPartecipantsInfo().stream().filter(userInfo -> userInfo.getUsername().equals(username)).findAny().isPresent());
    }

    private void assertChannelNotContainsUser(String channelName, String username) {
        HyperIoTWebSocketChannel channel = getChannel(channelName);
        Assert.assertTrue(channel.getPartecipantsInfo().stream().filter(userInfo -> !userInfo.getUsername().equals(username)).findAny().isPresent());
    }

    @Before
    public void initContainer() {
        HyperIoTDynamicContainersConfigurationBuilder.getInstance()
                .withAutoStart()
                .withZookeeperContainer()
                .withKafkaContainer()
                .build();
    }

    @Test
    public void test000_hyperIoTFrameworkShouldBeInstalled() throws Exception {
        // assert on an available service
        assertServiceAvailable(FeaturesService.class, 0);
        String features = executeCommand("feature:list -i");
        assertContains("HyperIoTBase-features ", features);
        assertContains("HyperIoTPermission-features ", features);
        assertContains("HyperIoTHUser-features ", features);
        assertContains("HyperIoTAuthentication-features ", features);
        assertContains("HyperIoTKafkaConnector-features ", features);
        assertContains("HyperIoTZookeeperConnector-features ", features);
        assertContains("HyperIoTWebSocket-features ", features);
        String datasource = executeCommand("jdbc:ds-list");
        assertContains("hyperiot", datasource);
    }

    private HyperIoTChannelParticipant createAndConnectNewParticipant(String alias) {
        HyperIoTChannelParticipant participant = new HyperIoTChannelParticipanPlain(alias, WS_LOCAL_URL, new HyperIoTChannelWebSocketClient(), 1, true);
        participant.connectParticipant();
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.CONNECTION_OK, null);
        return participant;
    }

    @Test
    public void test002_ChannelManagerAndCoordinatorShouldBeAvailable() throws Exception {
        // assert on an available service
        HyperIoTWebSocketChannelClusterMessageBroker messageBroker = getOsgiService(HyperIoTWebSocketChannelClusterMessageBroker.class);
        HyperIoTWebSocketChannelClusterCoordinator coordinator = getOsgiService(HyperIoTWebSocketChannelClusterCoordinator.class);
        Assert.assertNotNull(messageBroker);
        Assert.assertNotNull(coordinator);
    }

    @Test
    public void test003_clientsShouldConnectPlain() throws Exception {
        // assert on an available service
        createAndConnectNewParticipant("participant1");
        createAndConnectNewParticipant("participant2");
    }

    @Test
    public void test004_clientShouldCreateAndJoinChannel() throws Exception {
        // assert on an available service
        String newChannelName = "new-channel";
        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(newChannelName, newChannelName, HyperIoTWebSocketChannelType.PLAIN, "1");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        assertChannelSize(newChannelName, 1);
    }

    @Test
    public void test005_maxParticipantsShouldNotBeExceeded() throws Exception {
        // assert on an available service
        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        String channelName = "new-channel";
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "1");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        //max participant is 1 , so user should not be available to join
        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        //access is denied
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.ERROR, null);
        assertChannelSize(channelName, 1);
        assertChannelContainsUser(channelName, owner.getUsername());
        assertChannelNotContainsUser(channelName, participant.getUsername());
    }

    @Test
    public void test006_partcipantsShouldReceiveJoinNotifications() throws Exception {
        // assert on an available service
        String channelName = "new-channel";
        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);

        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        assertChannelSize(channelName,2);
    }

    @Test
    public void test007_partcipantsShouldReceiveLeaveNotifications() throws Exception {
        // assert on an available service
        String channelName = "new-channel";
        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        //max participant is 1 , so user should not be available to join
        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);

        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        participant.leaveChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "CHANNEL_LEAVED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_GONE, null);

        assertChannelSize(channelName,1);
    }

    @Test
    public void test008_partcipantsShouldExchangeMessagesInsideChannel() throws Exception {
        // assert on an available service
        String channelName = "new-channel";
        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        participant.sendMessage("ciao", channelName);
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.APPLICATION, "ciao");
    }

    @Test
    public void test009_messagesCannotBeSentOutsideChannels() throws Exception {
        String channelName = "new-channel";
        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);
        //leaving channel
        participant.leaveChannel(channelName);

        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "CHANNEL_LEAVED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_GONE, null);

        assertChannelSize(channelName,1);

        participant.sendMessage("ciao", channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.ERROR, "Unauthorized to send message");
    }

    @Test
    public void test010_ownerCanKickParticipant() throws Exception {
        String channelName = "new-channel";

        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        owner.kickUser(channelName, participant.getUsername(), owner.getUsername(), "ciao ciao!");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_KICKED, "ciao ciao!");
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_KICKED, "ciao ciao!");

        assertChannelSize(channelName,1);
    }

    @Test
    public void test011_participantCannotKickOthers() throws Exception {
        String channelName = "new-channel";

        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        participant.kickUser(channelName, participant.getUsername(), owner.getUsername(), "ciao ciao!");
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.ERROR, "You do not have permissions to perform kick!");

        assertChannelSize(channelName,2);
    }

    @Test
    public void test012_ownerCanBanParticipant() throws Exception {
        String channelName = "new-channel";

        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        owner.banUser(channelName, participant.getUsername(), owner.getUsername(), "ciao ciao!");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_KICKED, "ciao ciao!");
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_KICKED, "ciao ciao!");

        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.ERROR, "Cannot join channel new-channel, you have been banned!");
        assertChannelSize(channelName,1);

        owner.unbanUser(channelName,"127.0.0.1",participant.getUsername());
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "UNBANNED");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);
    }

    @Test
    public void test013_participantCannotBanOthers() throws Exception {
        String channelName = "new-channel";

        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        participant.banUser(channelName, owner.getUsername(), participant.getUsername(), "ciao ciao!");
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.ERROR, "You do not have permissions to ban!");
    }

    @Test
    public void test014_ownerCanDeleteChannel() throws Exception {
        String channelName = "new-channel";
        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        owner.deleteChannel(channelName);
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "CHANNEL_DELETED");

        owner.sendMessage("prova",channelName);
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.ERROR, "Channel new-channel not found!");
    }

    @Test
    public void test015_participantsCannotDeleteChannel() throws Exception {
        String channelName = "new-channel";
        HyperIoTChannelParticipant owner = createAndConnectNewParticipant("owner");
        owner.createChannel(channelName, channelName, HyperIoTWebSocketChannelType.PLAIN, "2");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participant");
        participant.joinChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        owner.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        participant.deleteChannel(channelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.ERROR, "Channel not found or you don't have permissions to delete it");
    }

    @Test
    public void test016_channelsShouldBeIsolated() throws Exception {
        String firstChannelName = "new-channel";
        String secondChannelName = "new-channel-2";
        HyperIoTChannelParticipant ownerChannel = createAndConnectNewParticipant("ownerFirstChannel");
        ownerChannel.createChannel(firstChannelName, firstChannelName, HyperIoTWebSocketChannelType.PLAIN, "2");

        HyperIoTChannelParticipant ownerSecondChannel = createAndConnectNewParticipant("ownerSecondChannel");
        ownerSecondChannel.createChannel(secondChannelName, secondChannelName, HyperIoTWebSocketChannelType.PLAIN, "2");

        HyperIoTChannelParticipant participant = createAndConnectNewParticipant("participantBoth");
        //join first channel
        participant.joinChannel(firstChannelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        ownerChannel.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        //join second channel
        participant.joinChannel(secondChannelName);
        participant.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.OK, "SUCCESFULLY_JOINED");
        ownerSecondChannel.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND, HyperIoTWebSocketMessageType.PARTICIPANT_ADDED, null);

        assertChannelSize(firstChannelName,2);
        assertChannelSize(secondChannelName,2);

        participant.sendMessage("ciao-first-channel",firstChannelName);
        ownerChannel.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND,HyperIoTWebSocketMessageType.APPLICATION,"ciao-first-channel");
        participant.sendMessage("ciao-second-channel",secondChannelName);
        ownerSecondChannel.awaitForMessage(HyperIoTWebSocketBasicCommandType.READ_MESSAGE_COMMAND,HyperIoTWebSocketMessageType.APPLICATION,"ciao-second-channel");
    }

}