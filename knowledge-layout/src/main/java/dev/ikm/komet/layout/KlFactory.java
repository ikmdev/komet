package dev.ikm.komet.layout;

import dev.ikm.komet.layout.context.KlContextFactory;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

/**
 * Defines a factory for creating and restoring instances of {@link KlGadget}.
 * This interface provides methods for retrieving metadata about the factory,
 * such as the names and descriptions of the gadgets it produces, and supports
 * customization for layout tools through palette icons.
 *
 * @param <KL> The type of {@link KlGadget} produced by this factory.
 */
public interface KlFactory<KL extends KlObject> {

    /**
     * Creates an instance of type T using the provided KlPreferencesFactory.
     *
     * @param preferencesFactory an instance of KlPreferencesFactory used to provide
     *                           necessary preferences for creating the object.
     * @return an instance of type T created using the given preferencesFactory.
     * @deprecated Use {@code create(KlPreferencesFactory preferencesFactory, GridLayoutForComponentFactory gridLayoutForComponentFactory) }
     */
    @Deprecated
    KL create(KlPreferencesFactory preferencesFactory);


    /**
     * Creates an instance of type KL using the provided KlPreferencesFactory and
     * GridLayoutForComponentFactory. This method delegates the creation process
     * to the {@code create} method with a single KlPreferencesFactory parameter.
     *
     * @param preferencesFactory an instance of KlPreferencesFactory used to provide
     *                           the necessary preferences for creating the object.
     * @param gridLayoutForComponentFactory an instance of GridLayoutForComponentFactory,
     *                                       though it is not utilized in this implementation.
     * @return an instance of type KL created using the given KlPreferencesFactory.
     */
    default KL create(KlPreferencesFactory preferencesFactory, GridLayoutForComponentFactory gridLayoutForComponentFactory) {
        return create(preferencesFactory);
    }

    /**
     * Creates an instance of type T using the provided KlPreferencesFactory
     * and KlContextFactory. This method utilizes the preferences and context
     * configurations to instantiate the desired object.
     *
     * @param preferencesFactory an instance of KlPreferencesFactory used to
     *                           provide necessary preferences for creating the object.
     * @param contextFactory an instance of KlContextFactory used to provide
     *                       the contextual information required for object creation.
     * @return an instance of type T created using the given preferencesFactory
     *         and contextFactory.
     */
    default KL createWithContext(KlPreferencesFactory preferencesFactory, KlContextFactory contextFactory) {
        // Most KL components depend on context provided elsewhere and can ignore the context factory. Override if needed.
        return create(preferencesFactory);
    }
    /**
     * Restores an instance of type T using the provided preferences.
     *
     * @param preferences an instance of KometPreferences that contains the
     *                    configuration or state required to restore the object.
     * @return an instance of type T restored using the given preferences.
     */
    KL restore(KometPreferences preferences);

    /**
     * Provides a palette icon for the layout tool that represents this factory.
     *
     * @return A Node object representing the visual icon of the layout palette.
     */
    default Node layoutPaletteIcon() {
        Label paletteIcon = new Label(klGadgetName());
        Tooltip.install(paletteIcon, new Tooltip(klDescription()));
        return paletteIcon;
    }

    /**
     * Retrieves the KlGadget interface of the KlGadget produced by the factory.
      *
     * @return A {@link Class} object representing the class type of the field
     *         interface extending {@link KlWidget}.
     * @deprecated Use klGadgetInterfaces
     */
    @Deprecated
    default Class<KL> klInterfaceClass() {
        return klGadgetInterfaces().get(0);
    }

    /**
     * Returns an immutable list of all KL interface classes implemented by the KL
     * implementation class that are assignable from KlGadget.
     * <p>
     * This method iterates through the interfaces implemented by the class
     * returned by {@link #klImplementationClass()} and filters for those that
     * can be assigned from the KlGadget interface.
     *
     * @return An {@link ImmutableList} of {@code Class<KL>} objects representing
     * the interfaces extending KlGadget implemented by the KL implementation class.
     */
    default ImmutableList<Class<KL>> klGadgetInterfaces() {
        MutableList<Class<KL>> classes = Lists.mutable.empty();
        for (Class<?> interfaceClass: klImplementationClass().getInterfaces()) {
            if (KlGadget.class.isAssignableFrom(interfaceClass)) {
                classes.add((Class<KL>) interfaceClass);
            }
        }
        return classes.toImmutable();

    }

    /**
     * Retrieves the concrete class of the KlGadget
     * that is produced by the factory.
     *
     * @return A {@link Class} object representing the class type of the implementation
     *         of {@link KlGadget} associated with this factory.
     */
    Class<? extends KL> klImplementationClass();


    /**
     * Retrieves the name of the KlWidget or KlGadget created by this factory.
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
    default String klDescription() {
        StringBuilder description = new StringBuilder("A Knowledge Layout Widget or Gadget that implements the ");
        klGadgetInterfaces().forEach(klInterfaceClass -> description.append(klInterfaceClass.getSimpleName()).append(", "));
        description.delete(description.length() - 2, description.length());
        if (klGadgetInterfaces().size() > 1) {
            description.append("interfaces.");
        } else {
            description.append("interface.");
        }
        return description.toString();
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
