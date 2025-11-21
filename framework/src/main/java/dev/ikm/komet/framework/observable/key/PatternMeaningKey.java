package dev.ikm.komet.framework.observable.key;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record PatternMeaningKey(int nid, int stampNid) implements FeatureKey.VersionFeature.Pattern.PatternMeaning {
    public PatternMeaningKey() {
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
    public static PatternMeaningKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            default -> new PatternMeaningKey(in.readNid(), in.readNid());
        };
    }
}
