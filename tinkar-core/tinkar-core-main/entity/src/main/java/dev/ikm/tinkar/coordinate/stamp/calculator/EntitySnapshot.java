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
package dev.ikm.tinkar.coordinate.stamp.calculator;

import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * TODO: Integrate EntitySnapshot better with ObservableEntitySnapshot
 *
 * @param <V>
 * 
 */
public class EntitySnapshot<V extends EntityVersion> {
    private final Latest<V> latestVersion;
    private final IntIdCollection latestStampIds;
    private final IntIdCollection allStampIds;
    private final Entity<V> entity;
    private final ImmutableList<V> uncommittedVersions;
    private final ImmutableList<V> historicVersions;


    public EntitySnapshot(ViewCalculator viewCalculator, int nid) {
        this(viewCalculator, Entity.provider().getEntityFast(nid));
    }

    public EntitySnapshot(ViewCalculator viewCalculator, Entity<V> entity) {
        this.entity = entity;
        this.latestVersion = viewCalculator.latest(entity);
        if (latestVersion.isPresent()) {
            this.allStampIds = latestVersion.get().entity().stampNids();
            this.latestStampIds = latestVersion.stampNids();
        } else {
            throw new IllegalStateException("No latest value: " + latestVersion);
        }

        MutableList<V> uncommittedVersions = Lists.mutable.empty();
        MutableList<V> historicVersions = Lists.mutable.empty();

        for (V version : this.entity.versions()) {
            if (version.uncommitted()) {
                uncommittedVersions.add(version);
            } else if (!latestStampIds.contains(version.stampNid())) {
                historicVersions.add(version);
            }
        }
        this.uncommittedVersions = uncommittedVersions.toImmutable();
        this.historicVersions = historicVersions.toImmutable();
    }


    //~--- methods -------------------------------------------------------------
    public int nid() {
        return this.entity.nid();
    }

    @Override
    public String toString() {
        return "CategorizedVersions{" + "uncommittedVersions=\n" + uncommittedVersions + "historicVersions=\n" +
                historicVersions + ", latestVersion=\n" + latestVersion +
                ", latestStampSequences=\n" + latestStampIds +
                ", allStampSequences=\n" + allStampIds + '}';
    }

    public ImmutableList<V> getUncommittedVersions() {
        return uncommittedVersions;
    }

    public ImmutableList<V> getHistoricVersions() {
        return historicVersions;
    }

    public Latest<V> getLatestVersion() {
        return latestVersion;
    }

    public VersionCategory getVersionCategory(EntityVersion version) {

        if (version.uncommitted()) {
            return VersionCategory.Uncommitted;
        }

        int stampNid = version.stampNid();

        if (latestStampIds.contains(stampNid)) {
            if (latestVersion.contradictions().isEmpty()) {
                return VersionCategory.UncontradictedLatest;
            }

            return VersionCategory.ContradictedLatest;
        }

        if (this.allStampIds.contains(stampNid)) {
            return VersionCategory.Prior;
        }
        // should never reach here.
        throw new IllegalStateException();
    }
}

