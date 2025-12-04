package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.common.binary.*;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;

public sealed interface FieldLocator extends Encodable
        permits FieldLocatorForAssociatedComponent, FieldLocatorForComponent {

    enum PermittedImplementation {
        AssociatedComponentField(AssociatedComponentField.class),
        AssociatedComponentFieldListElement(AssociatedComponentFieldListElement.class),
        ComponentFieldLocator(ComponentFieldLocator.class),
        ComponentFieldListElementLocator(ComponentFieldListElementLocator.class);

        final Class implementationCLass;

        PermittedImplementation(Class implementationCLass) {
            this.implementationCLass = implementationCLass;
        }

        static PermittedImplementation getForClass(FieldLocator fieldLocator) {
            for (PermittedImplementation permittedImplementation : PermittedImplementation.values()) {
                if (permittedImplementation.implementationCLass.equals(fieldLocator.getClass())) {
                    return permittedImplementation;
                }
            }
            throw new IllegalStateException("FieldLocator " + fieldLocator + " is not available. ");
        }
    }

    FieldCategory category();

    default <T> ObservableField<T> get(ObservableComponent observableComponent, StampCalculator stampCalculator) {
        return switch (this) {
            case FieldLocatorForAssociatedComponent associatedComponentLocator -> {
                ObservableComponent associatedComponent = ObservableEntity.get(associatedComponentLocator.associatedComponentNid());
                yield associatedComponentLocator.componentFieldLocator().get(associatedComponent, stampCalculator);
            }
            case FieldLocatorForComponent componentLocator ->
                    locateComponentField(componentLocator, observableComponent, stampCalculator);
        };
    }

    static <T> ObservableField<T> locateComponentField(FieldLocatorForComponent componentLocator, ObservableComponent observableComponent, StampCalculator stampCalculator) {

        return switch (componentLocator) {
            case ComponentFieldLocator componentFieldLocator -> switch (observableComponent) {
                case ObservableEntity observableEntity ->
                        FieldLocatorForEntity.locate(observableEntity, componentFieldLocator, stampCalculator);
                case ObservableVersion observableVersion ->
                        FieldLocatorForVersion.locate(observableVersion, componentFieldLocator, stampCalculator);
            };

            case ComponentFieldListElementLocator componentFieldListElementLocator -> switch (observableComponent) {
                case ObservableEntity observableEntity ->
                        FieldLocatorListElementForEntity.locate(observableEntity, componentFieldListElementLocator, stampCalculator);
                case ObservableVersion observableVersion ->
                        FieldLocatorListElementForVersion.locate(observableVersion, componentFieldListElementLocator, stampCalculator);
            };
        };
    }

    @Encoder
    default void encode(EncoderOutput out) {
        PermittedImplementation implementation = PermittedImplementation.getForClass(this);
        out.writeString(implementation.name());
        this.subEncode(out);
    }

    void subEncode(EncoderOutput out);

    @Decoder
    static FieldLocator decode(DecoderInput in) {
        String implementationName = in.readString();
        switch (PermittedImplementation.valueOf(implementationName)) {
            case ComponentFieldLocator -> ComponentFieldLocator.decode(in);
            case ComponentFieldListElementLocator -> ComponentFieldListElementLocator.decode(in);
            case AssociatedComponentField -> AssociatedComponentField.decode(in);
            case AssociatedComponentFieldListElement -> AssociatedComponentFieldListElement.decode(in);
        }
        throw new IllegalStateException("Implementation " + implementationName + " is not available. ");
    }
}