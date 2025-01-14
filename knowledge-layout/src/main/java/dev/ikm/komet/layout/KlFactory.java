package dev.ikm.komet.layout;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public interface KlFactory<T extends KlGadget> {

    /**
     * Provides a palette icon for the layout tool that represents this factory.
     *
     * @return A Node object representing the visual icon of the layout palette.
     */
    default Node layoutPaletteIcon() {
        Label paletteIcon = new Label(klGadgetName());
        Tooltip.install(paletteIcon, new Tooltip(klGadgetDescription()));
        return paletteIcon;
    }

    /**
     * Retrieves the KlGadget interface of the KlGadget produced by the factory.
      *
     * @return A {@link Class} object representing the class type of the field
     *         interface extending {@link KlWidget}.
     */
    Class<T> klInterfaceClass();

    /**
     * Retrieves the concrete class of the KlGadget
     * that is produced by the factory.
     *
     * @return A {@link Class} object representing the class type of the implementation
     *         of {@link KlGadget} associated with this factory.
     */
    Class<? extends T> klImplementationClass();


    /**
     * Retrieves the name of the widget created by this factory.
     *
     * @return A string representing the name of the widget.
     */
    default String klGadgetName() {
        return camelCaseToWords(this.klImplementationClass().getSimpleName());
    }

    /**
     * Retrieves a description of the gadget created by this factory.
     *
     * @return A string representing the description of the gadget.
     */
    default String klGadgetDescription() {
        return "A Knowledge Layout Gadget that implements the " +
                camelCaseToWords(this.klInterfaceClass().getSimpleName() +
                        " interface.");
    }

    /**
     * TODO: move this to a text utility in tinkar-core ?
     * @param camelCaseString
     * @return
     */
    static String camelCaseToWords(String camelCaseString) {
        if (camelCaseString.startsWith("kl")) {
            camelCaseString = camelCaseString.replaceFirst("^kl", "knowledgeLayout");
        } else if (camelCaseString.startsWith("Kl")) {
            camelCaseString = camelCaseString.replaceFirst( "^Kl", "KnowledgeLayout");
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < camelCaseString.length(); i++) {
            char ch = camelCaseString.charAt(i);

            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    result.append(" ");
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString().replace("kl ", "Knowledge Layout ");
    }

    /**
     * Retrieves the name of this factory.
     *
     * @return A string representing the name of the factory.
     */
    default String name() {
        return camelCaseToWords(this.getClass().getSimpleName());
    }
}
