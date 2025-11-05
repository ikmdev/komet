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

import dev.ikm.komet.framework.observable.binding.Binding;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class ObservableSemantic
        extends ObservableEntity<ObservableSemanticVersion>
        implements SemanticEntity<ObservableSemanticVersion> {
    ObservableSemantic(SemanticEntity<SemanticVersionRecord> semanticEntity) {
        super(semanticEntity);
    }

    @Override
    protected ObservableSemanticVersion wrap(EntityVersion version) {
        return new ObservableSemanticVersion(this, (SemanticVersionRecord) version);
    }

    @Override
    public ObservableSemanticSnapshot getSnapshot(ViewCalculator calculator) {
        return new ObservableSemanticSnapshot(calculator, this);
    }

    @Override
    public int referencedComponentNid() {
        return ((SemanticEntity) entity()).referencedComponentNid();
    }

    @Override
    public int patternNid() {
        return ((SemanticEntity) entity()).patternNid();
    }

    public static ObservableSemanticSnapshot getSemanticSnapshot(int semanticNid, ViewCalculator calculator) {
        ObservableSemantic observableSemantic = get(semanticNid);
        return observableSemantic.getSnapshot(calculator);
    }

    public static Optional<ObservableSemanticSnapshot> getStatedAxiomSnapshot(int conceptNid, ViewCalculator calculator) {
        return getAxiomSnapshot(conceptNid, calculator.viewCoordinateRecord().logicCoordinate().statedAxiomsPatternNid(),
                calculator);
    }

    public static Optional<ObservableSemanticSnapshot> getInferredAxiomSnapshot(int conceptNid, ViewCalculator calculator) {
        return getAxiomSnapshot(conceptNid, calculator.viewCoordinateRecord().logicCoordinate().inferredAxiomsPatternNid(),
                calculator);
    }
    public static Optional<ObservableSemanticSnapshot> getAxiomSnapshot(int conceptNid, PremiseType premiseType, ViewCalculator calculator) {
        return switch (premiseType) {
            case STATED -> getAxiomSnapshot(conceptNid, calculator.viewCoordinateRecord().logicCoordinate().statedAxiomsPatternNid(),
                    calculator);
            case INFERRED -> getAxiomSnapshot(conceptNid, calculator.viewCoordinateRecord().logicCoordinate().inferredAxiomsPatternNid(),
                    calculator);
        };
    }

    public static Optional<ObservableSemanticSnapshot> getAxiomSnapshot(int conceptNid, int axiomPatterNid, ViewCalculator calculator) {


        int[] axiomSemanticNids = EntityService.get().semanticNidsForComponentOfPattern(conceptNid, axiomPatterNid);
        if (axiomSemanticNids.length == 0) {
            return Optional.empty();
        } else if (axiomSemanticNids.length > 1) {
            throw new IllegalStateException("To many axiom semantics in " +
                    calculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(axiomPatterNid) +
                    " for " + calculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(conceptNid));
        }
        ObservableSemanticSnapshot axiomSemanticSnapshot = getSemanticSnapshot(axiomSemanticNids[0], calculator);
        return Optional.of(axiomSemanticSnapshot);
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    final AtomicReference<FeatureWrapper> patternForSemanticFieldReference = new AtomicReference<>();
    private FeatureWrapper getPatternForSemanticFeature() {
        return patternForSemanticFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makePatternForSemanticFeature());
    }
    private FeatureWrapper makePatternForSemanticFeature() {
        FeatureKey locator = FeatureKey.Entity.SemanticPattern(this.nid());
        return new FeatureWrapper(this.pattern(), Binding.Semantic.pattern().nid(), Binding.Semantic.patternFieldDefinitionIndex(), this, locator);
    }

    // TODO: replace with JEP 502: Stable Values when finalized to allow lazy initialization of feature.
    final AtomicReference<FeatureWrapper> referencedComponentFieldReference = new AtomicReference<>();
    private FeatureWrapper getReferencedComponentFeature() {
        return referencedComponentFieldReference.updateAndGet(currentValue -> currentValue != null
                ? currentValue
                : makeReferencedComponentFeature());
    }

    private FeatureWrapper makeReferencedComponentFeature() {
        FeatureKey locator = FeatureKey.Entity.SemanticReferencedComponent(this.nid());
        return new FeatureWrapper(this.referencedComponent(), Binding.Semantic.pattern().nid(), Binding.Component.versionsFieldDefinitionIndex(), this, locator);
    }

    @Override
    protected void addAdditionalChronologyFeatures(MutableList<Feature> features) {
        // Pattern for semantic
        features.add(getPatternForSemanticFeature());

        // Referenced component for semantic
        features.add(getReferencedComponentFeature());
    }

}
