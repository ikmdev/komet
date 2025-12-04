package dev.ikm.tinkar.entity.aggregator;

import dev.ikm.tinkar.entity.EntityCountSummary;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;

abstract public class EntityAggregatorFilter extends EntityAggregator {
    protected AtomicLong conceptsFilteredCount = new AtomicLong(0);
    protected AtomicLong semanticsFilteredCount = new AtomicLong(0);
    protected AtomicLong patternsFilteredCount = new AtomicLong(0);
    protected AtomicLong stampsFilteredCount = new AtomicLong(0);

    protected final EntityAggregator entityAggregator;

    protected EntityAggregatorFilter(EntityAggregator entityAggregator) {
        this.entityAggregator = entityAggregator;
    }

    public abstract EntityCountSummary aggregate(IntConsumer nidConsumer);

    public void adjustCounts(EntityCountSummary unfilteredSummary) {
        this.conceptsAggregatedCount.set(unfilteredSummary.conceptsCount() - conceptsFilteredCount.get());
        this.semanticsAggregatedCount.set(unfilteredSummary.semanticsCount() - semanticsFilteredCount.get());
        this.patternsAggregatedCount.set(unfilteredSummary.patternsCount() - patternsFilteredCount.get());
        this.stampsAggregatedCount.set(unfilteredSummary.stampsCount() - stampsFilteredCount.get());
    }

    @Override
    protected void initCounts() {
        super.initCounts();
        conceptsFilteredCount.set(0);
        semanticsFilteredCount.set(0);
        patternsFilteredCount.set(0);
        stampsFilteredCount.set(0);
    }
}
