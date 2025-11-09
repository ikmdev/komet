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

import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptVersionRecord;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * View-specific projection of an {@link ObservableConcept} providing access to concept versions
 * categorized by their temporal and coordination state.
 * <p>
 * Concepts are the foundational building blocks representing clinical or business ideas.
 * This snapshot filters concept versions according to view coordinates, providing access to
 * current, historic, and uncommitted versions.
 *
 * <h2>Concept Snapshot Characteristics</h2>
 * <ul>
 *   <li><b>Simple Structure:</b> Concepts typically have minimal version data (just stamp fields)</li>
 *   <li><b>Foundation Role:</b> Concepts are referenced by patterns, semantics, and other entities</li>
 *   <li><b>Evolution:</b> Concept versions track lifecycle changes (active/inactive status changes)</li>
 *   <li><b>Common Operations:</b> Checking current status, viewing status history</li>
 * </ul>
 *
 * <h2>⚠️ How to Create: Use ObservableEntityHandle</h2>
 * <p>
 * <b>DO NOT</b> construct {@code ObservableConceptSnapshot} instances directly. Instead,
 * use {@link ObservableEntityHandle} for type-safe snapshot creation:
 *
 * <pre>{@code
 * // ✅ CORRECT: Use ObservableEntityHandle
 * ViewCalculator viewCalc = // ... from ViewCoordinateRecord
 * ObservableConceptSnapshot snapshot =
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(conceptNid, viewCalc);
 *
 * // ✅ CORRECT: Fluent API with optional handling
 * ObservableEntityHandle.get(nid)
 *     .ifConceptGetSnapshot(viewCalc, snapshot -> {
 *         Latest<ObservableConceptVersion> latest = snapshot.getLatestVersion();
 *         displayConceptStatus(latest);
 *     });
 * }</pre>
 *
 * <h2>Common Usage Patterns</h2>
 *
 * <p><b>Pattern 1: Check Current Status</b>
 * <pre>{@code
 * ObservableConceptSnapshot snapshot =
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(conceptNid, viewCalc);
 *
 * Latest<ObservableConceptVersion> latest = snapshot.getLatestVersion();
 * if (latest.isPresent()) {
 *     State currentState = latest.get().state();
 *     boolean isActive = currentState == State.ACTIVE;
 *     statusLabel.setText(isActive ? "Active" : "Inactive");
 * }
 * }</pre>
 *
 * <p><b>Pattern 2: Show Status History</b>
 * <pre>{@code
 * ObservableConceptSnapshot snapshot =
 *     ObservableEntityHandle.getConceptSnapshotOrThrow(conceptNid, viewCalc);
 *
 * // Show current status
 * displayVersion(snapshot.getLatestVersion().get(), "Current");
 *
 * // Show historic status changes
 * for (ObservableConceptVersion historic : snapshot.getHistoricVersions()) {
 *     displayVersion(historic, "Historic");
 * }
 * }</pre>
 *
 * @see ObservableEntityHandle
 * @see ObservableConcept
 * @see ObservableConceptVersion
 * @see ObservableEntitySnapshot
 * @see ViewCalculator
 */
public final class ObservableConceptSnapshot
        extends ObservableEntitySnapshot<ObservableConcept, ObservableConceptVersion> {

    public ObservableConceptSnapshot(ViewCalculator viewCalculator, ObservableConcept entity) {
        super(viewCalculator, entity);
    }

    @Override
    public ImmutableList<ObservableConceptVersion> getProcessedVersions() {
        return super.getProcessedVersions();
    }

    @Override
    public void filterProcessedVersions(Predicate<ObservableConceptVersion> filter) {
        super.filterProcessedVersions(filter);
    }

    @Override
    public void sortProcessedVersions(Comparator<ObservableConceptVersion> comparator) {
        super.sortProcessedVersions(comparator);
    }

    @Override
    public ObservableConcept observableEntity() {
        return super.observableEntity();
    }

    @Override
    public ImmutableList<ObservableConceptVersion> getUncommittedVersions() {
        return super.getUncommittedVersions();
    }

    @Override
    public ImmutableList<ObservableConceptVersion> getHistoricVersions() {
        return super.getHistoricVersions();
    }

    @Override
    public Latest<ObservableConceptVersion> getLatestVersion() {
        return super.getLatestVersion();
    }
}
