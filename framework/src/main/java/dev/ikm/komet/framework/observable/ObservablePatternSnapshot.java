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

import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.ConceptFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * View-specific projection of an {@link ObservablePattern} providing access to pattern versions
 * categorized by their temporal and coordination state.
 * <p>
 * Patterns define the structure of semantic records. This snapshot provides view-filtered access
 * to pattern versions, enabling analysis of how pattern definitions evolve over time.
 *
 * <h2>Pattern Snapshot Characteristics</h2>
 * <ul>
 *   <li><b>Structural Definition:</b> Patterns define field structures for semantic records</li>
 *   <li><b>Purpose &amp; Meaning:</b> Each pattern version has semantic purpose and meaning concepts</li>
 *   <li><b>Field Definitions:</b> Field definition lists can evolve across versions</li>
 *   <li><b>Breaking Changes:</b> Field definition changes may be incompatible across versions</li>
 * </ul>
 *
 * <h2>⚠️ How to Create: Use ObservableEntityHandle</h2>
 * <pre>{@code
 * ViewCalculator viewCalc = // ... from ViewCoordinateRecord
 * ObservablePatternSnapshot snapshot =
 *     ObservableEntityHandle.getPatternSnapshotOrThrow(patternNid, viewCalc);
 * }</pre>
 *
 * <h2>Common Usage Patterns</h2>
 *
 * <p><b>Pattern 1: Get Current Field Definitions</b>
 * <pre>{@code
 * ObservablePatternSnapshot snapshot =
 *     ObservableEntityHandle.getPatternSnapshotOrThrow(patternNid, viewCalc);
 *
 * Latest<ObservablePatternVersion> latest = snapshot.getLatestVersion();
 * if (latest.isPresent()) {
 *     ImmutableList<ObservableFeatureDefinition> fields = 
 *         latest.get().fieldDefinitions();
 *     displayFieldStructure(fields);
 * }
 * }</pre>
 *
 * <p><b>Pattern 2: Check for Pattern Evolution</b>
 * <pre>{@code
 * ObservablePatternSnapshot snapshot =
 *     ObservableEntityHandle.getPatternSnapshotOrThrow(patternNid, viewCalc);
 *
 * if (!snapshot.getHistoricVersions().isEmpty()) {
 *     // Pattern structure has evolved
 *     LOG.warn("Pattern {} has {} versions - check for breaking changes",
 *         patternNid, snapshot.getHistoricVersions().size() + 1);
 *     
 *     analyzePatternEvolution(
 *         snapshot.getLatestVersion().get(),
 *         snapshot.getHistoricVersions()
 *     );
 * }
 * }</pre>
 *
 * @see ObservableEntityHandle
 * @see ObservablePattern
 * @see ObservablePatternVersion
 * @see ObservableEntitySnapshot
 * @see ViewCalculator
 */
public final class ObservablePatternSnapshot extends ObservableEntitySnapshot<ObservablePattern, ObservablePatternVersion> {

    public ObservablePatternSnapshot(ViewCalculator viewCalculator, ObservablePattern entity) {
        super(viewCalculator, entity);
    }

    @Override
    public ImmutableList<ObservablePatternVersion> getProcessedVersions() {
        return super.getProcessedVersions();
    }

    @Override
    public void filterProcessedVersions(Predicate<ObservablePatternVersion> filter) {
        super.filterProcessedVersions(filter);
    }

    @Override
    public void sortProcessedVersions(Comparator<ObservablePatternVersion> comparator) {
        super.sortProcessedVersions(comparator);
    }

    @Override
    public ObservablePattern observableEntity() {
        return super.observableEntity();
    }

    @Override
    public ImmutableList<ObservablePatternVersion> getUncommittedVersions() {
        return super.getUncommittedVersions();
    }

    @Override
    public ImmutableList<ObservablePatternVersion> getHistoricVersions() {
        return super.getHistoricVersions();
    }

    @Override
    public Latest<ObservablePatternVersion> getLatestVersion() {
        return super.getLatestVersion();
    }

    /**
     * Returns the field definitions from the latest pattern version.
     * <p>
     * Convenience method equivalent to {@code getLatestVersion().get().fieldDefinitions()}.
     *
     * @return field definitions, or empty list if no latest version
     */
    public ImmutableList<ObservableFeatureDefinition> getLatestFieldDefinitions() {
        return latestVersion.ifAbsentOrFunction(
            () -> Lists.immutable.empty(),
            ObservablePatternVersion::fieldDefinitions
        );
    }

    /**
     * Returns the semantic purpose concept from the latest pattern version.
     */
    public Optional<ConceptFacade> getLatestPurpose() {
        return latestVersion.ifAbsentOrFunction(
            Optional::empty,
            v -> Optional.of(EntityHandle.getConceptOrThrow(v.semanticPurposeNid()))
        );
    }

    /**
     * Returns the semantic meaning concept from the latest pattern version.
     */
    public Optional<ConceptFacade> getLatestMeaning() {
        return latestVersion.ifAbsentOrFunction(
            Optional::empty,
            v -> Optional.of(EntityHandle.getConceptOrThrow(v.semanticMeaningNid()))
        );
    }
}