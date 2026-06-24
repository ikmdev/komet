package dev.ikm.komet.kleditorapp.view;

import dev.ikm.komet.layout.KlRestorable;
import dev.ikm.komet.layout.area.KlSupplementalArea;
import dev.ikm.komet.layout.area.KlToolArea;

/**
 * Represents an item in the editor's "Controls" palette: a placeable supplemental area,
 * identified by its {@link KlSupplementalArea.Factory} class name (the value persisted as the
 * area's {@code FACTORY_CLASS_NAME} when it is dropped into a layout and saved).
 */
public class ControlBrowserItem {

    private final String label;
    private final String factoryClassName;

    /**
     * Builds a palette item for the given discovered factory.
     *
     * @param factory a {@code ServiceLoader}/{@code PluggableService}-discovered supplemental
     *                area factory
     */
    @SuppressWarnings("rawtypes")
    public ControlBrowserItem(KlSupplementalArea.Factory factory) {
        this.factoryClassName = factory.getClass().getName();
        this.label = deriveLabel(factory);
    }

    @SuppressWarnings("rawtypes")
    private static String deriveLabel(KlSupplementalArea.Factory factory) {
        if (factory instanceof KlToolArea.Factory toolFactory) {
            return toolFactory.toolName();
        }
        Class<?> enclosing = factory.getClass().getEnclosingClass();
        String simpleName = (enclosing != null)
                ? enclosing.getSimpleName()
                : factory.getClass().getSimpleName();
        return KlRestorable.camelCaseToWords(simpleName);
    }

    /**
     * The human-readable label shown in the palette.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * The fully-qualified factory class name carried on the dragboard and persisted as the
     * placed area's {@code FACTORY_CLASS_NAME}.
     *
     * @return the factory class name
     */
    public String getFactoryClassName() {
        return factoryClassName;
    }
}
