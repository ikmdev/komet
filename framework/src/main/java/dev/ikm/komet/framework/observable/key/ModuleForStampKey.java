package dev.ikm.komet.framework.observable.key;

import dev.ikm.tinkar.common.id.Nid;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record ModuleForStampKey(long nid, long stampNid) implements FeatureKey.VersionFeature.Stamp.Module {
    public ModuleForStampKey() {
        this(FeatureKey.WILDCARD, FeatureKey.WILDCARD);
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
    public static ModuleForStampKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            default -> new ModuleForStampKey(in.readNid(), in.readNid());
        };
    }
}
