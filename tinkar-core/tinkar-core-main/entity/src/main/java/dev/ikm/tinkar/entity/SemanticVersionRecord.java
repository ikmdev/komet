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
package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.Nid;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.Validator;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.component.SemanticVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.time.Instant;
import java.util.Objects;

@RecordBuilder
public record SemanticVersionRecord(SemanticRecord chronology, int stampNid,
                                    ImmutableList<Object> fieldValues)
        implements SemanticEntityVersion, ImmutableVersion, SemanticVersionRecordBuilder.With {

    public SemanticVersionRecord {
        Nid.validate(stampNid);
        Objects.requireNonNull(chronology);
        Objects.requireNonNull(fieldValues);
    }
    public SemanticVersionRecord(SemanticRecord chronology, SemanticVersion version) {
        this(chronology,
                Entity.nid(version.stamp()),
                Lists.immutable.fromStream(version.fieldValues().stream().map(o -> EntityRecordFactory.externalToInternalObject(o))));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticVersionRecord that = (SemanticVersionRecord) o;
        return stampNid == that.stampNid && fieldValues.equals(that.fieldValues);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(stampNid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("≤");
        sb.append(Entity.getStamp(stampNid).describe() + " nid: " + stampNid);
        PatternEntity<?> pattern = EntityHandle.getPatternOrThrow(this.chronology().patternNid());
        if (pattern instanceof PatternRecord patternEntity) {
            // TODO get proper version after relative position computer available.
            // Maybe put stamp coordinate on thread, or relative position computer on thread
            PatternVersionRecord patternEntityVersion = patternEntity.versions().get(0);
            sb.append("\n");
            for (int i = 0; i < fieldValues.size(); i++) {
                if (i > 0) {
                    sb.append("\n");
                }
                sb.append("Field ");
                sb.append((i + 1));
                sb.append(": ‹");
                StringBuilder fieldStringBuilder = new StringBuilder();

                Object field = fieldValues.get(i);
                if (i < patternEntityVersion.fieldDefinitions().size()) {
                    FieldDefinitionRecord fieldDefinition = patternEntityVersion.fieldDefinitions().get(i);
                    fieldStringBuilder.append(PrimitiveData.text(fieldDefinition.meaningNid()));
                } else {
                    fieldStringBuilder.append("Size error @ " + i);
                }
                fieldStringBuilder.append(": ");
                if (field instanceof EntityFacade entity) {
                    fieldStringBuilder.append(PrimitiveData.text(entity.nid()));
                } else if (field instanceof String string) {
                    fieldStringBuilder.append(string);
                } else if (field instanceof Instant instant) {
                    fieldStringBuilder.append(DateTimeUtil.format(instant));
                } else if (field instanceof IntIdList intIdList) {
                    if (intIdList.size() == 0) {
                        fieldStringBuilder.append("ø");
                    } else {
                        for (int j = 0; j < intIdList.size(); j++) {
                            if (j > 0) {
                                fieldStringBuilder.append(", ");
                            }
                            fieldStringBuilder.append(PrimitiveData.text(intIdList.get(j)));
                        }
                    }
                } else if (field instanceof IntIdSet intIdSet) {
                    if (intIdSet.size() == 0) {
                        fieldStringBuilder.append("ø");
                    } else {
                        int[] idSetArray = intIdSet.toArray();
                        for (int j = 0; j < idSetArray.length; j++) {
                            if (j > 0) {
                                fieldStringBuilder.append(", ");
                            }
                            fieldStringBuilder.append(PrimitiveData.text(idSetArray[j]));
                        }
                    }
                } else {
                    fieldStringBuilder.append(field);
                }
                String fieldString = fieldStringBuilder.toString();
                if (fieldString.contains("\n")) {
                    sb.append("\n");
                    sb.append(fieldString);
                } else {
                    sb.append(fieldString);
                }
                sb.append("› ");
                if (field != null) {
                    sb.append(field.getClass().getSimpleName());
                }

            }
        } else {
            sb.append("Bad pattern: ");
            sb.append(PrimitiveData.text(pattern.nid()));
            sb.append("; ");
            for (int i = 0; i < fieldValues.size(); i++) {
                Object field = fieldValues.get(i);
                if (i > 0) {
                    sb.append("; ");
                }
                if (field instanceof EntityFacade entity) {
                    sb.append("Entity: ");
                    sb.append(PrimitiveData.text(entity.nid()));
                } else if (field instanceof String string) {
                    sb.append("String: ");
                    sb.append(string);
                } else if (field instanceof Instant instant) {
                    sb.append("Instant: ");
                    sb.append(DateTimeUtil.format(instant));
                } else if (field instanceof Long aLong) {
                    sb.append("Long: ");
                    sb.append(DateTimeUtil.format(aLong));
                } else if (field instanceof IntIdList intIdList) {
                    sb.append(field.getClass().getSimpleName());
                    sb.append(": ");
                    if (intIdList.size() == 0) {
                        sb.append("ø, ");
                    } else {
                        for (int j = 0; j < intIdList.size(); j++) {
                            if (j > 0) {
                                sb.append(", ");
                            }
                            sb.append(PrimitiveData.text(intIdList.get(j)));
                        }
                    }
                } else if (field instanceof IntIdSet intIdSet) {
                    sb.append(field.getClass().getSimpleName());
                    sb.append(": ");
                    if (intIdSet.size() == 0) {
                        sb.append("ø, ");
                    } else {
                        int[] idSetArray = intIdSet.toArray();
                        for (int j = 0; j < idSetArray.length; j++) {
                            if (j > 0) {
                                sb.append(", ");
                            }
                            sb.append(PrimitiveData.text(idSetArray[j]));
                        }
                    }
                } else {
                    sb.append(field.getClass().getSimpleName());
                    sb.append(": ");
                    sb.append(field);
                }
            }
        }

        sb.append("≥");

        return sb.toString();
    }

    @Override
    public ImmutableList<FieldRecord> fields() {
        MutableList<FieldRecord> fieldRecords = Lists.mutable.empty();
        for (int i = 0; i < fieldValues.size(); i++) {
            fieldRecords.add(new FieldRecord(fieldValues.get(i), nid(), stampNid,
            patternNid(), i));
        }
        return fieldRecords.toImmutable();
    }
}
