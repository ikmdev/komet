/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.app;

import com.jpro.webapi.WebAPI;
import de.jangassen.MenuToolkit;
import de.jangassen.model.AppearanceMode;
import dev.ikm.komet.details.DetailsNodeFactory;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.ScreenInfo;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.graphics.LoadFonts;
import dev.ikm.komet.framework.preferences.KometPreferencesStage;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.framework.tabs.DetachableTab;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.window.KometStageController;
import dev.ikm.komet.framework.window.MainWindowRecord;
import dev.ikm.komet.framework.window.WindowComponent;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.events.CreateJournalEvent;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.events.SignInUserEvent;
import dev.ikm.komet.kview.mvvm.view.export.ExportController;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.view.journal.JournalViewFactory;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageController;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageViewFactory;
import dev.ikm.komet.kview.mvvm.view.login.LoginPageController;
import dev.ikm.komet.list.ListNodeFactory;
import dev.ikm.komet.navigator.graph.GraphNavigatorNodeFactory;
import dev.ikm.komet.navigator.pattern.PatternNavigatorFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.komet.progress.CompletionNodeFactory;
import dev.ikm.komet.progress.ProgressNodeFactory;
import dev.ikm.komet.search.SearchNodeFactory;
import dev.ikm.komet.table.TableNodeFactory;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import one.jpro.platform.auth.core.authentication.User;
import one.jpro.platform.internal.util.CommandRunner;
import one.jpro.platform.internal.util.PlatformUtils;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.app.AppState.*;
import static dev.ikm.komet.app.LoginFeatureFlag.ENABLED_WEB_ONLY;
import static dev.ikm.komet.app.util.CssUtils.*;
import static dev.ikm.komet.framework.KometNodeFactory.KOMET_NODES;
import static dev.ikm.komet.framework.window.WindowSettings.Keys.*;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.EventTopics.USER_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.preferences.JournalWindowPreferences.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.*;

/**
 * Main application class for the Komet application, extending JavaFX {@link Application}.
 * <p>
 * The {@code WebApp} class serves as the entry point for launching the Komet application,
 * which is a JavaFX-based application supporting both desktop and web platforms via JPro.
 * It manages initialization, startup, and shutdown processes, and handles various application states
 * such as starting, login, data source selection, running, and shutdown.
 * </p>
 * <p>
 * <h4>Platform-Specific Features:</h4>
 * <ul>
 *   <li><strong>Web Support:</strong> Utilizes JPro's {@link WebAPI} to support running the application in a web browser.</li>
 *   <li><strong>macOS Integration:</strong> Configures macOS-specific properties and menus.</li>
 * </ul>
 * </p>
 * <p>
 * <h4>Event Bus and Subscriptions:</h4>
 * The application uses an event bus ({@link EvtBus}) for inter-component communication. It subscribes to various events like
 * {@code CreateJournalEvent} and {@code SignInUserEvent} to handle user actions and state changes.
 * </p>
 *
 * @see Application
 * @see AppState
 * @see LoginFeatureFlag
 * @see KometPreferences
 */
public class WebApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(WebApp.class);
    public static final String ICON_LOCATION = "/icons/Komet.png";
    public static final SimpleObjectProperty<AppState> state = new SimpleObjectProperty<>(STARTING);
    public static final SimpleObjectProperty<User> userProperty = new SimpleObjectProperty<>();

    private static Stage primaryStage;
    private static Stage classicKometStage;
    private static long windowCount = 1;
    private static KometPreferencesStage kometPreferencesStage;

    private static WebAPI webAPI;
    private static final boolean IS_BROWSER = WebAPI.isBrowser();
    private static final boolean IS_DESKTOP = !IS_BROWSER && PlatformUtils.isDesktop();
    private static final boolean IS_MAC = !IS_BROWSER && PlatformUtils.isMac();
    private final StackPane rootPane = createRootPane();
    private Image appIcon;

    /**
     * An entry point to launch the newer UI panels.
     */
    private MenuItem createJournalViewMenuItem;

    /**
     * This is a list of new windows that have been launched. During shutdown, the application close each stage gracefully.
     */
    private final List<JournalController> journalControllersList = new ArrayList<>();

    private EvtBus kViewEventBus;

    /**
     * Main method that serves as the entry point for the JavaFX application.
     *
     * @param args Command line arguments for the application.
     */
    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }

    /**
     * Configures system properties specific to macOS to ensure proper integration
     * with the macOS application menu.
     */
    private static void configureMacOSProperties() {
        // Ensures that the macOS application menu is used instead of a screen menu bar
        System.setProperty("apple.laf.useScreenMenuBar", "false");

        // Sets the name of the application in the macOS application menu
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Komet");
    }

    /**
     * Adds a shutdown hook to the Java Virtual Machine (JVM) to ensure that data is saved and resources
     * are released gracefully before the application exits.
     * This method should be called during the application's initialization phase to ensure that the shutdown
     * hook is registered before the application begins execution. By doing so, it guarantees that critical
     * cleanup operations are performed even if the application is terminated unexpectedly.
     */
    private static void addShutdownHook() {
        // Adding a shutdown hook that ensures data is saved and resources are released before the application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Starting shutdown hook");

            try {
                // Save and stop primitive data services gracefully
                PrimitiveData.save();
                PrimitiveData.stop();
            } catch (Exception e) {
                LOG.error("Error during shutdown hook execution", e);
            }

            LOG.info("Finished shutdown hook");
        }));
    }

    private static void createNewStage() {
        Stage stage = new Stage();
        stage.setScene(new Scene(new StackPane()));
        stage.setTitle("New stage" + " " + (windowCount++));
        stage.show();
    }

    private static ImmutableList<DetachableTab> makeDefaultLeftTabs(ObservableViewNoOverride windowView) {
        GraphNavigatorNodeFactory navigatorNodeFactory = new GraphNavigatorNodeFactory();
        KometNode navigatorNode1 = navigatorNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab navigatorNode1Tab = new DetachableTab(navigatorNode1);


        PatternNavigatorFactory patternNavigatorNodeFactory = new PatternNavigatorFactory();

        KometNode patternNavigatorNode2 = patternNavigatorNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        DetachableTab patternNavigatorNode1Tab = new DetachableTab(patternNavigatorNode2);

        return Lists.immutable.of(navigatorNode1Tab, patternNavigatorNode1Tab);
    }

    private static ImmutableList<DetachableTab> makeDefaultCenterTabs(ObservableViewNoOverride windowView) {
        DetailsNodeFactory detailsNodeFactory = new DetailsNodeFactory();
        KometNode detailsNode1 = detailsNodeFactory.create(windowView,
                ActivityStreams.NAVIGATION, ActivityStreamOption.SUBSCRIBE.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);

        DetachableTab detailsNode1Tab = new DetachableTab(detailsNode1);
        // TODO: setting up tab graphic, title, and tooltip needs to be standardized by the factory...
        detailsNode1Tab.textProperty().bind(detailsNode1.getTitle());
        detailsNode1Tab.tooltipProperty().setValue(detailsNode1.makeToolTip());

        KometNode detailsNode2 = detailsNodeFactory.create(windowView,
                ActivityStreams.SEARCH, ActivityStreamOption.SUBSCRIBE.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab detailsNode2Tab = new DetachableTab(detailsNode2);

        KometNode detailsNode3 = detailsNodeFactory.create(windowView,
                ActivityStreams.UNLINKED, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab detailsNode3Tab = new DetachableTab(detailsNode3);

        ListNodeFactory listNodeFactory = new ListNodeFactory();
        KometNode listNode = listNodeFactory.create(windowView,
                ActivityStreams.LIST, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab listNodeNodeTab = new DetachableTab(listNode);

        TableNodeFactory tableNodeFactory = new TableNodeFactory();
        KometNode tableNode = tableNodeFactory.create(windowView,
                ActivityStreams.UNLINKED, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab tableNodeTab = new DetachableTab(tableNode);

        return Lists.immutable.of(detailsNode1Tab, detailsNode2Tab, detailsNode3Tab, listNodeNodeTab, tableNodeTab);
    }

    private static ImmutableList<DetachableTab> makeDefaultRightTabs(ObservableViewNoOverride windowView) {
        SearchNodeFactory searchNodeFactory = new SearchNodeFactory();
        KometNode searchNode = searchNodeFactory.create(windowView,
                ActivityStreams.SEARCH, ActivityStreamOption.PUBLISH.keyForOption(), AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab newSearchTab = new DetachableTab(searchNode);

        ProgressNodeFactory progressNodeFactory = new ProgressNodeFactory();
        KometNode kometNode = progressNodeFactory.create(windowView,
                null, null, AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab progressTab = new DetachableTab(kometNode);

        CompletionNodeFactory completionNodeFactory = new CompletionNodeFactory();
        KometNode completionNode = completionNodeFactory.create(windowView,
                null, null, AlertStreams.ROOT_ALERT_STREAM_KEY);
        DetachableTab completionTab = new DetachableTab(completionNode);

        return Lists.immutable.of(newSearchTab, progressTab, completionTab);
    }

    @Override
    public void init() throws Exception {
        LOG.info("Starting Komet");

        // Set system properties for macOS look and feel and application name in the menu bar
        configureMacOSProperties();

        // Add a shutdown hook to ensure resources are saved and properly cleaned up before the application exits
        addShutdownHook();

        // Load custom fonts required by the application
        LoadFonts.load();

        // Load the application icon
        appIcon = new Image(WebApp.class.getResource(ICON_LOCATION).toString(), true);

        // Initialize the event bus for inter-component communication
        kViewEventBus = EvtBusFactory.getInstance(EvtBus.class);

        // Create a subscriber for handling CreateJournalEvent
        Subscriber<CreateJournalEvent> detailsSubscriber = evt -> {
            String journalName = evt.getWindowSettingsObjectMap().getValue(JOURNAL_TITLE);
            // Check if a journal window with the same title is already open
            journalControllersList.stream()
                    .filter(journalController -> journalController.getTitle().equals(journalName))
                    .findFirst()
                    .ifPresentOrElse(journalController -> {
                                if (IS_BROWSER) {
                                    // Similar to the desktop version, bring the existing tab to the front
                                    Stage journalStage = (Stage) journalController.getJournalBorderPane().getScene().getWindow();
                                    webAPI.openStageAsTab(journalStage, journalName.replace(" ", "_"));
                                } else {
                                    // Bring the existing window to the front
                                    journalController.windowToFront();
                                }
                            },
                            () -> launchJournalViewPage(evt.getWindowSettingsObjectMap()));
        };

        // Subscribe the subscriber to the JOURNAL_TOPIC
        kViewEventBus.subscribe(JOURNAL_TOPIC, CreateJournalEvent.class, detailsSubscriber);

        Subscriber<SignInUserEvent> signInUserEventSubscriber = evt -> {
            final User user = evt.getUser();
            userProperty.set(user);

            if (state.get() == RUNNING) {
                launchLandingPage(primaryStage, user);
            } else {
                state.set(AppState.SELECT_DATA_SOURCE);
                state.addListener(this::appStateChangeListener);
                launchSelectDataSourcePage(primaryStage);
            }
        };

        // Subscribe the subscriber to the USER_TOPIC
        kViewEventBus.subscribe(USER_TOPIC, SignInUserEvent.class, signInUserEventSubscriber);
    }

    @Override
    public void start(Stage stage) {
        try {
            WebApp.primaryStage = stage;
            Thread.currentThread().setUncaughtExceptionHandler((thread, exception) ->
                    AlertStreams.getRoot().dispatch(AlertObject.makeError(exception)));

            // Initialize the JPro WebAPI
            if (IS_BROWSER) {
                webAPI = WebAPI.getWebAPI(stage);
            }

            // Set the application icon
            stage.getIcons().setAll(appIcon);

            Scene scene = new Scene(rootPane);
            addStylesheets(scene, KOMET_CSS, KVIEW_CSS);
            stage.setScene(scene);

            // Handle the login feature based on the platform and the provided feature flag
            handleLoginFeature(ENABLED_WEB_ONLY, stage);

            addEventFilters(stage);

            // This is called only when the user clicks the close button on the window
            stage.setOnCloseRequest(windowEvent -> state.set(SHUTDOWN));

            // Show stage and set initial state
            stage.show();
        } catch (Exception ex) {
            LOG.error("Failed to initialize the application", ex);
            Platform.exit();
        }
    }

    /**
     * Handles the login feature based on the provided {@link LoginFeatureFlag} and platform.
     *
     * @param loginFeatureFlag the current state of the login feature
     * @param stage            the current application stage
     */
    public void handleLoginFeature(LoginFeatureFlag loginFeatureFlag, Stage stage) {
        switch (loginFeatureFlag) {
            case ENABLED_WEB_ONLY -> {
                if (IS_BROWSER) {
                    startLogin(stage);
                } else {
                    startSelectDataSource(stage);
                }
            }
            case ENABLED_DESKTOP_ONLY -> {
                if (IS_DESKTOP) {
                    startLogin(stage);
                } else {
                    startSelectDataSource(stage);
                }
            }
            case ENABLED -> startLogin(stage);
            case DISABLED -> startSelectDataSource(stage);
        }
    }

    /**
     * Initiates the login process by setting the application state to {@link AppState#LOGIN}
     * and launching the login page.
     *
     * @param stage the current application stage
     */
    private void startLogin(Stage stage) {
        state.set(LOGIN);
        launchLoginPage(stage);
    }

    /**
     * Initiates the data source selection process by setting the application state
     * to {@link AppState#SELECT_DATA_SOURCE}, adding a state change listener, and launching
     * the data source selection page.
     *
     * @param stage the current application stage
     */
    private void startSelectDataSource(Stage stage) {
        state.set(SELECT_DATA_SOURCE);
        state.addListener(this::appStateChangeListener);
        launchSelectDataSourcePage(stage);
    }

    @Override
    public void stop() {
        LOG.info("Stopping application\n\n###############\n\n");

        if (IS_DESKTOP) {
            // close all journal windows
            journalControllersList.forEach(JournalController::close);
        }
    }

    private StackPane createRootPane(Node... children) {
        return new StackPane(children) {

            @Override
            protected void layoutChildren() {
                if (IS_BROWSER) {
                    Scene scene = primaryStage.getScene();
                    if (scene != null) {
                        webAPI.layoutRoot(scene);
                    }
                }
                super.layoutChildren();
            }
        };
    }

    private void setupMenus() {
        MenuToolkit menuToolkit = MenuToolkit.toolkit();
        Menu kometAppMenu;
        if (IS_MAC) {
            kometAppMenu = menuToolkit.createDefaultApplicationMenu("Komet");
        } else {
            kometAppMenu = new Menu("Komet");
        }

        MenuItem prefsItem = new MenuItem("Komet preferences...");
        prefsItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
        prefsItem.setOnAction(event -> WebApp.kometPreferencesStage.showPreferences());

        if (IS_MAC) {
            kometAppMenu.getItems().add(2, prefsItem);
            kometAppMenu.getItems().add(3, new SeparatorMenuItem());
            MenuItem appleQuit = kometAppMenu.getItems().getLast();
            appleQuit.setOnAction(event -> quit());
        } else {
            kometAppMenu.getItems().addAll(prefsItem, new SeparatorMenuItem());
        }

        MenuBar menuBar = new MenuBar(kometAppMenu);

        if (state.get() == RUNNING) {
            Menu fileMenu = createFileMenu(menuToolkit);
            Menu editMenu = createEditMenu();
            Menu viewMenu = createViewMenu();
            menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);
        }

        Menu windowMenu = createWindowMenu(menuToolkit);
        Menu helpMenu = createHelpMenu();
        menuBar.getMenus().addAll(windowMenu, helpMenu);

        if (IS_MAC) {
            menuToolkit.setApplicationMenu(kometAppMenu);
            menuToolkit.setAppearanceMode(AppearanceMode.AUTO);
            menuToolkit.setDockIconMenu(createDockMenu());
            menuToolkit.autoAddWindowMenuItems(windowMenu);
            menuToolkit.setGlobalMenuBar(menuBar);
            menuToolkit.setTrayMenu(createSampleMenu());
        }
    }

    private Menu createFileMenu(MenuToolkit tk) {
        Menu fileMenu = new Menu("File");

        // Import Dataset Menu Item
        MenuItem importDatasetMenuItem = new MenuItem("Import Dataset...");
        importDatasetMenuItem.setOnAction(actionEvent -> doImportDataSet(primaryStage));

        // Export Dataset Menu Item
        MenuItem exportDatasetMenuItem = new MenuItem("Export Dataset...");
        exportDatasetMenuItem.setOnAction(actionEvent -> openExport(primaryStage));

        // Close Window Menu Item
        MenuItem closeWindowMenuItem = tk.createCloseWindowMenuItem();

        // Add menu items to the File menu
        fileMenu.getItems().addAll(
                importDatasetMenuItem,
                exportDatasetMenuItem,
                new SeparatorMenuItem(),
                closeWindowMenuItem);

        return fileMenu;
    }

    private Menu createEditMenu() {
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(
                createMenuItem("Undo"),
                createMenuItem("Redo"),
                new SeparatorMenuItem(),
                createMenuItem("Cut"),
                createMenuItem("Copy"),
                createMenuItem("Paste"),
                createMenuItem("Select All"));
        return editMenu;
    }

    private Menu createViewMenu() {
        Menu viewMenu = new Menu("View");
        MenuItem classicKometMenuItem = new MenuItem("Classic Komet");
        classicKometMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN));
        classicKometMenuItem.setOnAction(actionEvent -> {
            try {
                launchClassicKomet();
            } catch (IOException | BackingStoreException e) {
                throw new RuntimeException(e);
            }
        });
        viewMenu.getItems().add(classicKometMenuItem);
        return viewMenu;
    }

    private Menu createWindowMenu(MenuToolkit menuToolkit) {
        Menu windowMenu = new Menu("Window");
        windowMenu.getItems().addAll(
                menuToolkit.createMinimizeMenuItem(),
                menuToolkit.createZoomMenuItem(),
                menuToolkit.createCycleWindowsItem(),
                new SeparatorMenuItem(),
                menuToolkit.createBringAllToFrontItem());
        return windowMenu;
    }

    private Menu createHelpMenu() {
        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().add(new MenuItem("Getting started"));
        return helpMenu;
    }

    private void launchLoginPage(Stage stage) {
        JFXNode<BorderPane, Void> loginNode = FXMLMvvmLoader.make(
                LoginPageController.class.getResource("login-page.fxml"));
        BorderPane loginPane = loginNode.node();
        rootPane.getChildren().setAll(loginPane);
        stage.setTitle("KOMET Login");

        setupMenus();
    }

    private void launchSelectDataSourcePage(Stage stage) {
        try {
            FXMLLoader sourceLoader = new FXMLLoader(getClass().getResource("SelectDataSource.fxml"));
            BorderPane sourceRoot = sourceLoader.load();
            SelectDataSourceController sourceController = sourceLoader.getController();
            sourceController.getCancelButton().setOnAction(actionEvent -> {
                // Exit the application if the user cancels the data source selection
                Platform.exit();
                stopServer();
            });
            rootPane.getChildren().setAll(sourceRoot);
            stage.setTitle("KOMET Startup");

            setupMenus();
        } catch (IOException ex) {
            LOG.error("Failed to initialize the select data source window", ex);
        }
    }

    private void addEventFilters(Stage stage) {
        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            ScreenInfo.mouseIsPressed(true);
            ScreenInfo.mouseWasDragged(false);
        });
        stage.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            ScreenInfo.mouseIsPressed(false);
            ScreenInfo.mouseIsDragging(false);
        });
        stage.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            ScreenInfo.mouseIsDragging(true);
            ScreenInfo.mouseWasDragged(true);
        });
    }

    private void doImportDataSet(Stage stage) {
        if (IS_DESKTOP) { // TODO: Use JPro File module to handle file operations for desktop and browser
            File selectedFile = getFileFromChooser(stage);
            // selectedFile is null if the user clicks cancel
            if (selectedFile != null) {
                try {
                    LoadEntitiesFromProtobufFile loadEntities = new LoadEntitiesFromProtobufFile(selectedFile);
                    ProgressHelper.progress(loadEntities, "Cancel Import");
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private File getFileFromChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        return fileChooser.showOpenDialog(stage);
    }

    private void launchLandingPage(Stage stage, User user) {
        try {
            rootPane.getChildren().clear(); // Clear the root pane before adding new content

            KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
            KometPreferences windowPreferences = appPreferences.node("main-komet-window");
            WindowSettings windowSettings = new WindowSettings(windowPreferences);

            FXMLLoader landingPageLoader = LandingPageViewFactory.createFXMLLoader();
            BorderPane landingPageBorderPane = landingPageLoader.load();

            if (!IS_MAC) {
                createMenuOptions(landingPageBorderPane);
            }

            LandingPageController landingPageController = landingPageLoader.getController();
            landingPageController.getWelcomeTitleLabel().setText("Welcome " + user.getName());

            stage.setTitle("Landing Page");
            stage.setMaximized(true);
            stage.setOnCloseRequest(windowEvent -> {
                // This is called only when the user clicks the close button on the window
                state.set(SHUTDOWN);
                landingPageController.cleanup();
            });

            rootPane.getChildren().add(landingPageBorderPane);

            setupMenus();
        } catch (IOException e) {
            LOG.error("Failed to initialize the landing page window", e);
        }
    }

    /**
     * When a user selects the menu option View/New Journal a new Stage Window is launched.
     * This method will load a navigation panel to be a publisher and windows will be connected
     * (subscribed) to the activity stream.
     *
     * @param journalWindowSettings if present will give the size and positioning of the journal window
     */
    private void launchJournalViewPage(PrefX journalWindowSettings) {
        try {
            KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
            KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
            WindowSettings windowSettings = new WindowSettings(windowPreferences);

            FXMLLoader journalLoader = JournalViewFactory.createFXMLLoader();
            BorderPane journalBorderPane = journalLoader.load();
            JournalController journalController = journalLoader.getController();

            Scene sourceScene = new Scene(journalBorderPane, 800, 600);
            addStylesheets(sourceScene, KOMET_CSS, KVIEW_CSS);

            Stage journalStage = new Stage();
            journalStage.getIcons().setAll(appIcon);
            journalStage.setScene(sourceScene);

            if (!IS_MAC) {
                generateMsWindowsMenu(journalBorderPane, journalStage);
            }

            if (journalWindowSettings != null) {
                // load journal specific window settings
                String journalName = journalWindowSettings.getValue(JOURNAL_TITLE);
                journalStage.setTitle(journalName);
                if (journalWindowSettings.getValue(JOURNAL_HEIGHT) != null) {
                    journalStage.setHeight(journalWindowSettings.getValue(JOURNAL_HEIGHT));
                    journalStage.setWidth(journalWindowSettings.getValue(JOURNAL_WIDTH));
                    journalStage.setX(journalWindowSettings.getValue(JOURNAL_XPOS));
                    journalStage.setY(journalWindowSettings.getValue(JOURNAL_YPOS));
                    journalController.recreateConceptWindows(journalWindowSettings);
                }else{
                    journalStage.setMaximized(true);
                }
            }

            journalStage.setOnHidden(windowEvent -> {
                saveJournalWindowsToPreferences();
                journalController.shutdown();
                journalControllersList.remove(journalController);

                if (journalWindowSettings != null) {
                    journalWindowSettings.setValue(CAN_DELETE, true);
                    kViewEventBus.publish(JOURNAL_TOPIC,
                            new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
                }
            });

            journalController.setWindowView(windowSettings.getView());
            journalStage.setOnShown(windowEvent -> {
                KometNodeFactory navigatorNodeFactory = new GraphNavigatorNodeFactory();
                KometNodeFactory searchNodeFactory = new SearchNodeFactory();

                journalController.launchKometFactoryNodes(
                        journalWindowSettings.getValue(JOURNAL_TITLE),
                        navigatorNodeFactory,
                        searchNodeFactory);
                // load additional panels
                journalController.loadNextGenReasonerPanel();
                journalController.loadNextGenSearchPanel();
            });
            // disable the delete menu option for a Journal Card.
            journalWindowSettings.setValue(CAN_DELETE, false);
            kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
            journalControllersList.add(journalController);

            if (IS_BROWSER) {
                String journalName = journalWindowSettings.getValue(JOURNAL_TITLE);
                webAPI.openStageAsTab(journalStage, journalName.replace(" ", "_"));
            } else {
                journalStage.show();
            }
        } catch (IOException e) {
            LOG.error("Failed to launch the journal view window", e);
        }
    }

    private void saveJournalWindowsToPreferences() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences journalPreferences = appPreferences.node(JOURNAL_WINDOW);

        // Non launched journal windows should be preserved.
        List<String> journalSubWindowFoldersFromPref = journalPreferences.getList(JOURNAL_NAMES);

        // launched (journal Controllers List) will overwrite existing window preferences.
        List<String> journalSubWindowFolders = new ArrayList<>(journalControllersList.size());
        for (JournalController controller : journalControllersList) {
            String journalSubWindowPrefFolder = controller.generateJournalDirNameBasedOnTitle();
            journalSubWindowFolders.add(journalSubWindowPrefFolder);

            KometPreferences journalSubWindowPreferences = appPreferences.node(JOURNAL_WINDOW +
                    File.separator + journalSubWindowPrefFolder);
            controller.saveConceptWindowPreferences(journalSubWindowPreferences);
            journalSubWindowPreferences.put(JOURNAL_TITLE, controller.getTitle());
            journalSubWindowPreferences.putDouble(JOURNAL_HEIGHT, controller.getHeight());
            journalSubWindowPreferences.putDouble(JOURNAL_WIDTH, controller.getWidth());
            journalSubWindowPreferences.putDouble(JOURNAL_XPOS, controller.getX());
            journalSubWindowPreferences.putDouble(JOURNAL_YPOS, controller.getY());
            journalSubWindowPreferences.put(JOURNAL_AUTHOR, LandingPageController.DEMO_AUTHOR);
            journalSubWindowPreferences.putLong(JOURNAL_LAST_EDIT, (LocalDateTime.now())
                    .atZone(ZoneId.systemDefault()).toEpochSecond());
            try {
                journalSubWindowPreferences.flush();
            } catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }

        }

        // Make sure windows that are not summoned will not be deleted (not added to JOURNAL_NAMES)
        for (String x : journalSubWindowFolders) {
            if (!journalSubWindowFoldersFromPref.contains(x)) {
                journalSubWindowFoldersFromPref.add(x);
            }
        }
        journalPreferences.putList(JOURNAL_NAMES, journalSubWindowFoldersFromPref);

        try {
            journalPreferences.flush();
            appPreferences.flush();
            appPreferences.sync();
        } catch (BackingStoreException e) {
            LOG.error("error writing journal window flag to preferences", e);
        }
    }

    private MenuItem createMenuItem(String title) {
        MenuItem menuItem = new MenuItem(title);
        menuItem.setOnAction(this::handleEvent);
        return menuItem;
    }

    private Menu createDockMenu() {
        Menu dockMenu = createSampleMenu();
        MenuItem open = new MenuItem("New Window");
        open.setGraphic(Icon.OPEN.makeIcon());
        open.setOnAction(e -> createNewStage());
        dockMenu.getItems().addAll(new SeparatorMenuItem(), open);
        return dockMenu;
    }

    private Menu createSampleMenu() {
        Menu trayMenu = new Menu();
        trayMenu.setGraphic(Icon.TEMPORARY_FIX.makeIcon());
        MenuItem reload = new MenuItem("Reload");
        reload.setGraphic(Icon.SYNCHRONIZE_WITH_STREAM.makeIcon());
        reload.setOnAction(this::handleEvent);
        MenuItem print = new MenuItem("Print");
        print.setOnAction(this::handleEvent);

        Menu share = new Menu("Share");
        MenuItem mail = new MenuItem("Mail");
        mail.setOnAction(this::handleEvent);
        share.getItems().add(mail);

        trayMenu.getItems().addAll(reload, print, new SeparatorMenuItem(), share);
        return trayMenu;
    }

    private void handleEvent(ActionEvent actionEvent) {
        LOG.debug("clicked " + actionEvent.getSource());  // NOSONAR
    }

    private void appStateChangeListener(ObservableValue<? extends AppState> observable, AppState oldValue, AppState newValue) {
        try {
            switch (newValue) {
                case SELECTED_DATA_SOURCE -> {
                    Platform.runLater(() -> state.set(LOADING_DATA_SOURCE));
                    TinkExecutor.threadPool().submit(new LoadDataSourceTask(state));
                }
                case RUNNING -> {
                    if (userProperty.get() == null) {
                        String username = System.getProperty("user.name");
                        String capitalizeUsername = username.substring(0, 1).toUpperCase() + username.substring(1);
                        userProperty.set(new User(capitalizeUsername));
                    }
                    launchLandingPage(primaryStage, userProperty.get());
                }
                case SHUTDOWN -> quit();
            }
        } catch (Throwable e) {
            LOG.error("Error during state change", e);
            Platform.exit();
        }
    }

    private void launchClassicKomet() throws IOException, BackingStoreException {
        if (IS_DESKTOP) {
            // If already launched bring to the front
            if (classicKometStage != null && classicKometStage.isShowing()) {
                classicKometStage.show();
                classicKometStage.toFront();
                return;
            }
        }

        classicKometStage = new Stage();
        classicKometStage.getIcons().setAll(appIcon);

        //Starting up preferences and getting configurations
        Preferences.start();
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        boolean appInitialized = appPreferences.getBoolean(AppKeys.APP_INITIALIZED, false);
        if (appInitialized) {
            LOG.info("Restoring configuration preferences.");
        } else {
            LOG.info("Creating new configuration preferences.");
        }

        MainWindowRecord mainWindowRecord = MainWindowRecord.make();

        BorderPane kometRoot = mainWindowRecord.root();
        KometStageController controller = mainWindowRecord.controller();

        //Loading/setting the Komet screen
        Scene kometScene = new Scene(kometRoot, 1800, 1024);
        addStylesheets(kometScene, KOMET_CSS);

        // if NOT on macOS
        if (!IS_MAC) {
            generateMsWindowsMenu(kometRoot, classicKometStage);
        }

        classicKometStage.setScene(kometScene);

        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
        boolean mainWindowInitialized = windowPreferences.getBoolean(KometStageController.WindowKeys.WINDOW_INITIALIZED, false);
        controller.setup(windowPreferences);
        classicKometStage.setTitle("Komet");

        if (!mainWindowInitialized) {
            controller.setLeftTabs(makeDefaultLeftTabs(controller.windowView()), 0);
            controller.setCenterTabs(makeDefaultCenterTabs(controller.windowView()), 0);
            controller.setRightTabs(makeDefaultRightTabs(controller.windowView()), 1);
            windowPreferences.putBoolean(KometStageController.WindowKeys.WINDOW_INITIALIZED, true);
            appPreferences.putBoolean(AppKeys.APP_INITIALIZED, true);
        } else {
            // Restore nodes from preferences.
            windowPreferences.get(LEFT_TAB_PREFERENCES).ifPresent(leftTabPreferencesName ->
                    restoreTab(windowPreferences, leftTabPreferencesName, controller.windowView(),
                            controller::leftBorderPaneSetCenter));
            windowPreferences.get(CENTER_TAB_PREFERENCES).ifPresent(centerTabPreferencesName ->
                    restoreTab(windowPreferences, centerTabPreferencesName, controller.windowView(),
                            controller::centerBorderPaneSetCenter));
            windowPreferences.get(RIGHT_TAB_PREFERENCES).ifPresent(rightTabPreferencesName ->
                    restoreTab(windowPreferences, rightTabPreferencesName, controller.windowView(),
                            controller::rightBorderPaneSetCenter));
        }
        //Setting X and Y coordinates for location of the Komet stage
        classicKometStage.setX(controller.windowSettings().xLocationProperty().get());
        classicKometStage.setY(controller.windowSettings().yLocationProperty().get());
        classicKometStage.setHeight(controller.windowSettings().heightProperty().get());
        classicKometStage.setWidth(controller.windowSettings().widthProperty().get());
        classicKometStage.show();

        if (IS_BROWSER) {
            webAPI.openStageAsTab(classicKometStage);
        }

        WebApp.kometPreferencesStage = new KometPreferencesStage(controller.windowView().makeOverridableViewProperties());

        windowPreferences.sync();
        appPreferences.sync();

        if (createJournalViewMenuItem != null) {
            createJournalViewMenuItem.setDisable(false);
            KeyCombination newJournalKeyCombo = new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN);
            createJournalViewMenuItem.setAccelerator(newJournalKeyCombo);
            KometPreferences journalPreferences = appPreferences.node(JOURNAL_WINDOW);
        }
    }

    private void openExport(Window owner) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        Stage exportStage = new Stage();
        exportStage.initOwner(owner);
        //set up ExportViewModel
        Config exportConfig = new Config(ExportController.class.getResource("export.fxml"))
                .updateViewModel("exportViewModel", (exportViewModel) ->
                        exportViewModel.setPropertyValue(VIEW_PROPERTIES, windowSettings.getView().makeOverridableViewProperties()));
        JFXNode<Pane, ExportController> exportJFXNode = FXMLMvvmLoader.make(exportConfig);

        Pane exportPane = exportJFXNode.node();
        Scene exportScene = new Scene(exportPane);
        exportStage.setScene(exportScene);
        exportStage.show();
    }

    private void generateMsWindowsMenu(BorderPane kometRoot, Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem about = new MenuItem("About");
        about.setOnAction(actionEvent -> showWindowsAboutScreen(stage));
        fileMenu.getItems().add(about);

        // Importing data
        MenuItem importMenuItem = new MenuItem("Import Dataset");
        importMenuItem.setOnAction(actionEvent -> doImportDataSet(stage));
        fileMenu.getItems().add(importMenuItem);

        // Exporting data
        MenuItem exportDatasetMenuItem = new MenuItem("Export Dataset");
        exportDatasetMenuItem.setOnAction(actionEvent -> openExport(stage));
        fileMenu.getItems().add(exportDatasetMenuItem);

        MenuItem menuItemQuit = new MenuItem("Quit");
        KeyCombination quitKeyCombo = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        menuItemQuit.setOnAction(actionEvent -> quit());
        menuItemQuit.setAccelerator(quitKeyCombo);
        fileMenu.getItems().add(menuItemQuit);

        Menu editMenu = new Menu("Edit");
        MenuItem landingPage = new MenuItem("Landing Page");
        KeyCombination landingPageKeyCombo = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
        landingPage.setOnAction(actionEvent -> launchLandingPage(primaryStage, userProperty.get()));
        landingPage.setAccelerator(landingPageKeyCombo);
        landingPage.setDisable(IS_BROWSER);
        editMenu.getItems().add(landingPage);

        Menu windowMenu = new Menu("Window");
        MenuItem minimizeWindow = new MenuItem("Minimize");
        KeyCombination minimizeKeyCombo = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);
        minimizeWindow.setOnAction(event -> {
            Stage obj = (Stage) kometRoot.getScene().getWindow();
            obj.setIconified(true);
        });
        minimizeWindow.setAccelerator(minimizeKeyCombo);
        minimizeWindow.setDisable(IS_BROWSER);
        windowMenu.getItems().add(minimizeWindow);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(editMenu);
        menuBar.getMenus().add(windowMenu);
        //hBox.getChildren().add(menuBar);
        Platform.runLater(() -> kometRoot.setTop(menuBar));
    }

    private void showWindowsAboutScreen(Window owner) {
        Stage aboutWindow = new Stage();
        aboutWindow.initOwner(owner);
        Label kometLabel = new Label("Komet");
        kometLabel.setFont(new Font("Open Sans", 24));
        Label copyright = new Label("Copyright \u00a9 " + Year.now().getValue());
        copyright.setFont(new Font("Open Sans", 10));
        VBox container = new VBox(kometLabel, copyright);
        container.setAlignment(Pos.CENTER);
        Scene aboutScene = new Scene(container, 250, 100);
        aboutWindow.setScene(aboutScene);
        aboutWindow.setTitle("About Komet");
        aboutWindow.show();
    }

    private void quit() {
        //TODO: that this call will likely be moved into the landing page functionality
        saveJournalWindowsToPreferences();
        PrimitiveData.stop();
        Preferences.stop();
        Platform.exit();
        stopServer();
    }

    /**
     * Stops the JPro server by running the stop script.
     */
    public static void stopServer() {
        if (IS_BROWSER) {
            LOG.info("Stopping JPro server...");
            final String jproMode = WebAPI.getServerInfo().getJProMode();
            final String[] stopScriptArgs;
            final CommandRunner stopCommandRunner;
            final File workingDir;
            if ("dev".equalsIgnoreCase(jproMode)) {
                workingDir = new File(System.getProperty("user.dir")).getParentFile();
                stopScriptArgs = PlatformUtils.isWindows() ?
                        new String[]{"cmd", "/c", "mvnw.bat", "-f", "application", "-Pjpro", "jpro:stop"} :
                        new String[]{"bash", "./mvnw", "-f", "application", "-Pjpro", "jpro:stop"};
                stopCommandRunner = new CommandRunner(stopScriptArgs);
            } else {
                workingDir = new File("bin");
                final String scriptExtension = PlatformUtils.isWindows() ? ".bat" : ".sh";
                final String stopScriptName = "stop" + scriptExtension;
                stopScriptArgs = PlatformUtils.isWindows() ?
                        new String[]{"cmd", "/c", stopScriptName} : new String[]{"bash", stopScriptName};
                stopCommandRunner = new CommandRunner(stopScriptArgs);
            }
            try {
                stopCommandRunner.setPrintToConsole(true);
                final int exitCode = stopCommandRunner.run("jpro-stop", workingDir);
                if (exitCode == 0) {
                    LOG.info("JPro server stopped successfully.");
                } else {
                    LOG.error("Failed to stop JPro server. Exit code: {}", exitCode);
                }
            } catch (IOException | InterruptedException ex) {
                LOG.error("Error stopping the server", ex);
            }
        }
    }

    private void restoreTab(KometPreferences windowPreferences, String tabPreferenceNodeName, ObservableViewNoOverride windowView, Consumer<Node> nodeConsumer) {
        LOG.info("Restoring from: " + tabPreferenceNodeName);
        KometPreferences itemPreferences = windowPreferences.node(KOMET_NODES + tabPreferenceNodeName);
        itemPreferences.get(WindowComponent.WindowComponentKeys.FACTORY_CLASS).ifPresent(factoryClassName -> {
            try {
                Class<?> objectClass = Class.forName(factoryClassName);
                Class<? extends Annotation> annotationClass = Reconstructor.class;
                Object[] parameters = new Object[]{windowView, itemPreferences};
                WindowComponent windowComponent = (WindowComponent) Encodable.decode(objectClass, annotationClass, parameters);
                nodeConsumer.accept(windowComponent.getNode());

            } catch (Exception e) {
                AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            }
        });
    }

    public void createMenuOptions(BorderPane landingPageRoot) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem about = new MenuItem("About");
        about.setOnAction(actionEvent -> showWindowsAboutScreen(primaryStage));
        fileMenu.getItems().add(about);

        MenuItem menuItemQuit = new MenuItem("Quit");
        KeyCombination quitKeyCombo = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        menuItemQuit.setOnAction(actionEvent -> quit());
        menuItemQuit.setAccelerator(quitKeyCombo);
        fileMenu.getItems().add(menuItemQuit);

        Menu viewMenu = new Menu("View");
        MenuItem classicKometMenuItem = createClassicKometMenuItem();
        viewMenu.getItems().add(classicKometMenuItem);

        Menu windowMenu = new Menu("Window");
        MenuItem minimizeWindow = new MenuItem("Minimize");
        KeyCombination minimizeKeyCombo = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);
        minimizeWindow.setOnAction(event -> {
            Stage obj = (Stage) landingPageRoot.getScene().getWindow();
            obj.setIconified(true);
        });
        minimizeWindow.setAccelerator(minimizeKeyCombo);
        minimizeWindow.setDisable(IS_BROWSER);
        windowMenu.getItems().add(minimizeWindow);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(viewMenu);
        menuBar.getMenus().add(windowMenu);
        Platform.runLater(() -> landingPageRoot.setTop(menuBar));
    }

    private MenuItem createClassicKometMenuItem() {
        MenuItem classicKometMenuItem = new MenuItem("Classic Komet");
        KeyCombination classicKometKeyCombo = new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN);
        classicKometMenuItem.setOnAction(actionEvent -> {
            try {
                launchClassicKomet();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }
        });
        classicKometMenuItem.setAccelerator(classicKometKeyCombo);
        return classicKometMenuItem;
    }

    public enum AppKeys {
        APP_INITIALIZED
    }
}
