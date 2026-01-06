package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.framework.observable.Feature;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.component.FeatureDefinition;
import javafx.beans.property.Property;
import javafx.scene.layout.Region;
import javafx.util.Subscription;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract blueprint for editable field areas.
 * <p>
 * This class handles the common plumbing required for binding a UI control to an
 * {@link ObservableField.Editable}, including:
 * <ul>
 *   <li>Managing the {@link ObservableField.Editable} reference.</li>
 *   <li>Providing a standard {@link #rebind(ObservableField.Editable)} mechanism.</li>
 *   <li>Managing change listener subscriptions and cleaning them up on unbind/rebind.</li>
 *   <li>Delegating {@code save()} and {@code revert()} to the editable field.</li>
 * </ul>
 * <p>
 * Subclasses only need to implement:
 * <ul>
 *   <li>{@link #createControl()}: Create the UI control instance.</li>
 *   <li>{@link #bindControlToEditable(ObservableField.Editable)}: Bind the control's value property to the editable.</li>
 *   <li>{@link #unbindControlFromEditable()}: Unbind the control from the previous editable.</li>
 *   <li>{@link #getControlValueProperty()}: Return the control's value property for standard bidirectional binding.</li>
 *   <li>{@link #updateControlTitle(String)}: Set the title on the control.</li>
 * </ul>
 *
 * @param <DT> The data type of the field (e.g., Boolean, String, Integer).
 * @param <FX> The JavaFX Region type used as the root of this area.
 */
public abstract class EditableFieldAreaBlueprint<DT, FX extends Region>
        extends FeatureAreaBlueprint<DT, Feature<DT>, FX> {

    /** The current editable field wrapper, if any. */
    private ObservableField.Editable<DT> currentEditable;

    /** Subscriptions for change listeners on the current editable. */
    private final List<Subscription> editableSubscriptions = new ArrayList<>();

    /**
     * Constructs a new {@code EditableFieldAreaBlueprint} for restoration from preferences.
     *
     * @param preferences the preferences to restore from
     * @param fxObject the JavaFX root region
     */
    protected EditableFieldAreaBlueprint(KometPreferences preferences, FX fxObject) {
        super(preferences, fxObject);
        initializeControl();
    }

    /**
     * Constructs a new {@code EditableFieldAreaBlueprint} for creation.
     *
     * @param preferencesFactory the preferences factory
     * @param areaFactory the area factory
     * @param fxObject the JavaFX root region
     */
    protected EditableFieldAreaBlueprint(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory, FX fxObject) {
        super(preferencesFactory, areaFactory, fxObject);
        initializeControl();
    }

    /**
     * Called during construction to initialize the UI control.
     * Subclasses should create their control and add it to the fxObject.
     */
    private void initializeControl() {
        // Subclasses will add their control to fxObject() in createControl()
        createControl();
    }

    /**
     * Creates the UI control and adds it to {@link #fxObject()}.
     * <p>
     * This method is called once during construction. Implementations should
     * instantiate the control (e.g., KLBooleanControl) and add it to the
     * layout container returned by {@link #fxObject()}.
     */
    protected abstract void createControl();

    /**
     * Returns the value property of the UI control.
     * <p>
     * This is used for standard bidirectional binding with the editable's
     * {@code editableValueProperty()}.
     *
     * @return the control's value property
     */
    protected abstract Property<DT> getControlValueProperty();

    /**
     * Binds the UI control to the given editable field.
     * <p>
     * This method is called after {@link #unbindControlFromEditable()} when a new
     * editable is set. Implementations should:
     * <ol>
     *   <li>Bind the control's value property bidirectionally to {@code editable.editableValueProperty()}.</li>
     *   <li>Add any additional change listeners and register them via {@link #addEditableSubscription(Subscription)}.</li>
     *   <li>Update the control's title/label using field definition metadata.</li>
     * </ol>
     *
     * @param editable the new editable field to bind to
     */
    protected abstract void bindControlToEditable(ObservableField.Editable<DT> editable);

    /**
     * Unbinds the UI control from the current editable field.
     * <p>
     * This method is called before binding to a new editable. Implementations should
     * unbind any bidirectional bindings. Subscriptions added via {@link #addEditableSubscription(Subscription)}
     * are automatically unsubscribed by this blueprint.
     */
    protected abstract void unbindControlFromEditable();

    /**
     * Updates the control's title/label.
     *
     * @param title the title to display
     */
    protected abstract void updateControlTitle(String title);

    /**
     * Registers a subscription to be automatically unsubscribed when the editable changes.
     *
     * @param subscription the subscription to track
     */
    protected final void addEditableSubscription(Subscription subscription) {
        editableSubscriptions.add(subscription);
    }

    /**
     * Unsubscribes all tracked editable subscriptions.
     */
    private void clearEditableSubscriptions() {
        editableSubscriptions.forEach(Subscription::unsubscribe);
        editableSubscriptions.clear();
    }

    /**
     * Returns the current editable field, if any.
     *
     * @return the current editable, or null
     */
    public final ObservableField.Editable<DT> getEditable() {
        return currentEditable;
    }

    /**
     * Sets a new editable field, unbinding from the previous one.
     * <p>
     * This is the primary entry point for connecting this area to an editable field
     * obtained from an {@code ObservableComposer} or similar source.
     *
     * @param editable the new editable field
     */
    public final void setEditable(ObservableField.Editable<DT> editable) {
        rebind(editable);
    }

    /**
     * Rebinds the UI control to a new editable field.
     * <p>
     * This method handles the full unbind/bind cycle:
     * <ol>
     *   <li>Unbind from the current editable (if any).</li>
     *   <li>Clear all editable subscriptions.</li>
     *   <li>Set the new editable reference.</li>
     *   <li>Bind to the new editable (if not null).</li>
     * </ol>
     *
     * @param newEditable the new editable field, or null to unbind
     */
    public final void rebind(ObservableField.Editable<DT> newEditable) {
        // Unbind from previous
        if (currentEditable != null) {
            unbindControlFromEditable();
            clearEditableSubscriptions();
        }

        currentEditable = newEditable;

        // Bind to new
        if (currentEditable != null) {
            bindControlToEditable(currentEditable);

            // Derive title from field definition
            String title = deriveTitle(currentEditable.getObservableFeature());
            updateControlTitle(title);
        }
    }

    /**
     * Derives a title from the observable field's definition.
     * <p>
     * Override this method to customize how the title is generated.
     *
     * @param observableField the observable field
     * @return the derived title
     */
    protected String deriveTitle(ObservableField<DT> observableField) {
        // Default implementation - subclasses may override to use view calculator
        // for resolving meaning nid to description text
        return observableField.featureKey().toString() + ":";
    }

    /**
     * Called when the feature property changes.
     * <p>
     * If the new feature is an {@code ObservableField}, this method sets up
     * an editable wrapper. Subclasses generally don't need to override this.
     */
    @Override
    protected void featureChanged(Feature<DT> oldFeature, Feature<DT> newFeature) {
        if (newFeature instanceof ObservableField<DT> observableField) {
            // For direct ObservableField binding (non-transactional)
            // Create a simple wrapper or use editableValueProperty directly
            // In a full implementation, you'd get this from ObservableComposer
            // For now, we'll just handle the case where an Editable is set directly
            LOG.debug("Feature changed to ObservableField: {}", observableField.featureKey());
        }
        // Note: In most cases, setEditable() will be called directly by the
        // orchestrating code rather than going through featureChanged()
    }

    @Override
    protected void subFeatureAreaBlueprintRestoreFromPreferencesOrDefault() {
        // Subclasses can override if they have additional preferences to restore
    }

    /**
     * Reverts any uncommitted changes in the editable field.
     */
    @Override
    protected void subAreaRevert() {
        if (currentEditable != null) {
            currentEditable.reset();
        }
    }

    /**
     * Saves are handled by the ObservableComposer transaction lifecycle.
     * This method is a no-op for editable field areas.
     */
    @Override
    protected void subAreaSave() {
        // Saving is handled by ObservableComposer.commit()
        // The editable's value is automatically synced via bidirectional binding
    }

    @Override
    public void knowledgeLayoutUnbind() {
        rebind(null); // Unbind and clear subscriptions
        super.knowledgeLayoutUnbind();
    }
}