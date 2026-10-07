package dev.ikm.komet.framework.observable.key;

import dev.ikm.tinkar.common.id.Nid;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record VersionKey(long nid, long stampNid) implements FeatureKey.ChronologyFeature.Version {

    public VersionKey(long stampNid) {
        this(FeatureKey.WILDCARD, stampNid);
    }

    @Override
    public boolean isResolvable() {
        return !Nid.isNotApplicable(nid) && !Nid.isNotApplicable(stampNid);
    }

    @Override
    public void encode(EncoderOutput out) {
        out.writeNid(nid);
        out.writeNid(stampNid);
    }
    @Decoder
    public static VersionKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            // if special handling for particular versions, add case condition.
            default -> new VersionKey(in.readNid(), in.readNid());
        };
    }
}
