package dev.ikm.komet.framework.observable.key;

import dev.ikm.tinkar.common.id.Nid;
import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record FieldDefinitionListKey(long nid, long stampNid) implements FeatureKey.VersionFeature.Pattern.FieldDefinitionList {
    public FieldDefinitionListKey() {
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
    public static FieldDefinitionListKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            default -> new FieldDefinitionListKey(in.readNid(), in.readNid());
        };
    }
}
