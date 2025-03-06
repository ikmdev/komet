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
package dev.ikm.komet.framework.observable;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.VersionCategory;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.Comparator;
import java.util.function.Predicate;

public abstract sealed class ObservableEntitySnapshot<OE extends ObservableEntity<OV, EV>,
        OV extends ObservableVersion<EV>,
        EV extends EntityVersion>
        permits ObservableConceptSnapshot, ObservablePatternSnapshot, ObservableSemanticSnapshot {
    protected final Latest<OV> latestVersion;
    protected final IntIdCollection latestStampIds;
    protected final IntIdCollection allStampIds;
    protected final OE observableEntity;
    protected final ImmutableList<OV> uncommittedVersions;
    protected final ImmutableList<OV> historicVersions;
    protected final ViewCalculator viewCalculator;
    protected MutableList<OV> processedVersions;

    public ObservableEntitySnapshot(ViewCalculator viewCalculator, OE entity) {
        this.viewCalculator = viewCalculator;
        this.observableEntity = entity;
        this.latestVersion = viewCalculator.latest(entity);
        if (latestVersion.isPresent()) {
            this.allStampIds = latestVersion.get().entity().stampNids();
            this.latestStampIds = latestVersion.stampNids();
        } else {
            throw new IllegalStateException("No latest value: " + latestVersion);
        }
        processedVersions = Lists.mutable.ofInitialCapacity(entity.versions().size());
        MutableList<OV> uncommittedVersions = Lists.mutable.empty();
        MutableList<OV> historicVersions = Lists.mutable.empty();

        for (OV version : this.observableEntity.versionProperty()) {
            processedVersions.add(version);
            if (version.uncommitted()) {
                uncommittedVersions.add(version);
            } else if (!latestStampIds.contains(version.stampNid())) {
                historicVersions.add(version);
            }
        }
        this.uncommittedVersions = uncommittedVersions.toImmutable();
        // reverse sort, oldest record at the end on seconds granularity...
        // since some changes (classification then incremental classification)
        historicVersions.sort((o1, o2) -> Long.compare(o2.time(), o1.time()));
        this.historicVersions = historicVersions.toImmutable();
    }


    //~--- methods -------------------------------------------------------------


    public ImmutableList<OV> getProcessedVersions() {
        return processedVersions.toImmutable();
    }

    public void filterProcessedVersions(Predicate<OV> filter) {
        processedVersions = processedVersions.select(filter::test);
    }

    public void sortProcessedVersions(Comparator<OV> comparator) {
        processedVersions = processedVersions.sortThis(comparator);
    }

    public void lockProcessedVersions() {
        processedVersions = processedVersions.asUnmodifiable();
    }

    public int nid() {
        return this.observableEntity.nid();
    }

    public OE observableEntity() {
        return observableEntity;
    }

    @Override
    public String toString() {
        return "Observable Snapshot{\n   latest: " + latestVersion +
                "\n   uncommitted: " + uncommittedVersions + "" +
                "\n   historic: " + historicVersions +
                "\n   latest stamps: " + latestStampIds +
                "\n   all stamps: " + allStampIds + '}';
    }

    public ImmutableList<OV> getUncommittedVersions() {
        return uncommittedVersions;
    }

    public ImmutableList<OV> getHistoricVersions() {
        return historicVersions;
    }

    public Latest<OV> getLatestVersion() {
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
