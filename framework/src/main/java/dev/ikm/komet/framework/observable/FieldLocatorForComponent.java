package dev.ikm.komet.framework.observable;

public sealed interface FieldLocatorForComponent extends FieldLocator
    permits ComponentFieldLocator, ComponentFieldListElementLocator {
}
