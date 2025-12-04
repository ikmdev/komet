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

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

public class TemporalEntityAggregator extends EntityAggregator {
    private final long fromEpochMillis;
    private final long toEpochMillis;

    public TemporalEntityAggregator(long fromEpochMillis, long toEpochMillis) {
        this.fromEpochMillis = fromEpochMillis;
        this.toEpochMillis = toEpochMillis;
    }

    @Override
    public EntityCountSummary aggregate(IntConsumer nidConsumer) {
        initCounts();
        // Filter Stamp Nids based on the supplied time span
        Set<Integer> filteredStampNids = new HashSet<>();
        PrimitiveData.get().forEachStampNid((stampNid) -> {
            EntityService.get().getStamp(stampNid).ifPresent((stampEntity) -> {
                if (fromEpochMillis <= stampEntity.time() && stampEntity.time() <= toEpochMillis) {
                    filteredStampNids.add(stampEntity.nid());
                }
            });
        });

        List<Integer> stampsToExport = new ArrayList<>();

        // Aggregate concepts with a filtered stamp
        PrimitiveData.get().forEachConceptNid((conceptNid) -> {
            EntityService.get().getEntity(conceptNid).ifPresent((conceptEntity) -> {
                Set<Integer> conceptStampNidList = conceptEntity.stampNids().mapToSet(i->i);
                // Write whole chronology if ANY of the stamps satisfy conditions
                if (!Collections.disjoint(filteredStampNids, conceptStampNidList)) {
                    conceptsAggregatedCount.incrementAndGet();
                    nidConsumer.accept(conceptNid);
                    stampsToExport.addAll(conceptStampNidList);
                }
            });
        });

        // Aggregate semantics with a filtered stamp
        PrimitiveData.get().forEachSemanticNid((semanticNid) -> {
            EntityService.get().getEntity(semanticNid).ifPresent((semanticEntity) -> {
                Set<Integer> semanticStampNidList = semanticEntity.stampNids().mapToSet(i->i);
                // Write whole chronology if ANY of the stamps satisfy conditions
                if (!Collections.disjoint(filteredStampNids, semanticStampNidList)) {
                    semanticsAggregatedCount.incrementAndGet();
                    nidConsumer.accept(semanticNid);
                    stampsToExport.addAll(semanticStampNidList);
                }
            });
        });

        // Aggregate patterns with a filtered stamp
        PrimitiveData.get().forEachPatternNid((patternNid) -> {
            EntityService.get().getEntity(patternNid).ifPresent((patternEntity) -> {
                Set<Integer> patternStampNidList = patternEntity.stampNids().mapToSet(i->i);
                // Write whole chronology if ANY of the stamps satisfy conditions
                if (!Collections.disjoint(filteredStampNids, patternStampNidList)) {
                    patternsAggregatedCount.incrementAndGet();
                    nidConsumer.accept(patternNid);
                    stampsToExport.addAll(patternStampNidList);
                }
            });
        });

        // Deduplicate and Export Aggregated stamps
        Set<Integer> deduplicatedStampsToExport = new HashSet<>(stampsToExport);
        stampsAggregatedCount.set(deduplicatedStampsToExport.size());
        deduplicatedStampsToExport.forEach(nidConsumer::accept);

        return summarize();
    }
}
