package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.layout.*;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.layout.KlPeerable.PreferenceKeys;
import static dev.ikm.komet.layout.KlRestorable.camelCaseToWords;

/**
 * Abstract base class representing a gadget blueprint.
 * <p>
 * This class defines the foundational behavior and state for gadgets, including
 * preference management, initialization, and subscription handling. It is designed
 * to enable both the restoration of gadgets from previously stored preferences and
 * the creation of new gadgets from a factory.
 *
 * @param <FX> the type of Fx object (Node, Stage) managed or represented by the implementing gadget blueprint
 */
public sealed abstract class StateAndContextBlueprint<FX>
        implements KlStateCommands, KlContextSensitiveComponent, KlContextProvider
        permits AreaBlueprint /*, FxWindow,  RenderView */ {

    /**
     * Enum representing the various lifecycle states of a {@code StateAndContextBlueprint}.
     * These states define the different stages in the lifecycle of a blueprint instance
     * within the framework, particularly in relation to its initialization, restoration,
     * binding, and unbinding process.
     *
     * The possible states include:
     * - {@code INITIALIZED}: Indicates that the object has been created and initialized.
     * - {@code UNRESTORED}: Represents a state where the object has not yet been restored
     *   from its saved state or configuration, but has been initialized and is ready for restoration.
     * - {@code UNBOUND}: Denotes that the object is ready to be bound to its context (any restoration is complete).
     * - {@code BOUND}: Denotes that the object is fully bound to its context, and any
     *   necessary external dependencies or resources are available and operational.
     *
     * The lifecycle state transitions are governed by the operations defined within
     * the {@code StateAndContextBlueprint} class and its associated methods for subscribing,
     * unsubscribing, saving, reverting, and handling context changes.
     */
    public enum LifecycleState {
        INITIALIZED,
        UNRESTORED,
        UNBOUND,
        BOUND,
    }

    protected AtomicReference<LifecycleState> lifecycleState = new AtomicReference<>(LifecycleState.INITIALIZED);

    protected static final Logger LOG = LoggerFactory.getLogger(StateAndContextBlueprint.class);

    /**
     * The preferences associated with this {@code GadgetBlueprint} instance.
     * <p>
     * This field is used to store and manage configuration data required
     * by the {@code GadgetBlueprint}. The preferences enable synchronization
     * of state and behavior, and they are utilized for state restoration,
     * initialization, and handling preference changes.
     * <p>
     * {@code preferences} is immutable and is typically provided during object
     * construction. It forms the backbone for both state restoration and updates
     * triggered by preference changes.
     */
    private final KometPreferences preferences;
    /**
     * Holds an atomic reference to the current subscription for managing
     * changes or updates related to preferences associated with this gadget blueprint.
     * <p>
     * The `preferenceSubscriptionReference` is initialized with an empty subscription and can be
     * updated as new subscriptions are added. This ensures thread-safe handling of
     * preference-related notifications and updates. Subscriptions are automatically canceled when the
     * fxGadget is unassigned from a parent.
     */
    protected final AtomicReference<Subscription> preferenceSubscriptionReference = new AtomicReference<>(Subscription.EMPTY);

    /**
     * A thread-safe reference to manage the subscription associated with the current context.
     * This reference is used to hold and update the {@link Subscription} instance related
     * to the view context, allowing safe access and updates across multiple threads.
     * The initial value is set to {@link Subscription#EMPTY}.
     */
    protected final AtomicReference<Subscription> contextSubscriptionReference = new AtomicReference<>(Subscription.EMPTY);

    /**
     * Indicates whether the gadget blueprint has been modified.
     * <p>
     * This property is primarily used to track the state of the blueprint
     * in terms of changes. It is set to {@code true} when modifications
     * to preferences or internal state occur. This allows for monitoring
     * and handling updates related to the blueprint's configuration or behavior.
     */
    private final SimpleBooleanProperty changed = new SimpleBooleanProperty(false);

    /**
     * A protected, final instance variable representing a gadget of type T.
     * The specific type of T will be defined by the implementing class or subclass.
     * This variable is intended to represent a generic or custom gadget
     * that might be utilized in various functional contexts within the application.
     * Being declared as final, its reference cannot be changed after initialization.
     */
    private final FX fxObject;


    public StateAndContextBlueprint(KometPreferences preferences, FX fxObject) {
        this.preferences = preferences;
        this.fxObject = fxObject;
        this.lifecycleState.set(LifecycleState.UNRESTORED);
        setup();
    }

    public StateAndContextBlueprint(KlPreferencesFactory preferencesFactory, KlView.Factory viewFactory,
                                    FX fxObject) {
        Objects.requireNonNull(preferencesFactory, "preferencesFactory must not be null");
        if (viewFactory.getClass().getName().equals("dev.ikm.komet.layout.KlArea$Factory")) {
            throw new IllegalArgumentException("viewFactory must be instantiatable. Interface class provided: " + viewFactory.getClass().getName());
        }
        this.preferences = preferencesFactory.get();
        this.preferences.putUuid(PreferenceKeys.KL_OBJECT_ID, UUID.randomUUID());
        this.preferences.put(PreferenceKeys.FACTORY_CLASS_NAME, viewFactory.getClass().getName());
        this.preferences.put(PreferenceKeys.NAME_FOR_RESTORE, camelCaseToWords(viewFactory.getClass().getEnclosingClass().getName())  + " " + DateTimeUtil.timeNowSimple());
        this.preferences.putBoolean(PreferenceKeys.INITIALIZED, true);
        this.fxObject = fxObject;
        setup();
        this.lifecycleState.set(LifecycleState.UNBOUND);
        if (this instanceof KlArea<?> klArea) {
            AreaGridSettings areaGridSettings = AreaGridSettings.DEFAULT.withAreaFactoryClassName(viewFactory.getClass().getName());
            klArea.setAreaLayout(areaGridSettings);
        }
        try {
            preferences.sync();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void setup() {
        if (this instanceof KlView<?> klView) {
            klView.setFxPeer(fxObject);
        }
        preferences.addPreferenceChangeListener(evt -> changed.setValue(true));
        if (fxObject instanceof Node node) {
            node.parentProperty().subscribe(this::parentChanged);
        }
    }

    /**
     * Returns the current instance of klView cast from this object.
     *
     * @return the current instance cast as a klView.
     */
    public final KlView klView() {
        return (KlView) this;
    }

    /**
     * Retrieves an instance of KlObject by invoking the klView method.
     *
     * @return an instance of KlObject
     */
    public KlPeerable klObject() {
        return klView();
    }

    /**
     * Retrieves the unique identifier of the object.
     *
     * @return the UUID representing the unique identifier of the object.
     */
    public final UUID klObjectId() {
        return preferences.getUuid(PreferenceKeys.KL_OBJECT_ID).get();
    }

    /**
     * Handles changes in the parent object by performing the necessary operations
     * when the parent is updated or unset.
     *
     * @param oldParent the previous parent object before the change
     * @param newParent the new parent object after the change; may be null
     */
    private void parentChanged(Parent oldParent, Parent newParent) {
        if (oldParent != null && newParent == null) {
            this.forget();
        }
    }

    /**
     * Unsubscribes from the current subscription and removes associated preference data.
     * This method ensures that the subscription is properly terminated and that any
     * preferences stored in the backing store are deleted and flushed.
     *
     * Throws a RuntimeException if there is an error during the preference removal or flush process.
     */
    protected final void forget() {
        this.preferenceSubscriptionReference.get().unsubscribe();
        this.contextSubscriptionReference.get().unsubscribe();
        try {
            if (this instanceof KlView<?> klView) {
                klView.setFxPeer(null);
            }
            preferences.clear();
            preferences.removeNode();
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Signals that this instance of {@code klView} needs to unsubscribe from all  {@code KlContext} properties,
     * prior to {@code klView} deletion or reorganization. The specific behavior and implementation of this method
     * are left to the discretion of the implementing class. Calls to {@code klView.unsubscribeFromContext()}
     * must occur in a depth-first manner, staring with the top {@code klView} that will encapsulate all the
     * intended changes. It is not the responsibility of this method to provide the depth-first logic. That responsibility
     * is placed on the {@code KlContext} object which will notify subordinate {@code klView} of an impending change.
     */

    @Override
    public final void unsubscribeFromContext() {
        this.contextSubscriptionReference.getAndSet(Subscription.EMPTY).unsubscribe();
    }

    /**
     * Signals this {@code klView} instance to subscribe to any necessary {@code KlContext} properties
     * or events. This method ensures the gadget is actively synchronized with any relevant contextual
     * updates within the knowledge layout system. The specific subscription logic and its scope
     * are left to the implementing class.
     * <p>
     * It is the responsibility of the implementing class to define how and which properties or
     * events of the {@code KlContext} are subscribed to. This provides flexibility for the gadget
     * to interact with its contextual environment according to its requirements.
     */

    @Override
    public final void subscribeToContext() {
        contextSubscriptionReference.get().and(context().viewCoordinate().subscribe(this::contextChanged));
    }

    /**
     * Handles the event when the context of the application or component changes.
     * This method is typically invoked to perform the necessary updates or actions
     * in response to a change in the application's execution environment, user interface,
     * or other relevant context.
     *<p>
     * This implementation does not take any parameters or return a value. It
     * serves as a simple method that developers can override to take action when the context changes.
     */
    public void contextChanged() {
    }


    /**
     * Retrieves the JavaFx object associated with this {@code GadgetBlueprint} instance.
     *
     * @return the {@code fxObject}, representing the encapsulated Fx object
     *         associated with this blueprint.
     */
    public final FX fxObject() {
        return fxObject;
    }

    /**
     * Adds a subscription to preference-related updates for this gadget blueprint.
     *
     * @param preferenceSubscription the subscription to be added that represents
     *                                a listener or handler for preference-related changes
     */
    public final void addPreferenceSubscription(Subscription preferenceSubscription) {
        this.preferenceSubscriptionReference.set(this.preferenceSubscriptionReference.get().and(preferenceSubscription));
    }

    /**
     * Notifies that the preferences associated with this gadget blueprint have been changed.
     * This method updates the internal state to reflect that the blueprint has been modified
     * by setting the {@code changed} property to {@code true}.
     */
    public void preferencesChanged() {
        changed.set(true);
    }

    /**
     * Retrieves the property indicating whether the gadget blueprint has undergone changes.
     *
     * @return the {@code BooleanProperty} representing the change state of the gadget blueprint.
     */
    protected BooleanProperty changedProperty() {
        return changed;
    }

    /**
     * Retrieves the preferences associated with this GadgetBlueprint instance.
     *
     * @return the preferences configured for this gadget blueprint
     */
    public KometPreferences preferences() {
        return preferences;
    }

    /**
     * Deletes the preference node associated with this instance and ensures
     * that any changes are properly persisted to the backing store.
     *
     * This method performs the following operations:
     * 1. Removes the preference node tied to the current instance.
     * 2. Flushes the preferences to ensure changes are saved.
     *
     * If the operation fails due to a {@link BackingStoreException}, a runtime
     * exception is thrown to signal an error.
     *
     * @throws RuntimeException if the preferences cannot be removed or flushed
     *                          due to a backing store error
     */
    @Override
    public final void delete() {
        try {
            preferences().removeNode();
            preferences().flush();

        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the current state of the window and its preferences to persistent storage.
     *
     * This method iterates over all defined preference keys and updates the associated
     * preferences with the current state of the window properties such as opacity,
     * visibility, location, and size. It ensures that the updated preferences are
     * persisted by flushing the preferences to the backing store.
     *
     * If there are any additional stage-specific preferences to be saved, it delegates
     * that responsibility to the subStageSave method.
     *
     * Upon successful completion of the saving process, the changed property is reset
     * to indicate that there are no unsaved changes.
     *
     * Throws a RuntimeException if an error occurs while flushing preferences to the
     * backing store.
     */
    @Override
    public final void save() {
        try {
            subContextSave();
            preferences().flush();
            changedProperty().setValue(false);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Defines the custom save behavior for subclasses of {@code StateAndContextBlueprint}.
     *
     * This method is intended to be implemented by subclasses to handle the saving
     * of additional or subclass-specific state properties to preferences or
     * persistent storage. It is invoked during the {@code save} process to ensure
     * that all state associated with the gadget blueprint, including subclass-specific
     * properties, is saved correctly.
     *
     * Subclasses must override this method to provide the concrete logic for saving
     * any additional state that is not handled by the base class. This may include
     * saving data for custom components, properties, or configurations specific to
     * the subclass implementation.
     *
     * The implementation of this method should ensure that:
     * - Relevant state properties are captured and stored persistently.
     * - State consistency is maintained across the application's lifecycle.
     * - Data integrity and error handling are properly managed.
     *
     * This method is abstract to enforce that every subclass provides its own
     * implementation for persisting custom state.
     */
    protected abstract void subContextSave();

    /**
     * Reverts the current stage blueprint to its last saved state or default configuration.
     *
     * This method performs the following operations:
     * 1. Restores properties of the stage to their values from user preferences or defaults,
     *    ensuring that the stage location, size, visibility, and other attributes are
     *    reset accordingly.
     * 2. Triggers the `subStageRevert` method, which allows subclasses to implement specific
     *    revert logic for sub-stage components or additional properties.
     *
     * This method is typically invoked to undo changes made to the stage or to restore
     * its state to a consistent baseline, either due to user action or system requirements.
     *
     * The `revert` mechanism ensures that the stage and its subcomponents are aligned with
     * the user's preferences or default configuration.
     *
     * Note: Subclasses must implement {@link #subContextRevert()} to define custom
     * revert behavior for specific sub-stage properties or states.
     */
    @Override
    public final void revert() {
        subContextRevert();
    }
    /**
     * Defines the behavior for reverting specific state or properties
     * of a gadget blueprint subclass to its last saved state or default configuration.
     *
     * This method is intended to be implemented by subclasses to handle
     * the restoration of custom or additional state properties specific
     * to the gadget blueprint. It is called during the overall revert
     * process to ensure that all subclass-specific attributes and components
     * are appropriately restored.
     *
     * Subclasses must override this method to provide concrete logic
     * for reverting their custom state changes or additional properties,
     * ensuring alignment with the user's preferences or default values.
     *
     * This is part of the broader revert mechanism, which includes restoring:
     * - General gadget blueprint properties, such as size, location, visibility, etc.
     * - Subclass-specific properties defined by overriding this method.
     *
     * The method is abstract to enforce that subclasses provide a concrete
     * implementation for handling their unique revert requirements.
     */
    protected abstract void subContextRevert();

}
