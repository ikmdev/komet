package dev.ikm.komet.layout_engine.component.window;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlPeerToRegion;
import dev.ikm.komet.layout.KlPeerable;
import dev.ikm.komet.layout.KlStateCommands;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.*;
import dev.ikm.komet.layout.window.KlFxWindow;
import dev.ikm.komet.layout.window.KlRenderView;
import dev.ikm.komet.layout_engine.blueprint.StateAndContextBlueprint;
import dev.ikm.komet.layout_engine.component.area.SupplementalTestArea;
import dev.ikm.komet.layout_engine.component.menu.ViewContextMenuButtonArea;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.eclipse.collections.api.list.ImmutableList;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.layout.window.KlFxWindow.PreferenceKeys.*;
import static dev.ikm.orchestration.provider.knowledge.layout.gadget.blueprint.StateAndContextBlueprint.LOG;

/**
 * The StageBlueprint class is a concrete implementation derived from GadgetBlueprint<Stage>
 * and KlFxWindow. It serves as a blueprint for defining, configuring, and synchronizing
 * stage-specific properties such as location, size, visibility, opacity, and other
 * related settings with user preferences. This class ensures consistency between the
 * application's state and the user's configured preferences for the stage window.
 * <p>
 * Key functionalities of this class include:
 * - Initializing properties based on user preferences or default values.
 * - Restoring the state of the window from saved preferences.
 * - Subscribing to changes and establishing bidirectional synchronization
 * between preferences and window properties.
 * - Allowing further subclass-specific initialization with the classInitialize method.
 * <p>
 * Fields:
 * - LOG: Logger instance for handling debugging, error reporting, or informational messages.
 * - locationX: The X-coordinate of the stage window's position on the screen.
 * - locationY: The Y-coordinate of the stage window's position on the screen.
 * - width: The width of the stage window in pixels.
 * - height: The height of the stage window in pixels.
 * - opacity: The transparency level of the stage window.
 * - visible: A boolean indicating whether the stage window is visible or hidden.
 * - windowStage: The actual Stage object associated with this blueprint.
 * <p>
 * Interfaces/Inheritance:
 * - Extends GadgetBlueprint<Stage>: Provides fundamental blueprinting behavior
 * and manages preference-related configurations.
 * - Extends KlFxWindow: Adds window-specific functionality to manage stage-related
 * properties such as location, size, and visibility.
 * <p>
 * This class forms the foundational implementation for creating and managing
 * customizable and synchronized stage layouts using user-defined settings.
 */
public final class FxWindow
        extends StateAndContextBlueprint<Stage>
        implements KlFxWindow {

    /**
     * Represents the X-coordinate of the stage window's location. This property is
     * synchronized with user preferences and maintains the horizontal position of
     * the window on the screen.
     * <p>
     * The `locationX` property retrieves its initial value from the user preferences
     * store during the setup process of the {@code StageBlueprint} class. If no
     * value is stored, a default value is applied. Any changes to this property
     * are automatically synchronized back to the preferences to maintain consistency.
     * <p>
     * This property is primarily used to ensure that the window reopens at the
     * same horizontal position it was at when last used.
     */
    private final PreferencePropertyDouble locationX = PreferencePropertyDouble.doubleProp(klView(), WINDOW_X_LOCATION);
    /**
     * Represents the Y-coordinate of the window's position in the user interface.
     * This property is tied to user preferences, allowing for persistent storage
     * and retrieval of the window's vertical position.
     * <p>
     * The value is stored as a double and synchronized with the user's preferences
     * through the {@link PreferencePropertyDouble} mechanism. This ensures that
     * changes to the Y-coordinate are reflected in both the application state and
     * the stored user preferences, providing consistency across application sessions.
     * <p>
     * The property is initialized with the preference key {@code WINDOW_Y_LOCATION},
     * which identifies the stored value in the preference infrastructure.
     * <p>
     * This field is immutable and is finalized to prevent reassignment.
     */
    private final PreferencePropertyDouble locationY = PreferencePropertyDouble.doubleProp(klView(), WINDOW_Y_LOCATION);
    /**
     * Represents the width of the stage window, managed as a preference-backed property.
     * This property is synchronized with the user's stored preferences, allowing for
     * restoration of the stage's width when the application is initialized.
     * <p>
     * The value is stored as a double precision floating-point number and reflects the
     * current width of the window in pixels. Changes to this property are automatically
     * persisted to the preferences system, ensuring that the preferred width is retained
     * across application sessions.
     * <p>
     * This property also allows bidirectional synchronization with the stage's actual width,
     * so updates to either the stage or the preferences will keep them in sync.
     * <p>
     * The field is declared as final to enforce immutability, ensuring the property itself
     * cannot be reassigned after initialization.
     */
    private final PreferencePropertyDouble width = PreferencePropertyDouble.doubleProp(klView(), WINDOW_WIDTH);
    /**
     * Represents the height property of the stage window in the `StageBlueprint` class.
     * This property is synchronized with the user preferences to persist the height value of the window.
     * <p>
     * The value is stored as a double-precision floating-point number through
     * the `PreferencePropertyDouble` abstraction, which provides functionality
     * for binding and bidirectional synchronization.
     * <p>
     * Key characteristics:
     * - The property's default or restored value is derived from the user preferences.
     * - Changes to this property are reflected in user preferences, and vice versa.
     * - This ensures that the height of the stage window respects user settings
     * and persists across application sessions.
     */
    private final PreferencePropertyDouble height = PreferencePropertyDouble.doubleProp(klView(), WINDOW_HEIGHT);
    /**
     * Represents the opacity level of the stage window as a user preference.
     * This property allows the storage, retrieval, and synchronization of the
     * stage's transparency setting with user preferences.
     * <p>
     * The opacity value is managed using the `PreferencePropertyDouble` class,
     * which provides mechanisms for interacting with the stored preference and
     * synchronizing changes between the application state and the preferences
     * system. The variable is initialized with the associated key `OPACITY`
     * to uniquely identify this property in the preferences store.
     * <p>
     * This property is used in the context of a StageBlueprint to restore,
     * persist, and synchronize the opacity level of the stage window, ensuring
     * consistency between the user's preferences and the window's appearance.
     */
    private final PreferencePropertyDouble opacity = PreferencePropertyDouble.doubleProp(klView(), WINDOW_OPACITY);
    /**
     * Represents a boolean property for controlling the visibility state of the stage window.
     * This property is used to synchronize and store the visibility preference of the stage
     * between the application's runtime state and the user's preference storage.
     * <p>
     * The visibility state is managed as a preference property, ensuring bidirectional
     * synchronization such that changes in the application's window visibility setting are
     * reflected in stored preferences, and vice versa. This integration allows the application
     * to restore the user's preferred visibility state across sessions.
     * <p>
     * The property is declared as final, emphasizing that it is a constant member of the
     * containing class and its reference cannot be reassigned.
     * <p>
     * Usage Context:
     * - Automatically updated when the user toggles the visibility of the stage.
     * - Used during the initialization to restore the visibility state from user preferences
     * or to set its default value if no preference is available.
     */
    private final PreferencePropertyBoolean visible = PreferencePropertyBoolean.booleanProp(klView(), WINDOW_VISIBLE);

    /**
     * Represents the preference property for the title of a window.
     * This property is a string type and is finalized to ensure immutability.
     * It links to the preference system for storing and retrieving the window title.
     */
    private final PreferencePropertyString title = PreferencePropertyString.stringProp(klView(), WINDOW_TITLE);

    KlRenderView klRenderView;

    /**
     * Constructs a StageBlueprint instance and initializes the window
     * stage properties based on user preferences. Additionally, it sets
     * up functionality for maintaining synchronization between state
     * changes and user preferences.
     *
     * @param preferences the user preferences object used to store and retrieve
     *                    properties related to the stage window, such as location,
     *                    size, visibility, opacity, and other relevant settings.
     */
    private FxWindow(KometPreferences preferences) {
        super(preferences, new Stage());
        fxObject().setScene(new Scene(new Label("Please fix unset default root node")));
        finishSetup();
    }

    private FxWindow(KlPreferencesFactory preferencesFactory, KlFxWindow.Factory windowFactory) {
        super(preferencesFactory, windowFactory, new Stage());
        fxObject().setScene(new Scene(new Label("Please fix unset default root node")));
        this.windowStage().setTitle(windowFactory.productName() + " " + DateTimeUtil.nowWithZoneCompact());
        finishSetup();
    }

    private void finishSetup() {
        subscribeToChanges();
        restoreFromPreferencesOrDefaults();
        windowStage().setOnCloseRequest(this::onCloseRequest);
    }

    /**
     * Represents the primary {@link Stage} instance used as the window stage
     * within the {@code StageBlueprint} class. This stage defines properties
     * such as its size, location, visibility, transparency, and behavior,
     * which are synchronized with user preferences for consistency and state
     * restoration.
     * <p>
     * The {@code windowStage} is initialized upon {@code StageBlueprint}
     * instantiation and serves as the core UI window that reflects changes
     * made to its properties from user interactions or preference updates.
     * Modifications to the properties of this stage, such as position or
     * visibility, are persisted and restored across sessions based on the
     * synchronized user preferences.
     * <p>
     * Responsibilities and interactions:
     * - It is managed and configured by methods like {@code restoreFromPreferencesOrDefaults()}
     * to apply initial property settings.
     * - Changes to the stage properties are tracked and synchronized using
     * {@code subscribeToChanges()}, ensuring that updates in user preferences
     * and application state remain consistent.
     * - It is foundational for configuring the visual and functional aspects
     * of the user interface provided by the blueprint.
     * <p>
     * This stage remains final to ensure that its core responsibilities and
     * behavior are not altered, maintaining a consistent interface and ensuring
     * stability across applications using this blueprint.
     */
    public Stage windowStage() {
        return fxObject();
    }


    @Override
    public void setKlRenderView(KlRenderView renderView) {
        this.klRenderView = renderView;
        fxObject().setScene(renderView.fxObject());
    }

    @Override
    public KlRenderView getKlRenderView() {
        return this.klRenderView;
    }

    /**
     * Restores the state of window properties (such as opacity, visibility, location, width, and height)
     * from user preferences if available, or default values if preferences are not set.
     * <p>
     * This method iterates through each key defined in {@link KlFxWindow.PreferenceKeys}
     * and assigns the corresponding value from the preferences store or the default value
     * associated with the key to the stage-related properties.
     * <p>
     * Preference keys and their purpose:
     * - OPACITY: Represents the transparency level of the window.
     * - VISIBLE: Determines whether the window is visible or hidden.
     * - WINDOW_X_LOCATION: Refers to the X-coordinate of the window's position.
     * - WINDOW_Y_LOCATION: Refers to the Y-coordinate of the window's position.
     * - WINDOW_WIDTH: The width of the window in pixels.
     * - WINDOW_HEIGHT: The height of the window in pixels.
     * <p>
     * For each key, the method retrieves its stored value using the preferences API.
     * If no stored value exists for the key, the key's default value is used instead.
     * <p>
     * This method is typically invoked during initialization to restore the user's
     * previously saved preferences or initialize with default settings.
     */
    public void restoreFromPreferencesOrDefaults() {
        StateAndContextBlueprint.LOG.info("Restoring from preferences or defaults for {}", this.getClass().getSimpleName());
        for (KlFxWindow.PreferenceKeys key : KlFxWindow.PreferenceKeys.values()) {
            switch (key) {
                case WINDOW_OPACITY -> opacity.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case WINDOW_VISIBLE -> visible.setValue(preferences().getBoolean(key, (Boolean) key.defaultValue()));
                case WINDOW_X_LOCATION -> locationX.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case WINDOW_Y_LOCATION -> locationY.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case WINDOW_WIDTH -> width.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case WINDOW_HEIGHT -> height.setValue(preferences().getDouble(key, (Double) key.defaultValue()));
                case WINDOW_TITLE -> title.setValue(preferences().get(key, (String) key.defaultValue()));
            }
        }
    }

    /**
     * Subscribes to changes in specific window preference properties and sets up
     * bidirectional synchronization between the application state and window stage
     * properties such as opacity, visibility, location, width, and height.
     * <p>
     * This method iterates through the defined {@link KlFxWindow.PreferenceKeys} enums
     * and establishes subscriptions for each key. These subscriptions ensure that
     * any changes to the preferences are reflected in the corresponding window stage
     * properties and vice versa. It additionally links each subscription to a
     * handler for recording preference changes.
     * <p>
     * The following preference keys are supported:
     * - OPACITY: Synchronizes stage opacity.
     * - VISIBLE: Synchronizes stage visibility (show or hide).
     * - WINDOW_X_LOCATION: Synchronizes X coordinate of the stage.
     * - WINDOW_Y_LOCATION: Synchronizes Y coordinate of the stage.
     * - WINDOW_WIDTH: Synchronizes the width of the stage.
     * - WINDOW_HEIGHT: Synchronizes the height of the stage.
     * <p>
     * For each key, a set of subscriptions are created:
     * - One to listen for changes in preferences and apply them to the stage properties.
     * - One to listen for changes in the stage properties and update the preference values.
     * - One to monitor changes and trigger the `preferencesChanged` handler.
     * <p>
     * Note: This method currently raises questions regarding the use of bidirectional
     * bindings versus separate subscriptions, particularly for the OPACITY property.
     * Further discussion and decisions on this matter may be required for consistency.
     */
    private void subscribeToChanges() {
        for (KlFxWindow.PreferenceKeys key : KlFxWindow.PreferenceKeys.values()) {
            addPreferenceSubscription(switch (key) {
                case WINDOW_OPACITY -> {
                    // TODO: Discuss with team... Subscription vs bidirectional binding when available?
                    // bindBidirectional changes the ceremony to unsubscribe, and perhaps introduces inconsistency?
                    // windowStage.opacityProperty().bindBidirectional(opacity);
                    addPreferenceSubscription(opacity.subscribe(v -> windowStage().setOpacity(v.doubleValue())));
                    addPreferenceSubscription(windowStage().opacityProperty().subscribe(v -> opacity.setValue(v.doubleValue())));
                    yield opacity.subscribe(this::preferencesChanged);
                }
                case WINDOW_VISIBLE -> {
                    addPreferenceSubscription(windowStage().showingProperty().subscribe(visible::setValue));
                    addPreferenceSubscription(visible.subscribe(() -> {
                        if (visible.getValue()) {
                            windowStage().show();
                        } else {
                            windowStage().hide();
                        }
                    }));
                    yield visible.subscribe(this::preferencesChanged);
                }
                case WINDOW_X_LOCATION -> {
                    addPreferenceSubscription(locationX.subscribe(x -> windowStage().setX(x.doubleValue())));
                    addPreferenceSubscription(windowStage().xProperty().subscribe(x -> locationX.setValue(x.doubleValue())));
                    yield locationX.subscribe(this::preferencesChanged);
                }
                case WINDOW_Y_LOCATION -> {
                    addPreferenceSubscription(locationY.subscribe(y -> windowStage().setY(y.doubleValue())));
                    addPreferenceSubscription(windowStage().yProperty().subscribe(y -> locationY.setValue(y.doubleValue())));
                    yield locationY.subscribe(this::preferencesChanged);
                }
                case WINDOW_WIDTH -> {
                    addPreferenceSubscription(width.subscribe(w -> windowStage().setWidth(w.doubleValue())));
                    addPreferenceSubscription(windowStage().widthProperty().subscribe(w -> width.setValue(w.doubleValue())));
                    yield width.subscribe(this::preferencesChanged);
                }
                case WINDOW_HEIGHT -> {
                    addPreferenceSubscription(height.subscribe(h -> windowStage().setHeight(h.doubleValue())));
                    addPreferenceSubscription(windowStage().heightProperty().subscribe(h -> height.setValue(h.doubleValue())));
                    yield height.subscribe(this::preferencesChanged);
                }
                case WINDOW_TITLE -> {
                    addPreferenceSubscription(title.subscribe(t -> windowStage().setTitle(t)));
                    addPreferenceSubscription(windowStage().titleProperty().subscribe(t -> title.setValue(t)));
                    yield title.subscribe(this::preferencesChanged);
                }
            });
        }
    }

    /**
     * Provides access to the X-coordinate location property of the stage.
     * This property represents the horizontal position of the stage and
     * is synchronized with user preferences.
     *
     * @return the X-coordinate location preference property associated
     * with the stage
     */
    public PreferencePropertyDouble locationXProperty() {
        return locationX;
    }

    /**
     * Provides access to the Y-coordinate location property of the stage.
     * This property represents the vertical position of the stage and
     * is synchronized with user preferences.
     *
     * @return the Y-coordinate location preference property associated
     * with the stage
     */
    public PreferencePropertyDouble locationYProperty() {
        return locationY;
    }

    /**
     * Provides access to the width property of the stage. This property represents
     * the width of the stage in pixels and is synchronized with user preferences.
     *
     * @return the width preference property associated with the stage
     */
    public PreferencePropertyDouble widthProperty() {
        return width;
    }

    /**
     * Provides access to the height property of the stage. This property represents
     * the height of the stage in pixels and is synchronized with user preferences.
     *
     * @return the height preference property associated with the stage
     */
    public PreferencePropertyDouble heightProperty() {
        return height;
    }

    /**
     * Provides access to the opacity property of the stage. This property represents
     * the transparency level of the stage and is synchronized with user preferences.
     *
     * @return the opacity preference property associated with the stage
     */
    public PreferencePropertyDouble opacityProperty() {
        return opacity;
    }

    /**
     * Provides access to the visibility property of the stage. This property
     * represents whether the stage should be visible or hidden and is
     * synchronized with user preferences.
     *
     * @return the visibility preference property associated with the stage
     */
    public PreferencePropertyBoolean visibleProperty() {
        return visible;
    }

    /**
     * Gets the property representing the title of the Stage.
     *
     * @return the title property of type PreferencePropertyString
     */
    public PreferencePropertyString titleProperty() {
        return title;
    }

    @Override
    public Parent root() {
        return fxObject().getScene().getRoot();
    }

    @Override
    public void show() {
        fxObject().show();
    }

    @Override
    public void hide() {
        fxObject().hide();
    }

    /**
     * Saves the current preferences subtree as a shared layout using the specified layout name.
     *
     * @param layoutName the name of the layout to save the preferences subtree under
     */
    @Override
    public void saveAsLayout(String layoutName) {
        try {
            preferences().copyThisSubtreeTo(KlProfiles.sharedLayoutPreferences(), true);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the current state of the window and its preferences to persistent storage.
     * <p>
     * This method iterates over all defined preference keys and updates the associated
     * preferences with the current state of the window properties such as opacity,
     * visibility, location, and size. It ensures that the updated preferences are
     * persisted by flushing the preferences to the backing store.
     * <p>
     * If there are any additional stage-specific preferences to be saved, it delegates
     * that responsibility to the subStageSave method.
     * <p>
     * Upon successful completion of the saving process, the changed property is reset
     * to indicate that there are no unsaved changes.
     * <p>
     * Throws a RuntimeException if an error occurs while flushing preferences to the
     * backing store.
     */
    @Override
    public final void subContextSave() {
        for (KlFxWindow.PreferenceKeys key : KlFxWindow.PreferenceKeys.values()) {
            switch (key) {
                case WINDOW_OPACITY -> preferences().putDouble(key, opacity.doubleValue());
                case WINDOW_VISIBLE -> preferences().putBoolean(key, visible.getValue());
                case WINDOW_X_LOCATION -> preferences().putDouble(key, locationX.doubleValue());
                case WINDOW_Y_LOCATION -> preferences().putDouble(key, locationY.doubleValue());
                case WINDOW_WIDTH -> preferences().putDouble(key, width.doubleValue());
                case WINDOW_HEIGHT -> preferences().putDouble(key, height.doubleValue());
                case WINDOW_TITLE -> preferences().put(key, title.getValue());
            }
        }
    }

    /**
     * Reverts the current stage blueprint to its last saved state or default configuration.
     * <p>
     * This method performs the following operations:
     * 1. Restores properties of the stage to their values from user preferences or defaults,
     * ensuring that the stage location, size, visibility, and other attributes are
     * reset accordingly.
     * 2. Triggers the `subStageRevert` method, which allows subclasses to implement specific
     * revert logic for sub-stage components or additional properties.
     * <p>
     * This method is typically invoked to undo changes made to the stage or to restore
     * its state to a consistent baseline, either due to user action or system requirements.
     * <p>
     * The `revert` mechanism ensures that the stage and its subcomponents are aligned with
     * the user's preferences or default configuration.
     *
     */
    @Override
    public void subContextRevert() {
        restoreFromPreferencesOrDefaults();
    }

    /**
     * Handles the close request of the window by presenting the user with a choice dialog
     * to determine how to proceed based on the current state of the window. If there are
     * unsaved changes, users are presented with multiple options such as canceling the
     * close action, saving the current state, deleting the window, or reverting changes.
     * <p>
     * The method ensures that user preferences and the state of the window are updated
     * or reverted based on the selected action. If an unexpected error occurs during
     * preference updates, an exception is thrown.
     *
     * @param windowEvent the {@link WindowEvent} associated with the window close request.
     *                    This event may be consumed to cancel the close action under certain conditions.
     */
    protected void onCloseRequest(WindowEvent windowEvent) {
        try {
            if (changedProperty().getValue()) {
                ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Delete window", "Cancel",
                        "Delete window", "Save window and close", "Save window and keep open", "Save as layout", "Revert window");
                choiceDialog.initOwner(this.windowStage());
                choiceDialog.showAndWait();
                switch (choiceDialog.getResult()) {
                    case null -> windowEvent.consume();
                    case "Cancel" -> windowEvent.consume();
                    case "Delete window" -> delete();
                    case "Save window and close" -> findKlStateCommandPeers().forEach(KlStateCommands::save);
                    case "Save window and keep open" -> {
                        findKlStateCommandPeers().forEach(KlStateCommands::save);
                        windowEvent.consume();
                    }
                    case "Save as layout" -> {
                        DialogState dialogState;
                        do {
                            dialogState = showSaveDialog(this.windowStage());
                        } while (dialogState != DialogState.COMPLETE);
                        windowEvent.consume();
                    }
                    case "Revert window" -> findKlStateCommandPeers().forEach(KlStateCommands::revert);
                    default -> KlPeerToRegion.LOG.error("Unexpected choice dialog result: {}", choiceDialog.getResult());
                }
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    enum DialogState {
        COMPLETE, ERROR
    }

    public DialogState showSaveDialog(Stage owner) throws BackingStoreException {
        DirectoryChooser prefFolderChooser = new DirectoryChooser();
        prefFolderChooser.setTitle("Create and Select Folder for Window Settings");
        // Optionally set an initial directory or file name:
        KometPreferences userWindowNode = KlProfiles.userWindowPreferences("kec");

        Path userWindowNodePath = Path.of(userWindowNode.absolutePath());
        userWindowNodePath.toFile().mkdirs();
        prefFolderChooser.setInitialDirectory(userWindowNodePath.toFile());
        File newDirectory = prefFolderChooser.showDialog(owner);
        if (newDirectory == null) {
            return DialogState.COMPLETE;
        }
        if (newDirectory.list().length > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Directory is not empty");
            alert.setContentText("The selected directory contains files or folders. "
                    + "Choosing a non-empty directory may overwrite or mix with existing files.");
            alert.showAndWait();
            return DialogState.ERROR;
        } else {
            save();
            dfsProcessKlView(klView -> klView.save());
            preferences().sync();

            Path diff = userWindowNodePath.relativize(newDirectory.toPath());

            preferences().copyThisSubtreeTo(userWindowNode.node(diff.toString()), true);
            preferences().flush();
            return DialogState.COMPLETE;
        }
    }

    /**
     * Finds and collects instances of {@link KlStateCommands} within the JavaFX hierarchy associated with the
     * current context. The method identifies eligible peers based on specific criteria and returns them as
     * an immutable list.
     *
     * @return an immutable list of {@link KlStateCommands} objects that meet the specified criteria.
     */
    ImmutableList<KlStateCommands> findKlStateCommandPeers() {
        return findPeers(object -> {
            if (object != null && object instanceof KlStateCommands commandsPeer) {
                return Optional.of(commandsPeer);
            }
            return Optional.empty();
        });
    }

    public static FxWindow restore(KometPreferences preferences) {
        return factory().restore(preferences);
    }

    public static FxWindow create(KlPreferencesFactory preferencesFactory) {
        return factory().create(preferencesFactory);
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory implements KlFxWindow.Factory {

        @Override
        public FxWindow restore(KometPreferences preferences) {
            FxWindow fxWindow = new FxWindow(preferences);
            return fxWindow;
        }

        @Override
        public FxWindow create(KlPreferencesFactory preferencesFactory) {
            return new FxWindow(preferencesFactory, this);
        }
    }
    public static class ComponentVersionTestFactory implements KlFxWindow.Factory {
        @Override
        public FxWindow restore(KometPreferences preferences) {
            FxWindow fxWindow = new FxWindow(preferences);
            return fxWindow;
        }

        @Override
        public FxWindow create(KlPreferencesFactory preferencesFactory) {
            // TODO: Think more about layoutKeyForArea use here...
            FxWindow fxWindow = new FxWindow(preferencesFactory, this);
            RenderView renderView = new RenderView.Factory().create(fxWindow.childPreferencesFactory(RenderView.class));
            fxWindow.addChild(renderView);

            SupplementalTestArea supplementalArea = SupplementalTestArea.factory()
                    .create(renderView.childPreferencesFactory(SupplementalTestArea.class),
                            AreaGridSettings.DEFAULT.with(ChronologyDetailsArea.Factory.class));
            renderView.addChild(supplementalArea);


            ViewContextMenuButtonArea viewContextMenuButtonArea = ViewContextMenuButtonArea.factory()
                    .create(supplementalArea.childPreferencesFactory(ViewContextMenuButtonArea.class),
                            AreaGridSettings.DEFAULT.with(ChronologyDetailsArea.Factory.class));
            supplementalArea.addChild(viewContextMenuButtonArea);

            AreaGridSettings componentVersionsSettings = AreaGridSettings.DEFAULT.with(ChronologyDetailsArea.Factory.class)
                    .withLayoutKeyForArea(renderView.getMasterLayout().rootLayoutKey());

            ChronologyDetailsArea componentVersionsArea =
                    ChronologyDetailsArea.factory().create(viewContextMenuButtonArea.childPreferencesFactory(
                            ChronologyDetailsArea.class), componentVersionsSettings);

            viewContextMenuButtonArea.addChild(componentVersionsArea);
            supplementalArea.setMasterLayout(new SimpleKnowledgeLayout(componentVersionsArea));

            return fxWindow;
        }
    }


    @Override
    public void knowledgeLayoutUnbind() {
        // Nothing to do here.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

}