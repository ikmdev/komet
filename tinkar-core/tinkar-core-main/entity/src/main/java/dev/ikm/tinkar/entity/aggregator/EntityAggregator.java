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

import dev.ikm.tinkar.entity.EntityCountSummary;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;

public abstract class EntityAggregator {
    protected AtomicLong conceptsAggregatedCount = new AtomicLong(0);
    protected AtomicLong semanticsAggregatedCount = new AtomicLong(0);
    protected AtomicLong patternsAggregatedCount = new AtomicLong(0);
    protected AtomicLong stampsAggregatedCount = new AtomicLong(0);

    public abstract EntityCountSummary aggregate(IntConsumer nidConsumer);

    public long totalCount() {
        EntityCountSummary countSummary = this.aggregate((nid) -> {});
        return countSummary.getTotalCount();
    }

    public EntityCountSummary summarize() {
        return new EntityCountSummary(
            conceptsAggregatedCount.get(),
            semanticsAggregatedCount.get(),
            patternsAggregatedCount.get(),
            stampsAggregatedCount.get()
        );
    }

    protected void initCounts() {
        conceptsAggregatedCount.set(0);
        semanticsAggregatedCount.set(0);
        patternsAggregatedCount.set(0);
        stampsAggregatedCount.set(0);
    }
}
