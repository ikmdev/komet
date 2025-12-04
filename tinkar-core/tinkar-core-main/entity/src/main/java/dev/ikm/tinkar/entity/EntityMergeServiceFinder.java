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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.service.PluggableService;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.Future;

public enum EntityMergeServiceFinder {
    INSTANCE;

    EntityMergeService service;

    EntityMergeServiceFinder() {
        Class serviceClass = EntityMergeService.class;
        ServiceLoader<EntityMergeService> serviceLoader = PluggableService.load(serviceClass);
        Optional<EntityMergeService> optionalService = serviceLoader.findFirst();
        if (optionalService.isPresent()) {
            this.service = optionalService.get();
        } else {
            throw new NoSuchElementException("No " + serviceClass.getName() +
                    " found by PluggableService...");
        }
    }

    public static EntityMergeService get() {
        if (INSTANCE.service == null) {
            throw new NoSuchElementException("No EntityMergeService found by PluggableService...");
        }
        return INSTANCE.service;
    }

    public static Future<Entity> adjudicatedMerge(Entity entityToMergeInto, Entity... entitiesToMergeFrom) {
        return get().adjudicatedMerge(entityToMergeInto, entitiesToMergeFrom);
    }
}
