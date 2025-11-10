package dev.ikm.komet.framework.observable.key;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record VersionKey(int nid, int stampNid) implements FeatureKey.ChronologyFeature.Version {
    public VersionKey {
        checkForIndex(nid);
        checkForIndex(stampNid);
    }

    public VersionKey(int stampNid) {
        this(FeatureKey.WILDCARD, checkForIndex(stampNid));
    }

    static int checkForIndex(int nid) {
        if (nid < 0) {
            return nid;
        }
        throw new IllegalStateException("Not a nid, probably an index because value >= 0: " + nid);
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
    public static VersionKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            // if special handling for particular versions, add case condition.
            default -> new VersionKey(in.readNid(), in.readNid());
        };
    }
}
