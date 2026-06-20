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
package dev.ikm.komet.framework.observable.read;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.VersionCategory;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinate-relative reads available on an
 * {@link dev.ikm.komet.framework.observable.ObservableEntitySnapshot}. The snapshot already carries
 * its {@link ViewCalculator} and resolved versions, so these reads take <em>no</em> coordinate
 * argument — the coordinate is intrinsic to the snapshot. (A chronology, lacking a coordinate, must
 * be handed one and would simply delegate to {@code getSnapshot(calc)}.)
 *
 * <p>This is a default-method capability mixin: the snapshot implements it by already providing the
 * required accessors below, so it gains these reads with only an {@code implements} clause and a
 * {@code viewCalculator()} accessor — no method bodies in the snapshot class.
 */
public interface SnapshotReads {

    // ---- required accessors, all already provided by ObservableEntitySnapshot ----

    /** The view calculator (coordinate) this snapshot was computed under. */
    ViewCalculator viewCalculator();

    /** This entity's nid. */
    int nid();

    /** The observable entity this is a snapshot of. */
    ObservableEntity<?> observableEntity();

    /** The latest version under the coordinate. */
    Latest<? extends EntityVersion> getLatestVersion();

    /** The prior (non-latest) committed versions. */
    ImmutableList<? extends EntityVersion> getHistoricVersions();

    /** Categorizes a version (committed/uncommitted/latest/prior) under the coordinate. */
    VersionCategory getVersionCategory(EntityVersion version);

    // ---- coordinate-free reads (the capability) ----

    /**
     * The STAMP of the latest <em>committed</em> version under this snapshot's coordinate: the latest
     * version if it is committed, otherwise the most recent committed prior version.
     *
     * @return the latest committed {@link StampEntity}, or {@code null} if none is committed
     */
    default StampEntity<?> latestCommittedStamp() {
        Latest<? extends EntityVersion> latest = getLatestVersion();
        if (latest.isPresent()) {
            EntityVersion latestVersion = latest.get();
            if (getVersionCategory(latestVersion) != VersionCategory.Uncommitted) {
                return latestVersion.stamp();
            }
        }
        for (EntityVersion version : getHistoricVersions()) {
            if (getVersionCategory(version) != VersionCategory.Uncommitted) {
                return version.stamp();
            }
        }
        return null;
    }

    /**
     * This entity's external identifiers (e.g. {@code "SCTID: 1335899003"}), resolved through the
     * identifier pattern under this snapshot's coordinate, excluding the UUID source.
     *
     * @return display strings like {@code "SCTID: 1335899003"}; empty if none
     */
    default List<String> externalIdentifiers() {
        List<String> identifiers = new ArrayList<>();
        ViewCalculator calculator = viewCalculator();
        Latest<PatternEntityVersion> latestIdPattern =
                calculator.latestPatternEntityVersion(TinkarTerm.IDENTIFIER_PATTERN);
        if (latestIdPattern.isAbsent()) {
            return identifiers;
        }
        for (ObservableSemantic semantic :
                observableEntity().getObservableSemanticListOfPattern(TinkarTerm.IDENTIFIER_PATTERN.nid())) {
            calculator.latest(semantic).ifPresent(latestSemanticVersion -> {
                EntityProxy identifierSource =
                        latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_SOURCE, latestSemanticVersion);
                if (!PublicId.equals(identifierSource, TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)) {
                    try {
                        String idSourceName = calculator.getPreferredDescriptionTextWithFallbackOrNid(identifierSource);
                        String idValue =
                                latestIdPattern.get().getFieldWithMeaning(TinkarTerm.IDENTIFIER_VALUE, latestSemanticVersion);
                        identifiers.add("%s: %s".formatted(idSourceName, idValue));
                    } catch (IndexOutOfBoundsException ignored) {
                        // A field meaning may be absent in some starter data; skip that identifier.
                    }
                }
            });
        }
        return identifiers;
    }
}
