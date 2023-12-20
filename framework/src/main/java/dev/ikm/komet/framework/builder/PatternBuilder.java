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
package dev.ikm.komet.framework.builder;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.TinkarTerm;
import java.util.UUID;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * builder class to create and persist a Pattern entity
 */
public class PatternBuilder {

    private final StampEntity stampEntity;

    MutableList<DescriptionBuilderRecord> descriptionsToBuild = Lists.mutable.empty();

    Transaction transaction;

    /**
     * construct the builder class using a StampEntity
     * @param stampEntity
     */
    public PatternBuilder(StampEntity stampEntity) {
        this.stampEntity = stampEntity;
        Transaction.forStamp(stampEntity).ifPresentOrElse(transaction1 -> transaction = transaction1, () -> {
            throw new IllegalStateException("No transaction for stamp: " + stampEntity);
        });
    }

    /**
     * provide a PatternBuilder via the STAMP entity
     * @param stampEntity
     * @return
     */
    public static PatternBuilder builder(StampEntity stampEntity) {
        return new PatternBuilder(stampEntity);
    }

    /**
     * use the pattern description, i.e. its name to create a Pattern Builder
     * @param newPatternText
     * @return
     */
    public PatternBuilder makeRegularName(String newPatternText) {
        return with(DescriptionBuilderRecord.makeRegularName(newPatternText));
    }

    /**
     * return the current instance of the Pattern Builder while leveraging the
     * DescriptionBuilderRecord
     * @param descriptionRecord
     * @return
     */
    public PatternBuilder with(DescriptionBuilderRecord descriptionRecord) {
        descriptionsToBuild.add(descriptionRecord);
        return this;
    }

    /**
     * utility class to create and persist the new Pattern(s)
     * @return entities that represent the Pattern(s) created
     */
    public ImmutableList<EntityFacade> build() {
        MutableList<EntityFacade> entities = Lists.mutable.empty();
        UUID patternUuid = UUID.randomUUID();
        RecordListBuilder<PatternVersionRecord> versionRecords = RecordListBuilder.make();
        PatternRecord patternRecord = new PatternRecord(patternUuid.getMostSignificantBits(), patternUuid.getLeastSignificantBits(),
                null, PrimitiveData.nid(patternUuid), versionRecords);

        // iterate through the descriptions to attach to the pattern
        for (DescriptionBuilderRecord descriptionToBuild : descriptionsToBuild) {
            MutableList<FieldDefinitionRecord> fieldDefinitions = Lists.mutable.empty();
            ImmutableList<FieldDefinitionRecord> immutableList = fieldDefinitions.toImmutable();

            // the semantic record of the pattern entity needs versions
            versionRecords.addAndBuild(new PatternVersionRecord(patternRecord,
                    stampEntity.nid(),
                    // basic Patterns can default to an anonymous concept
                    EntityService.get().nidForPublicId(TinkarTerm.ANONYMOUS_CONCEPT),
                    EntityService.get().nidForPublicId(TinkarTerm.ANONYMOUS_CONCEPT),
                    immutableList
            ));

            // persist the initial pattern record
            // not the final save operation
            processEntity(entities, patternRecord);

            RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();
            UUID descriptionSemanticUUID = UUID.randomUUID();
            SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                    .nid(EntityService.get().nidForUuids(descriptionSemanticUUID))
                    .leastSignificantBits(descriptionSemanticUUID.getLeastSignificantBits())
                    .mostSignificantBits(descriptionSemanticUUID.getMostSignificantBits())
                    .additionalUuidLongs(null)
                    .patternNid(TinkarTerm.DESCRIPTION_PATTERN.nid())
                    .referencedComponentNid(patternRecord.nid())
                    // add aforementioned version records to the SemanticRecord
                    .versions(versions.toImmutable())
                    .build();

            // default to English, non-case sensitive, fully qualified name
            ImmutableList<Object> descriptionFields = Lists.immutable.of(TinkarTerm.ENGLISH_LANGUAGE,
                    descriptionToBuild.text(),
                    TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                    TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);

            versions.add(SemanticVersionRecordBuilder.builder()
                    .chronology(semanticRecord)
                    .stampNid(stampEntity.nid())
                    .fieldValues(descriptionFields.toImmutable())
                    .build());
            // use the builder to create the description to persist
            Entity<? extends EntityVersion> description = SemanticRecordBuilder.builder(semanticRecord).versions(versions.toImmutable()).build();

            // persist the Pattern's description
            processEntity(entities, description);
        }

        return entities.toImmutable();
    }

    private void processEntity(MutableList<EntityFacade> entities, Entity<? extends EntityVersion> entity) {
        this.transaction.addComponent(entity);
        Entity.provider().putEntity(entity);
        entities.add(entity);
    }
}
