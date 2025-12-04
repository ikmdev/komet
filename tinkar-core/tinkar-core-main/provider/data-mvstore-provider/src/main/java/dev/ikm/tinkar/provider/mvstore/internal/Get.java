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
package dev.ikm.tinkar.provider.mvstore.internal;

import dev.ikm.tinkar.component.Stamp;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityRecordFactory;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.provider.mvstore.MVStoreProvider;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.UUID;

public class Get {
    public static MVStoreProvider singleton;

    public static ConceptEntity concept(int nid) {
        return EntityRecordFactory.make(singleton.getBytes(nid));
    }

    public static StampEntity stamp(int nid) {
        return EntityRecordFactory.make(singleton.getBytes(nid));
    }

    public static int nidForUuids(ImmutableList<UUID> uuidList) {
        return singleton.nidForUuids(uuidList);
    }

    public static int stampNid(Stamp stamp) {
        throw new UnsupportedOperationException();
    }
}
