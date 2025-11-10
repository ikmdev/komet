package dev.ikm.komet.framework.observable.key;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record SemanticFieldListItemKey(int nid, int index, int patternNid,
                                       int stampNid) implements FeatureKey.VersionFeature.Semantic.FieldListItem {
    public SemanticFieldListItemKey(int index, int patternNid) {
        this(FeatureKey.WILDCARD, index, patternNid, FeatureKey.WILDCARD);
    }

    @Override
    public boolean isResolvable() {
        return nid != FeatureKey.WILDCARD && index >= 0 && patternNid != FeatureKey.WILDCARD && stampNid != FeatureKey.WILDCARD;
    }

    @Override
    public void encode(EncoderOutput out) {
        out.writeNid(nid);
        out.writeNid(index);
        out.writeNid(patternNid);
        out.writeNid(stampNid);
    }

    @Decoder
    public static SemanticFieldListItemKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            default -> new SemanticFieldListItemKey(in.readNid(), in.readNid(), in.readNid(), in.readNid());
        };
    }
}
