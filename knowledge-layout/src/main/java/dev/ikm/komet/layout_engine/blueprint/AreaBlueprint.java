package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlPeerToRegion;
import dev.ikm.komet.layout.LayoutKey;
import dev.ikm.komet.layout.LayoutOverrides;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Abstract class representing a blueprint for creating configurable Widgets. This class provides
 * methods for managing widget preferences, dynamic configuration updates, and layout restoration.
 * It extends the {@code GadgetBlueprint} class and implements the {@code KlWidget} interface to
 * adhere to specific widget behaviors.
 *
 * @param <FX> the type parameter extending from {@code Parent}, representing the root node of
 *            the widget's layout.
 */
public sealed abstract class AreaBlueprint<FX extends Region>
        extends StateAndContextBlueprint<FX>
        implements KlPeerToRegion<FX>
        permits SupplementalAreaBlueprint, FeatureAreaBlueprint, FeatureListAreaBlueprint, ParentAreaBlueprint {

    protected static final Logger LOG = LoggerFactory.getLogger(AreaBlueprint.class);

    {
        subscribeToChanges();
    }
    /**
     * Restores a {@code AreaBlueprint} object with the specified preferences.
     * <p>
     * This constructor initializes the {@code AreaBlueprint} by invoking its superclass
     * constructor and performing necessary setup for synchronizing the widget's state
     * with the provided preferences and gadget.
     *
     * @param preferences the {@code KometPreferences} instance associated with the widget blueprint,
     *                    used for managing and restoring settings.
     * @param fxObject    the fxObject instance of type {@code T} to be configured and encapsulated
     *                    within the area blueprint. Checked to be a Java Fx {@code Window} or {@code Node}.
     * @throws IllegalStateException if the gadget object provided does not comply with
     *                               the expected specifications (e.g., back-end validation by superclass).
     */
    public AreaBlueprint(KometPreferences preferences, FX fxObject) {
        super(preferences, fxObject);
    }

    /**
     * Constructs a new instance of {@code AreaBlueprint} with the specified preferences factory,
     * area factory, and product. This constructor initializes the area blueprint by
     * delegating initialization to the superclass and invoking the {@code setup} method to
     * configure and synchronize the widget's state.
     *
     * @param preferencesFactory the factory responsible for creating and managing preferences,
     *                           which will be used for handling the widget's persistent settings
     *                           and configuration.
     * @param areaFactory      the instance of {@code KlArea.Factory} representing the area-related
     *                           configuration and metadata for initializing the area blueprint.
     * @param fxObject           the Fx object instance of type {@code T} to be encapsulated,
     *                           synchronized, and configured within the widget blueprint.
     *                           Checked to be a Java Fx {@code Window} or {@code Node}.
     */
    public AreaBlueprint(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory, FX fxObject) {
        super(preferencesFactory, areaFactory, fxObject);
    }


    /**
     * Restores the layout and configuration settings of a widget from either the stored preferences
     * or the associated default values. This method iterates through all defined preference keys and
     * applies the corresponding settings to the widget.
     * <p>
     * The method dynamically adjusts various properties of the widget, such as growth priorities,
     * alignment, grid positioning, and margins. For each preference key, it fetches the value from
     * persistent preferences storage or uses the default value defined in the {@link KlArea.PreferenceKeys}
     * enum. The fetched values are then applied to the widget using the appropriate configuration method.
     * <p>
     * The following settings are restored:
     * <p>- Horizontal and vertical growth priorities (H_GROW, V_GROW)
     * <p>- Horizontal and vertical alignment (H_ALIGNMENT, V_ALIGNMENT)
     * <p>- Grid positioning indices (COLUMN_INDEX, ROW_INDEX)
     * <p>- Grid span for rows and columns (COLUMN_SPAN, ROW_SPAN)
     * <p>- Margin values around the widget (MARGIN)
     * <p>
     * This method utilizes helper methods such as {@code fromDoubleArray} and {@code toDoubleArray}
     * for converting between custom data structures (e.g., insets and double arrays).
     * <p>
     * Note: Preferences are accessed through the {@link KometPreferences} instance retrieved via
     * the {@code preferences()} method. Default values are specified in the {@link KlArea.PreferenceKeys} enum.
     */
    public final void restoreFromPreferencesOrDefaults() {
        LOG.debug("Restoring from preferences or defaults for {}", this.getClass().getSimpleName());
        try {
            for (PreferenceKeys key : PreferenceKeys.values()) {
                switch (key) {
                    case INITIALIZED -> this.setInitialized();
                    case FACTORY_CLASS_NAME -> this.setFactoryClassName(preferences().get(key, key.defaultValue().toString()));
                    case KL_OBJECT_ID -> this.setKlObjectId(preferences().getUuid(key, UUID.randomUUID()));
                    case NAME_FOR_RESTORE -> this.setNameForRestore(preferences().get(key, key.defaultValue().toString()));
                    default -> throw new IllegalStateException("Unexpected value: " + key);
                }
            }
            for (KlArea.PreferenceKeys key : KlArea.PreferenceKeys.values()) {
                switch (key) {
                    case COLUMN_INDEX -> GridPane.setColumnIndex(fxObject(), preferences().getInt(key, (Integer) key.defaultValue()));
                    case COLUMN_SPAN -> GridPane.setColumnSpan(fxObject(), preferences().getInt(key, (Integer) key.defaultValue()));
                    case FILL_HEIGHT -> this.setFillHeight(preferences().getBoolean(key, (Boolean) key.defaultValue()));
                    case FILL_WIDTH -> this.setFillWidth(preferences().getBoolean(key, (Boolean) key.defaultValue()));
                    case H_ALIGNMENT -> GridPane.setHalignment(fxObject(), javafx.geometry.HPos.valueOf(preferences().get(key, key.defaultValue().toString())));
                    case H_GROW -> GridPane.setHgrow(fxObject(), Priority.valueOf(preferences().get(key, key.defaultValue().toString())));
                    case LAYOUT_KEY -> this.setLayoutKeyForArea(preferences().getObject(key, (LayoutKey.ForArea) key.defaultValue()));
                    case MARGIN -> GridPane.setMargin(fxObject(), fromDoubleArray(preferences().getDoubleArray(key, toDoubleArray(Insets.EMPTY))));
                    case MAX_HEIGHT -> this.setMaxHeight(preferences().getDouble(key, (Double) key.defaultValue()));
                    case MAX_WIDTH -> this.setMaxWidth(preferences().getDouble(key, (Double) key.defaultValue()));
                    case PREFERRED_HEIGHT -> this.setPrefHeight(preferences().getDouble(key, (Double) key.defaultValue()));
                    case PREFERRED_WIDTH -> this.setPrefWidth(preferences().getDouble(key, (Double) key.defaultValue()));
                    case ROW_INDEX -> GridPane.setRowIndex(fxObject(), preferences().getInt(key, (Integer) key.defaultValue()));
                    case ROW_SPAN -> GridPane.setRowSpan(fxObject(), preferences().getInt(key, (Integer) key.defaultValue()));
                    case VISIBLE -> this.setVisible(preferences().getBoolean(key, (Boolean) key.defaultValue()));
                    case V_ALIGNMENT -> GridPane.setValignment(fxObject(), javafx.geometry.VPos.valueOf(preferences().get(key, key.defaultValue().toString())));
                    case V_GROW -> GridPane.setVgrow(fxObject(), Priority.valueOf(preferences().get(key, key.defaultValue().toString())));
                    case LAYOUT_OVERRIDES_PERSISTED_IN_PREFERENCES -> {
                        if (preferences().hasKey(key) && this instanceof KlArea<?> area) {
                            LayoutOverrides layoutOverrides = LayoutOverrides.restore(preferences());
                            area.setLayoutOverrides(layoutOverrides);
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + key);
                }
            }
        } catch (IllegalStateException e) {
            LOG.error("Error restoring from preferences or defaults for {} from {} {} in {}",
                    this.getClass().getSimpleName(), this.preferences().name(),
                    this.preferences().delegateHash(), this.preferences().absolutePath());
            throw new RuntimeException(e);
        }
        subAreaRestoreFromPreferencesOrDefault();
    }

    protected abstract void subAreaRestoreFromPreferencesOrDefault();

    /**
     * Converts an array of doubles into an Insets object. If the array contains exactly four elements,
     * they are used as the values for top, right, bottom, and left insets. If the array does not contain
     * four elements, a default Insets object with all values set to 0 is returned.
     *
     * @param array the array of doubles to be converted into an Insets object.
     *              The array must have exactly four elements representing top, right, bottom, and left insets.
     * @return an Insets object created from the given array. If the array does not have exactly four elements,
     *         a default Insets object with all values set to 0 is returned.
     */
    private static Insets fromDoubleArray(double[] array) {
        if (array.length == 4) {
            return new Insets(array[0], array[1], array[2], array[3]);
        } else {
            return new Insets(0);
        }
    }

    /**
     * Converts an Insets object into an array of doubles, where the elements
     * in the array represent the top, right, bottom, and left values of the Insets,
     * in that order.
     *
     * @param insets the Insets object to be converted into a double array.
     *               The Insets object must not be null.
     * @return an array of doubles containing four elements representing the
     *         top, right, bottom, and left insets, respectively.
     */
    private static double[] toDoubleArray(Insets insets) {
        return new double[]{insets.getTop(), insets.getRight(), insets.getBottom(), insets.getLeft()};
    }

    /**
     * Subscribes to changes in specific preference keys and attaches appropriate listeners
     * for handling preference updates. This method is unique from some other blueprint subscriptions
     * since the values are stored in an observable map. Preferences not in the observable map
     * should be put in an independent enumeration and handled separately.
     */
    private void subscribeToChanges() {
        addPreferenceSubscription(this.properties().subscribe(this::preferencesChanged));
    }

    @Override
    protected void subContextRevert() {
        restoreFromPreferencesOrDefaults();
        subAreaRevert();
    }

    @Override
    protected void subContextSave() {
        for (KlArea.PreferenceKeys key : KlArea.PreferenceKeys.values()) {
            switch (key) {
                case COLUMN_INDEX -> preferences().putInt(key, this.getColumnIndex());
                case COLUMN_SPAN -> preferences().putInt(key, this.getColspan());
                case FILL_HEIGHT -> preferences().putBoolean(key, this.getFillHeight());
                case FILL_WIDTH -> preferences().putBoolean(key, this.getFillWidth());
                case H_ALIGNMENT -> preferences().put(key, this.getHalignment().name());
                case H_GROW -> preferences().put(key, this.getHgrow().name());
                case LAYOUT_KEY -> preferences().putObject(key, this.getLayoutKeyForArea());
                case MARGIN -> preferences().putDoubleArray(key, toDoubleArray(this.getMargins()));
                case MAX_HEIGHT -> preferences().putDouble(key, this.getMaxHeight());
                case MAX_WIDTH -> preferences().putDouble(key, this.getMaxWidth());
                case PREFERRED_HEIGHT -> preferences().putDouble(key, this.getPrefHeight());
                case PREFERRED_WIDTH -> preferences().putDouble(key, this.getPrefWidth());
                case ROW_INDEX -> preferences().putInt(key, this.getRowIndex());
                case ROW_SPAN -> preferences().putInt(key, this.getRowspan());
                case VISIBLE -> preferences().putBoolean(key, this.getVisible());
                case V_ALIGNMENT -> preferences().put(key, this.getValignment().name());
                case V_GROW -> preferences().put(key, this.getVgrow().name());
            }
        }

        subAreaSave();
    }

    protected abstract void subAreaRevert();
    protected abstract void subAreaSave();
}
