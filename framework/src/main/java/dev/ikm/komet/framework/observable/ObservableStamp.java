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

import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

public final class ObservableStamp
        extends ObservableEntity<ObservableStampVersion, StampVersionRecord> {
    ObservableStamp(StampEntity<StampVersionRecord> stampEntity) {
        super(stampEntity);
    }

    @Override
    protected ObservableStampVersion wrap(StampVersionRecord version) {
        return new ObservableStampVersion(version);
    }

    @Override
    public ObservableEntitySnapshot getSnapshot(ViewCalculator calculator) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param value
     * @param fieldIndex
     * @param version
     * @param <T>
     */
    @Override
    public <T> void createNewVersionAndTransaction(T value, int fieldIndex, ViewCalculator viewCalculator) {
        //TODO update the the version in versionProperty.
    }

    /**
     *
     */
    @Override
    public void addListeners() {

    }

    /**
     * Retrieves the most recent version of the observable stamp based on the timestamp.
     * If there is only one version, it returns that version.
     * In the case of multiple versions, it evaluates each version to determine the latest one.
     * Versions with a timestamp of {@code Long.MIN_VALUE} (cancelled) take precedence and are immediately returned.
     * Uncommitted versions marked with {@code Long.MAX_VALUE} are ignored in the evaluation of the latest version.
     *
     * @return The latest {@link ObservableStampVersion}, or the canceled version if one exists.
     */
    public ObservableStampVersion lastVersion() {
        if (versions().size() == 1) {
            return versions().get(0);
        }
        ObservableStampVersion latest = null;
        for (ObservableStampVersion version : versions()) {
            if (version.time() == Long.MIN_VALUE) {
                // if canceled (Long.MIN_VALUE), the latest is canceled.
                return version;
            } else if (latest == null) {
                latest = version;
            } else if (latest.time() == Long.MAX_VALUE) {
                latest = version;
            } else if (version.time() == Long.MAX_VALUE) {
                // ignore uncommitted version;
            } else if (latest.time() < version.time()) {
                latest = version;
            }
        }
        return latest;
    }


    @Override
    public ImmutableMap<FieldCategory, ObservableField> getObservableFields() {
        MutableMap<FieldCategory, ObservableField> fieldMap = Maps.mutable.empty();

        int firstStamp = StampCalculator.firstStampTimeOnly(this.entity().stampNids());

        for (FieldCategory field: FieldCategorySet.stampFields()) {
            switch (field) {
                case PUBLIC_ID_FIELD -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.publicId();
                    int dataTypeNid = TinkarTerm.IDENTIFIER_VALUE.nid();
                    int purposeNid = TinkarTerm.IDENTIFIER_VALUE.nid();
                    int meaningNid = TinkarTerm.IDENTIFIER_VALUE.nid();
                    Entity<EntityVersion> idPattern = Entity.getFast(TinkarTerm.IDENTIFIER_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(idPattern.stampNids());
                    int patternNid = TinkarTerm.IDENTIFIER_PATTERN.nid();
                    int indexInPattern = 0;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid,  indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }

                case COMPONENT_VERSIONS_LIST -> {
                    //TODO temporary until we get a pattern for concept fields...
                    //TODO get right starter set entities. Temporary incorrect codes for now.
                    Object value = this.versions();
                    int dataTypeNid = TinkarTerm.VERSION_LIST_FOR_CHRONICLE.nid();
                    int purposeNid = TinkarTerm.VERSION_LIST_FOR_CHRONICLE.nid();
                    int meaningNid = TinkarTerm.VERSION_LIST_FOR_CHRONICLE.nid();
                    Entity<EntityVersion> idPattern = Entity.getFast(TinkarTerm.STAMP_PATTERN.nid());
                    int patternVersionStampNid = StampCalculator.firstStampTimeOnly(idPattern.stampNids());
                    int patternNid = TinkarTerm.IDENTIFIER_PATTERN.nid();
                    int indexInPattern = 0;

                    FieldDefinitionRecord fdr = new FieldDefinitionRecord(dataTypeNid, purposeNid, meaningNid,
                            patternVersionStampNid, patternNid,  indexInPattern);

                    fieldMap.put(field, new ObservableField(new FieldRecord(value, this.nid(), firstStamp, fdr)));
                }

            }
        }
        return fieldMap.toImmutable();
    }
}
