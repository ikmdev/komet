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

public final class ObservableConceptSnapshot
        extends ObservableEntitySnapshot<ObservableConcept, ObservableConceptVersion, ConceptVersionRecord> {

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
