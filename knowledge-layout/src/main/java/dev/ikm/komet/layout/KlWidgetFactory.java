package dev.ikm.komet.layout;

import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.util.function.Supplier;


public interface KlWidgetFactory<T extends KlWidget> extends KlFactory<T> {

    /**
     * Creates an instance of the specified KlWidget.
     *
     * @param preferencesSupplier a supplier providing KometPreferences
     *                            used to store the widget's initial
     *                            configuration preferences
     * @return an instance of the KlWidget type created by this factory
     */
    T create(Supplier<KometPreferences> preferencesSupplier);

    /**
     * Provides a palette icon for the layout tool that represents this factory.
     *
     * @return A Node object representing the visual icon of the layout palette.
     */
    default Node layoutPaletteIcon() {
        Label paletteIcon = new Label(klWidgetName());
        Tooltip.install(paletteIcon, new Tooltip(klWidgetDescription()));
        return paletteIcon;
    }

    /**
     * Retrieves the KlWidget interface of the KlWidget produced by the factory.
     *
     * @return A {@link Class} object representing the class type of the field
     *         interface extending {@link KlWidget}.
     */
    Class<? extends KlWidget> klWidgetInterfaceClass();


    /**
     * Retrieves the name of the widget created by this factory.
     *
     * @return A string representing the name of the widget.
     */
    default String klWidgetName() {
        return KlFactory.camelCaseToWords(this.klImplementationClass().getSimpleName());
    }

    /**
     * Retrieves a description of the widget created by this factory.
     *
     * @return A string representing the description of the widget.
     */
    default String klWidgetDescription() {
        return "A Knowledge Layout Widget that implements the " +
                KlFactory.camelCaseToWords(this.klWidgetInterfaceClass().getSimpleName() +
                        " interface.");
    }

}
