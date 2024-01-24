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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory class to create and return an Event Bus implementation
 */
public class EvtBusFactory {

    // collection of EvtBus implementations
    private static final Map<String, EvtBus> evtBusMap = new HashMap<>();

    private EvtBusFactory() {}

    /**
     * Get instance by class definition, this is the preferred approach.
     * @param clazz the class definition
     * @return the EvtBus implementation
     */
    public static EvtBus getInstance(Class clazz) {
        if (null == evtBusMap.get(clazz.getSimpleName())) {
            EvtBus bus = (EvtBus) ServiceLoader.load(clazz).findFirst().get();
            evtBusMap.put(clazz.getSimpleName(), bus);
        }
        return evtBusMap.get(clazz.getSimpleName());
    }

    /**
     * Get instance by class name as a String
     * @param name the class definition
     * @return the EvtBus implementation
     */
    public static EvtBus getInstance(String name) {
        if (null == evtBusMap.get(name)) {
            Optional<EvtBus> optBus = ServiceLoader.load(EvtBus.class)
                    .stream()
                    .filter(evtBusProvider ->
                            evtBusProvider.type().isAnnotationPresent(EvtBusName.class)
                                    && evtBusProvider.type().getAnnotation(EvtBusName.class).value().equals(name)
                    ).map(ServiceLoader.Provider::get).findFirst();
            if (optBus.isPresent()) {
                evtBusMap.put(name, optBus.get());
            }

        }
        return evtBusMap.get(name);
    }
}