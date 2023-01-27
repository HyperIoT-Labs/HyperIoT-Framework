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

package it.acsoftware.hyperiot.websocket.policy;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Aristide Cittadino
 * Max message per second Policy
 */
public class MaxMessagesPerSecondPolicy extends HyperIoTWebSocketAbstractPolicy {
    private static Logger log = LoggerFactory.getLogger(MaxMessagesPerSecondPolicy.class.getName());

    public static final long TIME_WINDOW_MS = 1000;
    private long startTimestamp = -1;
    private long count;
    private long maxMessagesPerSecond;
    private Session session;

    public MaxMessagesPerSecondPolicy(Session s, long max) {
        super(s);
        this.count = 1;
        this.maxMessagesPerSecond = max;
    }

    @Override
    public synchronized boolean isSatisfied(Map<String, Object> params, byte[] payload) {
        log.debug( "Policy Max Message Per Second");
        long currentTimeStamp = System.currentTimeMillis();
        if (startTimestamp == -1) {
            startTimestamp = currentTimeStamp;
            return true;
        }
        long diff = currentTimeStamp - startTimestamp;
        if (diff / TIME_WINDOW_MS <= 1) {
            count++;
            log.debug( "Policy Max Message Per Second:Time Window less than 1sec, counter is: {}", count);
            if (count > maxMessagesPerSecond)
                return false;
            return true;
        } else {
            log.debug( "Policy Max Message Per Second: resetting time winwdow");
            //resetting the time window
            startTimestamp = currentTimeStamp;
            count = 1;
        }
        return true;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getCount() {
        return count;
    }

    public long getMaxMessagesPerSecond() {
        return maxMessagesPerSecond;
    }

    @Override
    public boolean closeWebSocketOnFail() {
        return true;
    }

    @Override
    public boolean printWarningOnFail() {
        return true;
    }

    @Override
    public boolean sendWarningBackToClientOnFail() {
        return false;
    }
}
