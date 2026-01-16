package dev.ikm.komet.layout;

import dev.ikm.komet.framework.observable.FieldLocator;
import dev.ikm.tinkar.common.binary.*;
import dev.ikm.tinkar.common.service.PluggableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public record GridLayoutForComponent(String fieldFactoryName, FieldLocator fieldLocator,
                                     GridLayout gridLayout)
        implements Encodable {
        private static final Logger LOG = LoggerFactory.getLogger(GridLayoutForComponent.class);

    <KL extends KlObject> Optional<KlFactory<KL>> makeFactory() {
        if (fieldFactoryName == null || fieldFactoryName.isBlank()) {
            return Optional.empty();
        }
        try {
            Class factoryClass = PluggableService.forName(fieldFactoryName);
            Constructor constructor = factoryClass.getConstructor();
            KlFactory<KL> factory = (KlFactory<KL>) constructor.newInstance();
            return Optional.of(factory);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LOG.error("Constructing: {}", fieldFactoryName, e);
        }
        return Optional.empty();
    }

    @Encoder
    @Override
    public void encode(EncoderOutput out) {
        out.writeString(fieldFactoryName);
        fieldLocator.encode(out);
        gridLayout.encode(out);
    }

    @Decoder
    public static GridLayoutForComponent decode(DecoderInput in) {
        return new GridLayoutForComponent(in.readString(),
                FieldLocator.decode(in), GridLayout.decode(in));
    }
}
