package dev.ikm.komet.framework.observable.key;

import dev.ikm.komet.framework.observable.FeatureKey;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;

public record ReferencedComponentForSemanticKey(int nid) implements FeatureKey.ChronologyFeature.Semantic.ReferencedComponent {
    public ReferencedComponentForSemanticKey() {
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
    public static ReferencedComponentForSemanticKey decode(DecoderInput in) {
        return switch (Encodable.checkVersion(in)) {
            default -> new ReferencedComponentForSemanticKey(in.readNid());
        };
    }
}
