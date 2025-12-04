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

import dev.ikm.tinkar.common.util.functional.TriConsumer;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.graph.DiTreeVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface StampCalculatorDelegate extends StampCalculator {
    @Override
    default Stream<Latest<SemanticEntityVersion>> streamLatestVersionForPattern(int patternNid) {
        return stampCalculator().streamLatestVersionForPattern(patternNid);
    }

    @Override
    default <V extends EntityVersion> Latest<V> latest(int nid) {
        return stampCalculator().latest(nid);
    }

    @Override
    default <V extends EntityVersion> List<DiTreeVersion<V>> getVersionGraphList(Entity<V> chronicle) {
        return stampCalculator().getVersionGraphList(chronicle);
    }

    @Override
    default <V extends EntityVersion> Latest<V> latest(Entity<V> chronicle) {
        return stampCalculator().latest(chronicle);
    }

    @Override
    default StampCoordinate stampCoordinate() {
        return stampCalculator().stampCoordinate();
    }

    @Override
    default StateSet allowedStates() {
        return stampCalculator().allowedStates();
    }

    @Override
    default RelativePosition relativePosition(int stampNid, int stampNid2) {
        return stampCalculator().relativePosition(stampNid, stampNid2);
    }

    @Override
    default void forEachSemanticVersionOfPattern(int patternNid, BiConsumer<SemanticEntityVersion, PatternEntityVersion> procedure) {
        stampCalculator().forEachSemanticVersionOfPattern(patternNid, procedure);
    }

    @Override
    default void forEachSemanticVersionOfPatternParallel(int patternNid, BiConsumer<SemanticEntityVersion, PatternEntityVersion> procedure) {
        stampCalculator().forEachSemanticVersionOfPatternParallel(patternNid, procedure);
    }

    @Override
    default void forEachSemanticVersionForComponent(int componentNid, BiConsumer<SemanticEntityVersion, EntityVersion> procedure) {
        stampCalculator().forEachSemanticVersionForComponent(componentNid, procedure);
    }

    @Override
    default void forEachSemanticVersionForComponentOfPattern(int componentNid, int patternNid, TriConsumer<SemanticEntityVersion, EntityVersion, PatternEntityVersion> procedure) {
        stampCalculator().forEachSemanticVersionForComponentOfPattern(componentNid, patternNid, procedure);
    }

    @Override
    default void forEachSemanticVersionWithFieldsForComponent(int componentNid, TriConsumer<SemanticEntityVersion, ImmutableList<? extends Field>, EntityVersion> procedure) {
        stampCalculator().forEachSemanticVersionWithFieldsForComponent(componentNid, procedure);
    }

    @Override
    default Latest<PatternEntityVersion> latestPatternEntityVersion(int patternNid) {
        return stampCalculator().latestPatternEntityVersion(patternNid);
    }

    @Override
    default OptionalInt getIndexForMeaning(int patternNid, int meaningNid) {
        return stampCalculator().getIndexForMeaning(patternNid, meaningNid);
    }

    @Override
    default OptionalInt getIndexForPurpose(int patternNid, int purposeNid) {
        return stampCalculator().getIndexForPurpose(patternNid, purposeNid);
    }

    @Override
    default <T> Latest<Field<T>> getFieldForSemanticWithMeaning(SemanticEntityVersion semanticVersion, EntityFacade meaning) {
        return stampCalculator().getFieldForSemanticWithMeaning(semanticVersion, meaning);
    }

    @Override
    default <T> Latest<Field<T>> getFieldForSemantic(Latest<SemanticEntityVersion> latestSemanticVersion, int criterionNid, FieldCriterion fieldCriterion) {
        return stampCalculator().getFieldForSemantic(latestSemanticVersion, criterionNid, fieldCriterion);
    }

    @Override
    default <T> Latest<Field<T>> getFieldForSemanticWithMeaning(SemanticEntityVersion semanticVersion, int meaningNid) {
        return stampCalculator().getFieldForSemanticWithMeaning(semanticVersion, meaningNid);
    }

    @Override
    default <T> Latest<Field<T>> getFieldForSemantic(int componentNid, int criterionNid, FieldCriterion fieldCriterion) {
        return stampCalculator().getFieldForSemantic(componentNid, criterionNid, fieldCriterion);
    }

    @Override
    default <T> Latest<Field<T>> getFieldForSemanticWithMeaning(int componentNid, int meaningNid) {
        return stampCalculator().getFieldForSemanticWithMeaning(componentNid, meaningNid);
    }

    StampCalculator stampCalculator();
}
