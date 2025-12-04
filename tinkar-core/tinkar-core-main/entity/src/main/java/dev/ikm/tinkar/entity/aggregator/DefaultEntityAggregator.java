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

import java.util.function.IntConsumer;

public class DefaultEntityAggregator extends EntityAggregator {

    @Override
    public EntityCountSummary aggregate(IntConsumer nidConsumer) {
        initCounts();
        // Aggregate all Stamps
        PrimitiveData.get().forEachStampNid(stampNid -> {
            nidConsumer.accept(stampNid);
            stampsAggregatedCount.incrementAndGet();
        });

        // Aggregate all Concepts
        PrimitiveData.get().forEachConceptNid(conceptNid -> {
            nidConsumer.accept(conceptNid);
            conceptsAggregatedCount.incrementAndGet();
        });

        // Aggregate all Semantics
        PrimitiveData.get().forEachSemanticNid(semanticNid -> {
            nidConsumer.accept(semanticNid);
            semanticsAggregatedCount.incrementAndGet();
        });

        // Aggregate all Patterns
        PrimitiveData.get().forEachPatternNid(patternNid -> {
            nidConsumer.accept(patternNid);
            patternsAggregatedCount.incrementAndGet();
        });

        return summarize();
    }
}
