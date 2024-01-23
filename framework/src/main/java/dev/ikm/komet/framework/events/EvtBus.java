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


import java.util.*;

public interface EvtBus {

    static final Map<String, EvtBus> evtBusMap = new HashMap<>();

    <T extends Evt> void publish(Enum topic, T evt);

    void subscribe(Enum topic, Subscriber subscriber);

    void unsubscribe(Enum topic, Subscriber subscriber);

    static EvtBus getInstance(Class clazz) {
        if (null == evtBusMap.get(clazz.getName())) {
            EvtBus bus = (EvtBus) ServiceLoader.load(clazz).findFirst().get();
            evtBusMap.put(clazz.getName(), bus);
        }
        return evtBusMap.get(clazz.getName());
    }

    static EvtBus getInstance(String name) {
        if (null == evtBusMap.get(name)) {
            Optional<EvtBus> optBus = ServiceLoader.load(EvtBus.class)
                    .stream()
                    .filter(evtBusProvider ->
                            evtBusProvider.type().isAnnotationPresent(EvtBusName.class)
                    ).map(ServiceLoader.Provider::get).findFirst();
            if (optBus.isPresent()) {
                evtBusMap.put(name, optBus.get());
            }

        }
        return evtBusMap.get(name);
    }

}
