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
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

public final class ObservableSemanticSnapshot extends ObservableEntitySnapshot<ObservableSemantic, ObservableSemanticVersion, SemanticVersionRecord> {
    private final Latest<PatternEntityVersion> latestPattern;

    public ObservableSemanticSnapshot(ViewCalculator viewCalculator, ObservableSemantic entity) {
        super(viewCalculator, entity);
        this.latestPattern = viewCalculator.latestPatternEntityVersion(entity.patternNid());
    }

    public int referencedComponentNid() {
        return observableEntity.referencedComponentNid();
    }

    public int patternNid() {
        return observableEntity.patternNid();
    }

    @Override
    public ImmutableList<ObservableSemanticVersion> getProcessedVersions() {
        return super.getProcessedVersions();
    }

    @Override
    public void filterProcessedVersions(Predicate<ObservableSemanticVersion> filter) {
        super.filterProcessedVersions(filter);
    }

    @Override
    public void sortProcessedVersions(Comparator<ObservableSemanticVersion> comparator) {
        super.sortProcessedVersions(comparator);
    }

    @Override
    public ObservableSemantic observableEntity() {
        return super.observableEntity();
    }

    @Override
    public ImmutableList<ObservableSemanticVersion> getUncommittedVersions() {
        return super.getUncommittedVersions();
    }

    @Override
    public ImmutableList<ObservableSemanticVersion> getHistoricVersions() {
        return super.getHistoricVersions();
    }

    @Override
    public Latest<ObservableSemanticVersion> getLatestVersion() {
        return super.getLatestVersion();
    }

    public <T> Latest<T> findFirstValue(Predicate<Field> test) {
        Latest<Field<T>> latestField = findFirstField(test);
        return latestField.ifAbsentOrFunction(Latest::empty,
                field -> {
                    Latest<T> latestValue = Latest.of(field.value());
                    for (Field<T> contradictionField : latestField.contradictions()) {
                        latestValue.addLatest(contradictionField.value());
                    }
                    return latestValue;
                });
    }

    public <T> Latest<Field<T>> findFirstField(Predicate<Field> test) {
        return getLatestFields().ifAbsentOrFunction(Latest::empty,
                fields -> {
                    for (int index = 0; index < fields.size(); index++) {
                        Field field = fields.get(index);
                        // TODO handle case where only contradictions match...
                        if (test.test(field)) {
                            Latest<Field<T>> latestField = Latest.of(field);
                            if (latestVersion.isContradicted()) {
                                for (ObservableSemanticVersion contradiction : latestVersion.contradictions()) {
                                    latestField.addLatest(contradiction.fields(latestPattern.get()).get(index));
                                }
                            }
                            return latestField;
                        }
                    }
                    return Latest.empty();
                });
    }

    public Latest<ImmutableList<ObservableField>> getLatestFields() {
        return latestVersion.ifAbsentOrFunction(
                Latest::empty,
                version -> {
                    Latest<PatternEntityVersion> latestPattern = viewCalculator.latestPatternEntityVersion(version.patternNid());
                    return latestPattern.ifAbsentOrFunction(Latest::empty,
                            patternEntityVersion -> {
                                Latest<ImmutableList<ObservableField>> latest = Latest.of(version.fields(patternEntityVersion));
                                if (latestVersion.isContradicted()) {
                                    for (ObservableSemanticVersion contradiction : latestVersion.contradictions()) {
                                        latest.addLatest(contradiction.fields(patternEntityVersion));
                                    }
                                }
                               /* latest.get().forEach(observableField -> {
                                        observableField.refreshProperties.set(true);
                                        observableField.valueProperty().addListener((observable) ->{
                                            Object nv = observableField.valueProperty().get();
                                            if(nv != null){
                                                ObservableSemanticSnapshot observableSemanticSnapshot = new ObservableSemanticSnapshot(viewCalculator, observableEntity);
                                                //(ObservableSemanticSnapshot) ObservableEntity.get(this.observableEntity()).getSnapshot(this.viewCalculator);
                                                // Alternate approach to call updateVersion directly.
                                                // ObservableEntity.updateVersions(Entity.getFast(observableEntity.nid()), this.observableEntity);
                                                this.observableEntity.manageEntityVersion(observableField.value(), observableField.fieldIndex(),observableSemanticSnapshot);
                                                observableField.fieldProperty.set(observableField.field().withValue(nv));
                                            }
                                        });
                                    });*/

                                return latest;
                            });
                });
    }

    public String findFirstFieldStringValueOrEmpty(Predicate<Field> test) {
        Latest<Field<String>> stringFieldLatest = findFirstField(test);
        if (stringFieldLatest.isPresent()) {
            return stringFieldLatest.get().value();
        }
        return "";
    }

    public String findFirstFieldStringValueOrDefault(Predicate<Field> test, String defaultString) {
        Latest<Field<String>> stringFieldLatest = findFirstField(test);
        if (stringFieldLatest.isPresent()) {
            return stringFieldLatest.get().value();
        }
        return defaultString;
    }

    public Optional<String> findFirstFieldStringValue(Predicate<Field> test) {
        Latest<Field<String>> stringFieldLatest = findFirstField(test);
        if (stringFieldLatest.isPresent()) {
            return Optional.of(stringFieldLatest.get().value());
        }
        return Optional.empty();
    }

    public int findFirstFieldNidValueOrMaxValue(Predicate<Field> test) {
        return findFirstFieldNidValueOrDefault(test, Integer.MAX_VALUE);
    }

    public int findFirstFieldNidValueOrDefault(Predicate<Field> test, int defaultNid) {
        OptionalInt optionalNid = findFirstFieldNidValue(test);
        if (optionalNid.isPresent()) {
            return optionalNid.getAsInt();
        }
        return defaultNid;
    }

    public OptionalInt findFirstFieldNidValue(Predicate<Field> test) {
        return findFirstField(test).ifAbsentOrFunction(OptionalInt::empty,
                objectField -> {
                    if (objectField.value() instanceof EntityFacade entityFacade) {
                        return OptionalInt.of(entityFacade.nid());
                    }
                    return OptionalInt.empty();
                });
    }

    public ImmutableList<Latest<Field>> findAllFields(Predicate<Field<Object>> test) {
        MutableList<Latest<Field>> matchedFields = Lists.mutable.empty();
        getLatestFields().ifAbsentOrFunction(Latest::empty,
                fields -> {
                    for (int index = 0; index < fields.size(); index++) {
                        Field field = fields.get(index);
                        // TODO handle case where only contradictions match...
                        if (test.test(field)) {
                            Latest<Field> latestField = Latest.of(field);
                            if (latestVersion.isContradicted()) {
                                for (ObservableSemanticVersion contradiction : latestVersion.contradictions()) {
                                    latestField.addLatest(contradiction.fields(latestPattern.get()).get(index));
                                }
                            }
                            matchedFields.add(latestField);
                        }
                    }
                    return Latest.empty();
                });
        return matchedFields.toImmutable();
    }

}