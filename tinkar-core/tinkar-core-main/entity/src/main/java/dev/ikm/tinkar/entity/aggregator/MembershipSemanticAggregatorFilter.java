package dev.ikm.tinkar.entity.aggregator;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

public class MembershipSemanticAggregatorFilter extends EntityAggregatorFilter {
    private static final Logger LOG = LoggerFactory.getLogger(MembershipSemanticAggregatorFilter.class);
    private final List<PublicId> membershipPatternsToPassFilter;

    public MembershipSemanticAggregatorFilter(EntityAggregator entityAggregator, List<PublicId> membershipPatternsToPassFilter) {
        super(entityAggregator);
        this.membershipPatternsToPassFilter = membershipPatternsToPassFilter;
    }

    @Override
    public EntityCountSummary aggregate(IntConsumer nidConsumer) {
        initCounts();

        final List<Integer> membershipPatternNidsToPassFilter =
                membershipPatternsToPassFilter.stream().map(EntityService.get()::nidForPublicId).toList();

        Predicate<EntityVersion> hasZeroSemanticFields = (version) ->
                version instanceof SemanticEntityVersion semanticVersion && semanticVersion.fieldValues().isEmpty();

        IntConsumer membershipSemanticConsumer = (nid) ->
            EntityService.get().getEntity(nid).ifPresentOrElse((entity) -> {
                // Filter out Membership Semantics (i.e., semantics with no fields) that are not in the acceptable list
                if (entity instanceof SemanticEntity<?> semanticEntity
                        && semanticEntity.versions().stream().anyMatch(hasZeroSemanticFields)
                        && !membershipPatternNidsToPassFilter.contains(semanticEntity.patternNid())) {
                    semanticsFilteredCount.incrementAndGet();
                    LOG.info("Filtered out Membership Semantic with PublicId: {}", semanticEntity.publicId());
                } else {
                    nidConsumer.accept(nid);
                }
            }, () -> nidConsumer.accept(nid)); // Not the right place to handle missing Entities so pass the filter

        EntityCountSummary unfilteredEntityCounts = entityAggregator.aggregate(membershipSemanticConsumer);
        adjustCounts(unfilteredEntityCounts);
        return summarize();
    }

}
