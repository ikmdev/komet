package dev.ikm.komet.framework.observable.key;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record SemanticFieldListKey(int nid, int stampNid) implements FeatureKey.VersionFeature.Semantic.FieldList {
    public SemanticFieldListKey() {
        this(FeatureKey.WILDCARD, FeatureKey.WILDCARD);
    }

    @Override
    public boolean isResolvable() {
        return nid != FeatureKey.WILDCARD && stampNid != FeatureKey.WILDCARD;
    }

    @Override
    public void encode(EncoderOutput out) {
        out.writeNid(nid);
        out.writeNid(stampNid);
    }

    @Decoder
    public static SemanticFieldListKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            default -> new SemanticFieldListKey(in.readNid(), in.readNid());
        };
    }
}
