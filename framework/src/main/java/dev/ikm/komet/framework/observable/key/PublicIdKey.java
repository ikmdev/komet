package dev.ikm.komet.framework.observable.key;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record PublicIdKey(int nid) implements FeatureKey.ChronologyFeature.PublicId {
    public PublicIdKey() {
        this(FeatureKey.WILDCARD);
    }

    @Override
    public boolean isResolvable() {
        return nid != FeatureKey.WILDCARD;
    }

    @Override
    public void encode(EncoderOutput out) {
        out.writeNid(nid);
    }

    @Decoder
    public static PublicIdKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            // if special handling for particular versions, add case condition.
            default -> new PublicIdKey(in.readNid());
        };
    }
}
