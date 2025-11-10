package dev.ikm.komet.layout;

import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.UUID;

import static dev.ikm.komet.layout.preferences.PreferenceProperty.INITIAL_STRING_VALUE;
import static dev.ikm.tinkar.common.util.uuid.UuidUtil.NIL_UUID;

public interface KlRestorable {

    /**
     * Enum representing the keys used to manage and access user preferences
     * related to gadgets within the application. This enum defines constants
     * that are essential for storing and retrieving configuration or state
     * information for restoring windows or initializing preferences. These items
     * are stored in, and retrieved from, a {@link KometPreferences} instance.
     */
    enum PreferenceKeys implements PropertyWithDefault {
        /**
         * Boolean string representing if the preferences have been initialized.
         */
        INITIALIZED(Boolean.FALSE),

        /**
         * Fully qualified name of the factory class. Used to restore the {@link KlView}
         * from preferences.
         */
        FACTORY_CLASS_NAME(INITIAL_STRING_VALUE),

        /**
         * Represents the name of the specific implementation of a {@link KlView}
         * that can be restored from preferences. This key is used to identify
         * and manage restoration of the window's state during application initialization
         * or when reloading user preferences.
         */
        NAME_FOR_RESTORE(INITIAL_STRING_VALUE),

        /**
         * Represents a preference key used to identify and store the unique identifier (UUID)
         * of a KL object. This key is primarily utilized for restoring a specific KL object
         * within the user preferences system, ensuring the correct object is reloaded during
         * application initialization or when accessing saved preferences.
         *
         * Default value: NIL_UUID, indicating an empty or non-assigned UUID.
         */
        KL_OBJECT_ID(NIL_UUID);

        /**
         * Represents the default value associated with a preference key.
         * This value provides an initial or fallback configuration used
         * when no other value has been explicitly set or retrieved.
         */
        Object defaultValue;

        PreferenceKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object defaultValue() {
            return defaultValue;
        }
    }

    /**
     * Restores the {@code KlView} from the user preferences.
     * The {@code KlView.Factory.restore(KometPreferences)}
     * method should call this method after the  {@code KlRestorable}
     * object has been constructed, but before returning the object to the caller.
     *
     * @see Factory#restore(KometPreferences)
     */
    void restoreFromPreferencesOrDefaults();

    /**
     * Retrieves the {@code KometPreferences} associated with the current {@code KlObject} instance.
     * These preferences provide configuration and customization options for the gadget's behavior.
     *
     * @return the {@code KometPreferences} associated with the current {@code KlGadget}.
     */
    KometPreferences preferences();

    default KlPreferencesFactory childPreferencesFactory(KlPeerable child) {
        return KlPreferencesFactory.create(preferences(), child.getClass());
    }

    default KlPreferencesFactory childPreferencesFactory(Class<? extends KlPeerable> childClass) {
        return KlPreferencesFactory.create(preferences(), childClass);
    }

    default void setFactoryClassName(String factoryClassName) {
        if (factoryClassName.equals("dev.ikm.komet.layout.KlArea$Factory")) {
            throw new IllegalArgumentException("viewFactory must be instantiatable. Interface class provided: " + factoryClassName);
        }
        preferences().put(PreferenceKeys.FACTORY_CLASS_NAME, factoryClassName);
    }

    default String getFactoryClassName() {
        return preferences().get(PreferenceKeys.FACTORY_CLASS_NAME, Factory.class.getName());
    }

    default void setNameForRestore(String nameForRestore) {
        preferences().put(PreferenceKeys.NAME_FOR_RESTORE, nameForRestore);
    }

    default String getNameForRestore() {
        return preferences().get(PreferenceKeys.NAME_FOR_RESTORE, camelCaseToWords(this.getClass().getSimpleName()));
    }

    default boolean isInitialized() {
        return preferences().getBoolean(PreferenceKeys.INITIALIZED, false);
    }
    default void setInitialized() {
        preferences().putBoolean(PreferenceKeys.INITIALIZED, true);
    }

    default void setKlObjectId(UUID klObjectId) {
        preferences().putUuid(PreferenceKeys.KL_OBJECT_ID, klObjectId);
    }

    default UUID getKlObjectId() {
        return preferences().getUuid(PreferenceKeys.KL_OBJECT_ID, NIL_UUID);
    }

    /**
     * Retrieves the unique identifier for this KlObject. Note that the UUID for the
     * KlObject is independent of whatever entity it may contain at a particular instant. And the
     * UUID will not change across the life of this Knowledge Layout Component.
     *
     * @return the UUID representing the unique identifier of the KlObject.
     */
    default UUID klObjectId() {
        return UuidT5Generator.get(this.getClass().getName() + this.hashCode());
    }

    sealed interface Factory<FX, KL extends KlView<FX>>
            permits KlView.Factory {

        KL create(KlPreferencesFactory preferencesFactory);

        KL restore(KometPreferences preferences);

        /**
         * Retrieves the concrete class of the {@code KlView}
         * product that is produced by the factory.
         *
         * @return A {@link Class} object representing the class type of the implementation
         *         of {@link KlView} associated with this factory.
         */
        default Class<?> productClass() {
            return this.getClass().getEnclosingClass();
        }

        /**
         * Retrieves the name of the product created by this factory.
         * Each instance does not have a resulting unique name.
         * Product is the textbook term in the context of design patterns
         * (as in the “Factory Method” and “Abstract Factory” Gang of Four patterns).
         *
         * @return A string representing a generic name of the product.
         */
        default String productName() {
            return camelCaseToWords(this.getClass().getEnclosingClass().getSimpleName());
        }

        /**
         * Retrieves the name of this factory.
         *
         * @return A string representing the name of the factory.
         */
        default String factoryName() {
            return camelCaseToWords(this.getClass().getEnclosingClass().getSimpleName()) +
                    " " + camelCaseToWords(this.getClass().getSimpleName());
        }

        /**
         * Retrieves the compatible service types for the area factory. These types can
         * be used to discover other factories that can be used interchangeably with
         * this factory and can be dynamically substituted for user preferences or
         * specific functionality.
         *
         * @return the service type name of the area factory as a String.
         */
        default ImmutableList<Class<?>> areaFactoryServiceTypes() {
            MutableList<Class<?>> interfaces = Lists.mutable.empty();
            Class<?> clazz = null;
            while (clazz != null) {
                for (Class<?> iface : clazz.getInterfaces()) {
                    if (KlArea.Factory.class.isAssignableFrom(iface)) {
                        interfaces.add(iface);
                    }

                }
                clazz = clazz.getSuperclass();
            }
            return interfaces.toImmutable();
        }

        /**
         * Retrieves a description of the product created by this factory.
         * Product is the textbook term in the context of design patterns
         * (as in the “Factory Method” and “Abstract Factory” Gang of Four patterns).
         *
         * @return A string representing the description of the factory's product.
         */
        default String productDescription() {
            StringBuilder description = new StringBuilder("A Knowledge Layout object that implements the ");
            ImmutableList<Class<?>> areaFactoryServiceTypes = areaFactoryServiceTypes();
            areaFactoryServiceTypes.forEach(klInterfaceClass -> description.append(klInterfaceClass.getSimpleName()).append(", "));
            description.delete(description.length() - 2, description.length());
            if (areaFactoryServiceTypes.size() > 1) {
                description.append(" interfaces.");
            } else {
                description.append(" interface.");
            }
            return description.toString();
        }

        /**
         * Provides a palette icon for the layout tool that represents this factory.
         *
         * @return A Node object representing the visual icon of the layout palette.
         */
        default Node layoutPaletteIcon() {
            Label paletteIcon = new Label(productName());
            Tooltip.install(paletteIcon, new Tooltip(productDescription()));
            return paletteIcon;
        }
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

}
