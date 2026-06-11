package dev.ikm.komet.layout_engine.component.view;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.layout.*;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.layout.preferences.PreferencePropertyObject;
import dev.ikm.komet.layout.preferences.PreferencePropertyString;
import dev.ikm.komet.layout_engine.blueprint.StateAndContextBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import javafx.scene.Node;

import java.util.Optional;
import java.util.UUID;

public final class ViewContext implements KlContext, KlStateCommands {

    /**
     * Represents a preference property object bound to the `VIEW_COORDINATE` key
     * within the `ViewBlueprint` class. This object stores and manages the view
     * coordinate configuration for this instance, linking it to the associated
     * preferences to ensure synchronization between user-defined settings and
     * default values.
     * <p>     * The `viewCoordinate` property serves as a reactive binding to handle changes
     * in user preferences or system configurations dynamically. Designed as a
     * final immutable object, it provides encapsulated access to the underlying
     * `ViewCoordinateRecord`, ensuring both data consistency and thread safety
     * during interactions.
     * <p>     * This property is initialized during instance creation of the `ViewBlueprint`
     * class, either with user preferences or default values, and is updated
     * as needed through reactive subscriptions implemented in the class methods.
     * Changes to this property may trigger dependent operations such as view
     * recalculations or layout updates.
     */
    private final PreferencePropertyObject<ViewCoordinateRecord> viewCoordinateProperty;

    /**
     * Represents a property containing the context name as a string in the {@code Context} class.
     * It is a constant preference property used to manage and persist the context name.
     * <p>     * This property may be used for associating a specific name or identifier
     * with a given context instance, enabling better traceability and management
     * of multiple contexts in an application.
     * <p>     * The property is declared as final to ensure immutability and safeguard its association
     * with the context instance and preferences it represents.
     */
    private final PreferencePropertyString contextNameProperty;

    /**
     * Represents a preference property for storing the UUID of the context as a string.
     * This property is immutable and serves as an identifier for the context.
     * It is primarily used for serialization, storage, and referencing purposes
     * where the context's unique UUID is required in string format.
     */
    private final PreferencePropertyString contextUuidStringProperty;

    final KometPreferences preferences;
    final ObservableViewNoOverride observableView;
    final PublicIdStringKey publicIdStringKey;
    final KlPeerable klPeerable;

    // create
    protected ViewContext(KlContextProvider contextProvider, ViewCoordinateRecord viewCoordinateRecord,
                          PublicIdStringKey publicIdStringKey) {
        this.preferences = contextProvider.klObject().preferences();
        this.klPeerable = contextProvider.klObject();
        this.viewCoordinateProperty =
                PreferencePropertyObject.objectProp(this.klPeerable, PreferenceKeys.VIEW_COORDINATE);
        this.contextUuidStringProperty =
                PreferencePropertyObject.stringProp(this.klPeerable, PreferenceKeys.CONTEXT_UUID);
        this.contextNameProperty =
                PreferencePropertyObject.stringProp(this.klPeerable, PreferenceKeys.CONTEXT_NAME);

        this.observableView = new ObservableViewNoOverride(viewCoordinateRecord, publicIdStringKey.getString());
        this.publicIdStringKey = publicIdStringKey;

        this.klPeerable.properties().put(KlPeerable.PropertyKeys.KL_CONTEXT, this);
    }

    // create wrapping a provided live source view, rather than building one from a record. The window
    // owns this coordinate and derives its (legacy) ViewProperties from the same ObservableView, so
    // there is one coordinate of record, not two — the basis for the authority-first inner-window
    // unification (ike-issues#660).
    protected ViewContext(KlContextProvider contextProvider, ObservableViewNoOverride sourceView,
                          PublicIdStringKey publicIdStringKey) {
        this.preferences = contextProvider.klObject().preferences();
        this.klPeerable = contextProvider.klObject();
        this.viewCoordinateProperty =
                PreferencePropertyObject.objectProp(this.klPeerable, PreferenceKeys.VIEW_COORDINATE);
        this.contextUuidStringProperty =
                PreferencePropertyObject.stringProp(this.klPeerable, PreferenceKeys.CONTEXT_UUID);
        this.contextNameProperty =
                PreferencePropertyObject.stringProp(this.klPeerable, PreferenceKeys.CONTEXT_NAME);

        this.observableView = sourceView;
        this.publicIdStringKey = publicIdStringKey;

        this.klPeerable.properties().put(KlPeerable.PropertyKeys.KL_CONTEXT, this);
    }

    // create default
    protected ViewContext(KlContextProvider contextProvider) {
        PublicId publicId = PublicIds.of(UUID.randomUUID());
        PublicIdStringKey publicIdStringKey = new PublicIdStringKey(publicId, "Context for " + contextProvider.getClass().getSimpleName());
        this(contextProvider, Coordinates.View.DefaultView(), publicIdStringKey);
    }

    // restore
    protected ViewContext(KometPreferences preferences, KlContextProvider contextProvider) {
        this.preferences = preferences;
        this.klPeerable = contextProvider.klObject();
        this.viewCoordinateProperty =
                PreferencePropertyObject.objectProp(this.klPeerable, PreferenceKeys.VIEW_COORDINATE);
        this.contextNameProperty = PreferencePropertyObject.stringProp(this.klPeerable, PreferenceKeys.CONTEXT_NAME);
        this.contextUuidStringProperty = PreferencePropertyString.stringProp(this.klPeerable, PreferenceKeys.CONTEXT_UUID);

        String contextName = preferences.get(PreferenceKeys.CONTEXT_NAME, (String) PreferenceKeys.CONTEXT_NAME.defaultValue());
        UUID contextUuid = preferences.getUuid(PreferenceKeys.CONTEXT_UUID, (UUID) PreferenceKeys.CONTEXT_UUID.defaultValue());
        this.publicIdStringKey = new PublicIdStringKey(PublicIds.of(contextUuid), contextName);
        ViewCoordinateRecord viewCoordinateRecord = preferences.getObject(PreferenceKeys.VIEW_COORDINATE, (ViewCoordinateRecord) PreferenceKeys.VIEW_COORDINATE.defaultValue());
        this.observableView = new ObservableViewNoOverride(viewCoordinateRecord, contextName);

        this.klPeerable.properties().put(KlPeerable.PropertyKeys.KL_CONTEXT, this);

    }

    @Override
    public KlPeerable klPeer() {
        return klPeerable;
    }

    public PreferencePropertyObject<ViewCoordinateRecord> viewCoordinatePropertyProperty() {
        return viewCoordinateProperty;
    }

    public PreferencePropertyString contextNameProperty() {
        return contextNameProperty;
    }

    public PreferencePropertyString contextUuidStringProperty() {
        return contextUuidStringProperty;
    }

    @Override
    public PublicIdStringKey<KlContext> contextId() {
        return publicIdStringKey;
    }

    @Override
    public Optional<Node> graphic() {
        return Optional.empty();
    }

    @Override
    public ObservableView viewCoordinate() {
        return observableView;
    }

    public static ViewContext create(KlContextProvider contextProvider, ViewCoordinateRecord viewCoordinateRecord, UUID contextId, String contextName) {
        PublicIdStringKey<KlContext> publicIdStringKey = new PublicIdStringKey<>(PublicIds.of(contextId), contextName);
        return new ViewContext(contextProvider, viewCoordinateRecord, publicIdStringKey);
    }

    public static ViewContext create(KlContextProvider contextProvider, ViewCoordinateRecord viewCoordinateRecord, String contextName) {
        PublicIdStringKey<KlContext> publicIdStringKey = new PublicIdStringKey<>(PublicIds.newRandom(), contextName);
        return new ViewContext(contextProvider, viewCoordinateRecord, publicIdStringKey);
    }

    /**
     * Creates a context that wraps a provided live source view, making that view the provider's single
     * coordinate of record. The provider derives its (legacy) {@link dev.ikm.komet.framework.view.ViewProperties}
     * from the same {@code ObservableView}, so menu edits, the derived view, and KL areas all read one
     * coordinate. The context attaches itself as the {@code KL_CONTEXT} on the provider's node.
     *
     * @param contextProvider the window providing this context
     * @param sourceView      the live coordinate source to expose
     * @param contextName     a display name for the context
     * @return the created {@code ViewContext}
     */
    public static ViewContext createOverView(KlContextProvider contextProvider,
                                             ObservableViewNoOverride sourceView, String contextName) {
        PublicIdStringKey<KlContext> publicIdStringKey = new PublicIdStringKey<>(PublicIds.newRandom(), contextName);
        return new ViewContext(contextProvider, sourceView, publicIdStringKey);
    }

    public static ViewContext restore(KometPreferences preferences, KlContextProvider contextProvider) {
        return new ViewContext(preferences, contextProvider);
    }

    @Override
    public void unsubscribeDependentContexts() {
        switch (this.klPeerable) {
            case KlView<?> klView -> klView.dfsProcessKlView(KlContextSensitiveComponent::unsubscribeFromContext);
            case KlKnowledgeBaseContext klKnowledgeBaseContext -> {
                // No action specified at this time for non-KlGadget klObjects.
                // TODO: Decide if this should propagate to windows and other KL Objects.
            }
        }
    }

    @Override
    public void subscribeDependentContexts() {
        switch (this.klPeerable) {
            case KlView<?> klView -> klView.dfsProcessKlView(KlContextSensitiveComponent::subscribeToContext);
            case KlKnowledgeBaseContext klKnowledgeBaseContext -> {
                // No action specified at this time for non-KlGadget klObjects.
                // TODO: Decide if this should propagate to windows and other KL Objects.
            }
        }
    }

    @Override
    public void save() {
        for (PreferenceKeys key : PreferenceKeys.values()) {
            switch (key) {
                case CONTEXT_NAME -> preferences.put(key, this.contextNameProperty.getValue());
                case CONTEXT_UUID -> preferences.put(key, this.contextUuidStringProperty.getValue());
                case VIEW_COORDINATE -> preferences.putObject(key, viewCoordinateProperty.getValue());
            }
        }
    }

    @Override
    public void revert() {
        for (PreferenceKeys key : PreferenceKeys.values()) {
            switch (key) {
                case CONTEXT_NAME ->
                        this.contextNameProperty.setValue(preferences.get(key, (String) key.defaultValue()));
                case CONTEXT_UUID ->
                        this.contextUuidStringProperty.setValue(preferences.get(key, (String) key.defaultValue()));
                case VIEW_COORDINATE ->
                        this.viewCoordinateProperty.setValue(preferences.getObject(key, (ViewCoordinateRecord) key.defaultValue()));
            }
        }
    }

    @Override
    public void delete() {
        // This class is encapsulated and uses the preferences of the encapsulator. The encapsulator is
        // responsible for deletion of the preference node.
        for (PreferenceKeys key : PreferenceKeys.values()) {
            preferences.remove(key);
        }
    }

    public void subscribeToChanges() {
        if (this.klPeerable instanceof StateAndContextBlueprint stateAndContextBlueprint) {
            for (PreferenceKeys key : PreferenceKeys.values()) {
                stateAndContextBlueprint.addPreferenceSubscription(switch (key) {
                    case CONTEXT_NAME ->
                            contextNameProperty.subscribe(() -> { /* TODO: placeholder for something to do? */ }).and(
                                    contextNameProperty.subscribe(stateAndContextBlueprint::preferencesChanged)
                            );
                    case CONTEXT_UUID ->
                            contextUuidStringProperty.subscribe(() -> { /* TODO: placeholder for something to do? */ }).and(
                                    contextUuidStringProperty.subscribe(stateAndContextBlueprint::preferencesChanged)
                            );
                    case VIEW_COORDINATE ->
                            viewCoordinateProperty.subscribe(() -> observableView.setExceptOverrides(viewCoordinateProperty.getValue())).and(
                                    viewCoordinateProperty.subscribe(stateAndContextBlueprint::preferencesChanged));
                });
            }
        } else {
        }
    }


}
