/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 */
package dev.ikm.komet.framework.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EvtBusName("AmplifyEvtBus")
public class AmplifyEvtBus implements EvtBus {

    private static final Logger LOG = LoggerFactory.getLogger(dev.ikm.komet.framework.events.DefaultEvtBus.class);

    public AmplifyEvtBus() {}

    private Map<Enum, List<Subscriber>> subscribersMap = new HashMap<>();

    /**
     * @param topic
     * @param evt
     * @param <T>
     */
    @Override
    public <T extends Evt> void publish(Enum topic, T evt) {
        // if there is no topic then create one as a String
        LOG.info(evt.getSource().toString());
        List<Subscriber> subscribers = subscribersMap.get(topic);
        if (null != subscribers && !subscribers.isEmpty()) {
            subscribers.forEach(s -> s.handle(evt));
        }
    }

    /**
     * @param topic
     * @param subscriber
     */
    @Override
    public void subscribe(Enum topic, Subscriber subscriber) {
        LOG.info(subscriber.toString());
        List<Subscriber> subscribers = subscribersMap.get(topic);
        if (null == subscribers) {
            subscribers = new ArrayList<>();
            subscribers.add(subscriber);
            subscribersMap.put(topic, subscribers);
        } else {
            subscribers.add(subscriber);
        }

    }

    /**
     * @param topic
     * @param subscriber
     */
    @Override
    public void unsubscribe(Enum topic, Subscriber subscriber) {
        List<Subscriber> subscribers = subscribersMap.get(topic);
        if (null != subscribers && !subscribers.isEmpty()) {
            subscribers.removeIf(subscriber::equals);
        }
    }

}
