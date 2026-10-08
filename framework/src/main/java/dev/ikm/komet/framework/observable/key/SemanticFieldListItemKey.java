package dev.ikm.komet.framework.observable.key;

import dev.ikm.tinkar.common.id.Nid;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record SemanticFieldListItemKey(long nid, int index, long patternNid,
                                       long stampNid) implements FeatureKey.VersionFeature.Semantic.FieldListItem {
    public SemanticFieldListItemKey(int index, long patternNid) {
        this(FeatureKey.WILDCARD, index, patternNid, FeatureKey.WILDCARD);
    }

    @Override
    public boolean isResolvable() {
        return !Nid.isNotApplicable(nid) && index >= 0 && !Nid.isNotApplicable(patternNid) && !Nid.isNotApplicable(stampNid);
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
            default -> new SemanticFieldListItemKey(in.readNid(), Math.toIntExact(in.readNid()), in.readNid(), in.readNid());
        };
    }
}
