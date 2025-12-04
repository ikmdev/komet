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
package dev.ikm.tinkar.entity.aggregator;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.component.FieldDataType;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.IntConsumer;

public class MembershipEntityAggregator extends EntityAggregator {
    private final List<PublicId> membershipTags;

    public MembershipEntityAggregator(List<PublicId> membershipTags) {
        this.membershipTags = membershipTags;
    }

    @Override
    public EntityCountSummary aggregate(IntConsumer nidConsumer) {
        initCounts();
        Set<Integer> stampNidSet = new HashSet<>();

        membershipTags.forEach((membershipTagId) -> {
            int patternNid = PrimitiveData.nid(membershipTagId);

            // Aggregate Patterns and Stamps
            nidConsumer.accept(patternNid);
            patternsAggregatedCount.incrementAndGet();
            Entity<? extends EntityVersion> patternEntity = EntityService.get().getEntityFast(patternNid);
            patternEntity.stampNids().forEach(stampNidSet::add);

            EntityService.get().forEachSemanticOfPattern(patternNid, (semanticEntityOfPattern) -> {
                int referencedComponentNid = semanticEntityOfPattern.referencedComponentNid();

                if (referencedComponentNid != patternNid) {
                    // Aggregate Concept and Stamps
                    nidConsumer.accept(referencedComponentNid);
                    Entity<? extends EntityVersion> referencedComponentEntity = EntityService.get().getEntityFast(referencedComponentNid);
                    switch (referencedComponentEntity.versionDataType()) {
                        case FieldDataType.CONCEPT_VERSION -> conceptsAggregatedCount.incrementAndGet();
                        case FieldDataType.PATTERN_VERSION -> patternsAggregatedCount.incrementAndGet();
                        case FieldDataType.SEMANTIC_VERSION -> semanticsAggregatedCount.incrementAndGet();
                        case FieldDataType.STAMP_VERSION -> stampsAggregatedCount.incrementAndGet();
                        default -> throw new IllegalStateException("Referenced Component not a valid type");
                    }
                    referencedComponentEntity.stampNids().forEach(stampNidSet::add);
                }

                // Aggregate Semantics and Stamps
                Queue<Integer> queue = new LinkedList<>();
                queue.add(referencedComponentNid);
                while (!queue.isEmpty()) {
                    EntityService.get().forEachSemanticForComponent(queue.remove(), (semanticEntity) -> {
                        queue.add(semanticEntity.nid());
                        semanticsAggregatedCount.incrementAndGet();
                        nidConsumer.accept(semanticEntity.nid());
                        semanticEntity.stampNids().forEach(stampNidSet::add);
                    });
                }
            });
        });
        stampsAggregatedCount.set(stampNidSet.size());
        stampNidSet.forEach(nidConsumer::accept);

        return summarize();
    }
}
