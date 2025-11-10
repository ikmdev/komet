package dev.ikm.komet.framework.observable.key;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record PatternPurposeKey(int nid, int stampNid) implements FeatureKey.VersionFeature.Pattern.PatternPurpose {
    public PatternPurposeKey() {
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
    public static PatternPurposeKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            default -> new PatternPurposeKey(in.readNid(), in.readNid());
        };
    }
}
