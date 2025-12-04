package dev.ikm.tinkar.entity.aggregator;

import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.function.IntConsumer;

public class InferredEntityAggregatorFilter extends EntityAggregatorFilter {

    public InferredEntityAggregatorFilter(EntityAggregator entityAggregator) {
        super(entityAggregator);
    }

    @Override
    public EntityCountSummary aggregate(IntConsumer nidConsumer) {
        initCounts();
        final ImmutableList<Integer> inferredNidList = Lists.immutable.of(
                TinkarTerm.INFERRED_NAVIGATION_PATTERN.nid(),
                TinkarTerm.EL_PLUS_PLUS_INFERRED_AXIOMS_PATTERN.nid());

        IntConsumer inferredFilterConsumer = (nid) -> {
            Entity<? extends EntityVersion> entity = EntityService.get().getEntityFast(nid);
            // Filter out inferred Semantics
            if (entity instanceof SemanticEntity semanticEntity
                && inferredNidList.contains(semanticEntity.patternNid())) {
                semanticsFilteredCount.incrementAndGet();
            } else {
                nidConsumer.accept(nid);
            }
        };

        EntityCountSummary unfilteredEntityCounts = entityAggregator.aggregate(inferredFilterConsumer);
        adjustCounts(unfilteredEntityCounts);
        return summarize();
    }

}
