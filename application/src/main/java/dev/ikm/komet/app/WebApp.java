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
import dev.ikm.komet.app.aboutdialog.AboutDialog;
import dev.ikm.komet.details.DetailsNodeFactory;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.ScreenInfo;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtBus;
import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
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
import dev.ikm.komet.kview.controls.GlassPane;
import dev.ikm.komet.kview.events.CreateJournalEvent;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.events.SignInUserEvent;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import dev.ikm.komet.kview.mvvm.view.changeset.ExportController;
import dev.ikm.komet.kview.mvvm.view.changeset.ImportController;
import dev.ikm.komet.kview.mvvm.view.changeset.exchange.*;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageController;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageViewFactory;
import dev.ikm.komet.kview.mvvm.view.login.LoginPageController;
import dev.ikm.komet.kview.mvvm.viewmodel.GitHubPreferencesViewModel;
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
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import one.jpro.platform.auth.core.authentication.User;
import one.jpro.platform.utils.CommandRunner;
import one.jpro.platform.utils.PlatformUtils;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.app.AppState.*;
import static dev.ikm.komet.app.LoginFeatureFlag.ENABLED_WEB_ONLY;
import static dev.ikm.komet.app.util.CssFile.KOMET_CSS;
import static dev.ikm.komet.app.util.CssFile.KVIEW_CSS;
import static dev.ikm.komet.app.util.CssUtils.addStylesheets;
import static dev.ikm.komet.framework.KometNodeFactory.KOMET_NODES;
import static dev.ikm.tinkar.events.FrameworkTopics.IMPORT_TOPIC;
import static dev.ikm.tinkar.events.FrameworkTopics.LANDING_PAGE_TOPIC;
import static dev.ikm.komet.framework.window.WindowSettings.Keys.*;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.EventTopics.USER_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.fxutils.FXUtils.runOnFxThread;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.*;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask.OperationMode.*;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask.README_FILENAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.JournalViewModel.WINDOW_VIEW;
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
    private static final String CHANGESETS_DIR = "changeSets";
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
    private static final boolean IS_MAC_AND_NOT_TESTFX_TEST = IS_MAC && !isTestFXTest();
    private final StackPane rootPane = createRootPane();
    private Image appIcon;
    private LandingPageController landingPageController;
    private EvtBus kViewEventBus;

    /**
     * An entry point to launch the newer UI panels.
     */
    private MenuItem createJournalViewMenuItem;

    /**
     * This is a list of new windows that have been launched. During shutdown, the application close each stage gracefully.
     */
    private final List<JournalController> journalControllersList = new ArrayList<>();

    /**
     * GitHub preferences data access object.
     */
    private final GitHubPreferencesDao gitHubPreferencesDao = new GitHubPreferencesDao();

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
            final PrefX journalWindowSettingsObjectMap = evt.getWindowSettingsObjectMap();
            final UUID journalTopic = journalWindowSettingsObjectMap.getValue(JOURNAL_TOPIC);
            final String journalName = journalWindowSettingsObjectMap.getValue(JOURNAL_TITLE);
            // Check if a journal window with the same title is already open
            journalControllersList.stream()
                    .filter(journalController ->
                            journalController.getJournalTopic().equals(journalTopic))
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
                            () -> launchJournalViewPage(journalWindowSettingsObjectMap));
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

        //Pops up the import dialog window on any events received on the IMPORT_TOPIC
        Subscriber<Evt> importSubscriber = _ -> {
            openImport(primaryStage);
        };
        kViewEventBus.subscribe(IMPORT_TOPIC, Evt.class, importSubscriber);
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

        disconnectFromGithub();

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

    /**
     * Checks if the application is being tested using TestFX Framework.
     *
     * @return {@code true} if testing mode; {@code false} otherwise.
     */
    private static boolean isTestFXTest() {
        String testFxTest = System.getProperty("testfx.headless");
        return testFxTest != null && !testFxTest.isBlank();
    }

    private void setupMenus() {
        Menu kometAppMenu;

        if (IS_MAC_AND_NOT_TESTFX_TEST) {
            MenuToolkit menuToolkit = MenuToolkit.toolkit();
            kometAppMenu = menuToolkit.createDefaultApplicationMenu("Komet");
        } else {
            kometAppMenu = new Menu("Komet");
        }

        MenuItem prefsItem = new MenuItem("Komet preferences...");
        prefsItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
        prefsItem.setOnAction(event -> WebApp.kometPreferencesStage.showPreferences());

        if (IS_MAC_AND_NOT_TESTFX_TEST) {
            kometAppMenu.getItems().add(2, prefsItem);
            kometAppMenu.getItems().add(3, new SeparatorMenuItem());
            MenuItem appleQuit = kometAppMenu.getItems().getLast();
            appleQuit.setOnAction(event -> quit());
        } else {
            kometAppMenu.getItems().addAll(prefsItem, new SeparatorMenuItem());
        }

        MenuBar menuBar = new MenuBar(kometAppMenu);

        if (state.get() == RUNNING) {
            Menu fileMenu = createFileMenu();
            Menu editMenu = createEditMenu();
            Menu viewMenu = createViewMenu();
            menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);
        }

        if (IS_MAC_AND_NOT_TESTFX_TEST) {
            MenuToolkit menuToolkit = MenuToolkit.toolkit();
            menuToolkit.setApplicationMenu(kometAppMenu);
            menuToolkit.setAppearanceMode(AppearanceMode.AUTO);
            menuToolkit.setDockIconMenu(createDockMenu());
            Menu windowMenu = createWindowMenuOnMacOS();
            menuToolkit.autoAddWindowMenuItems(windowMenu);
            menuToolkit.setGlobalMenuBar(menuBar);
            menuToolkit.setTrayMenu(createSampleMenu());

            // Add the window menu to the menu bar
            menuBar.getMenus().add(windowMenu);
        }

        // Create and add the exchange menu to the menu bar
        Menu exchangeMenu = createExchangeMenu();
        menuBar.getMenus().add(exchangeMenu);

        // Create and add the help menu to the menu bar
        Menu helpMenu = createHelpMenu();
        menuBar.getMenus().add(helpMenu);
    }

    private Menu createFileMenu() {
        Menu fileMenu = new Menu("File");

        // Import Dataset Menu Item
        MenuItem importDatasetMenuItem = new MenuItem("Import Dataset...");
        importDatasetMenuItem.setOnAction(actionEvent -> openImport(primaryStage));

        // Export Dataset Menu Item
        MenuItem exportDatasetMenuItem = new MenuItem("Export Dataset...");
        exportDatasetMenuItem.setOnAction(actionEvent -> openExport(primaryStage));

        // Add menu items to the File menu
        fileMenu.getItems().addAll(importDatasetMenuItem, exportDatasetMenuItem);

        if (IS_MAC_AND_NOT_TESTFX_TEST) {
            // Close Window Menu Item
            MenuToolkit tk = MenuToolkit.toolkit();
            MenuItem closeWindowMenuItem = tk.createCloseWindowMenuItem();
            fileMenu.getItems().addAll(new SeparatorMenuItem(), closeWindowMenuItem);
        }

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

    private Menu createWindowMenuOnMacOS() {
        MenuToolkit menuToolkit = MenuToolkit.toolkit();
        Menu windowMenu = new Menu("Window");
        windowMenu.getItems().addAll(
                menuToolkit.createMinimizeMenuItem(),
                menuToolkit.createZoomMenuItem(),
                menuToolkit.createCycleWindowsItem(),
                new SeparatorMenuItem(),
                menuToolkit.createBringAllToFrontItem());
        return windowMenu;
    }

    private Menu createExchangeMenu() {
        Menu exchangeMenu = new Menu("Exchange");

        MenuItem infoMenuItem = new MenuItem("Info");
        infoMenuItem.setOnAction(actionEvent -> infoAction());
        MenuItem pullMenuItem = new MenuItem("Pull");
        pullMenuItem.setOnAction(actionEvent -> executeGitTask(PULL));
        MenuItem pushMenuItem = new MenuItem("Sync");
        pushMenuItem.setOnAction(actionEvent -> executeGitTask(SYNC));

        exchangeMenu.getItems().addAll(infoMenuItem, pullMenuItem, pushMenuItem);
        return exchangeMenu;
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

            landingPageController = landingPageLoader.getController();
            landingPageController.getWelcomeTitleLabel().setText("Welcome " + user.getName());
            landingPageController.setSelectedDatasetTitle(PrimitiveData.get().name());
            landingPageController.getGithubStatusHyperlink().setOnAction(_ -> connectToGithub());

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
        Objects.requireNonNull(journalWindowSettings, "journalWindowSettings cannot be null");
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
        final WindowSettings windowSettings = new WindowSettings(windowPreferences);
        final UUID journalTopic = journalWindowSettings.getValue(JOURNAL_TOPIC);
        Objects.requireNonNull(journalTopic, "journalTopic cannot be null");

        Config journalConfig = new Config(JournalController.class.getResource("journal.fxml"))
                .updateViewModel("journalViewModel", journalViewModel -> {
                    journalViewModel.setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic);
                    journalViewModel.setPropertyValue(WINDOW_VIEW, windowSettings.getView());
                });
        JFXNode<BorderPane, JournalController> journalJFXNode = FXMLMvvmLoader.make(journalConfig);
        BorderPane journalBorderPane = journalJFXNode.node();
        JournalController journalController = journalJFXNode.controller();

        Scene sourceScene = new Scene(journalBorderPane, DEFAULT_JOURNAL_WIDTH, DEFAULT_JOURNAL_HEIGHT);
        addStylesheets(sourceScene, KOMET_CSS, KVIEW_CSS);

        Stage journalStage = new Stage();
        journalStage.getIcons().setAll(appIcon);
        journalStage.setScene(sourceScene);

        if (!IS_MAC) {
            generateMsWindowsMenu(journalBorderPane, journalStage);
        }

        // load journal specific window settings
        final String journalName = journalWindowSettings.getValue(JOURNAL_TITLE);
        journalStage.setTitle(journalName);

        // Get the UUID-based directory name from preferences
        String journalDirName = journalWindowSettings.getValue(JOURNAL_DIR_NAME);

        // For new journals (no UUID yet), generate one using the controller's UUID
        if (journalDirName == null) {
            journalDirName = journalController.getJournalDirName();
            journalWindowSettings.setValue(JOURNAL_DIR_NAME, journalDirName);
        }

        if (journalWindowSettings.getValue(JOURNAL_HEIGHT) != null) {
            journalStage.setHeight(journalWindowSettings.getValue(JOURNAL_HEIGHT));
            journalStage.setWidth(journalWindowSettings.getValue(JOURNAL_WIDTH));
            journalStage.setX(journalWindowSettings.getValue(JOURNAL_XPOS));
            journalStage.setY(journalWindowSettings.getValue(JOURNAL_YPOS));
            journalController.restoreWindows(journalWindowSettings);
        } else {
            journalStage.setMaximized(true);
        }

        journalStage.setOnHidden(windowEvent -> {
            saveJournalWindowsToPreferences();
            journalController.shutdown();
            journalControllersList.remove(journalController);

            journalWindowSettings.setValue(CAN_DELETE, true);
            kViewEventBus.publish(JOURNAL_TOPIC,
                    new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
        });

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
            webAPI.openStageAsTab(journalStage, journalName.replace(" ", "_"));
        } else {
            journalStage.show();
        }
    }

    /**
     * Saves the current state of the journals and its windows to the application's preferences system.
     * <p>
     * This method persists all journal-related data, including:
     * <ul>
     *   <li>All open window states (via {@link JournalController#saveWindows(KometPreferences)})</li>
     *   <li>Journal metadata (topic UUID, title, directory name)</li>
     *   <li>Window geometry (width, height, x/y position)</li>
     *   <li>Author information</li>
     *   <li>Last edit timestamp</li>
     * </ul>
     * <p>
     * The preferences are stored in a hierarchical structure:
     * <pre>
     * Root Configuration Preferences
     *   └── journals
     *       └── [journal_shortened-UUID]
     *           ├── Journal metadata (UUID, title, dimensions, position, etc.)
     *           └── Window states
     * </pre>
     */
    private void saveJournalWindowsToPreferences() {
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences journalsPreferences = appPreferences.node(JOURNALS);

        // Non launched journal windows should be preserved.
        List<String> journalSubWindowFoldersFromPref = journalsPreferences.getList(JOURNAL_IDS);

        // launched (journal Controllers List) will overwrite existing window preferences.
        List<String> journalSubWindowFolders = new ArrayList<>(journalControllersList.size());
        for (JournalController controller : journalControllersList) {
            String journalSubWindowPrefFolder = controller.getJournalDirName();
            journalSubWindowFolders.add(journalSubWindowPrefFolder);

            final KometPreferences journalSubWindowPreferences = appPreferences.node(JOURNALS +
                    File.separator + journalSubWindowPrefFolder);
            controller.saveWindows(journalSubWindowPreferences);
        }

        // Make sure windows that are not summoned will not be deleted (not added to JOURNAL_NAMES)
        for (String x : journalSubWindowFolders) {
            if (!journalSubWindowFoldersFromPref.contains(x)) {
                journalSubWindowFoldersFromPref.add(x);
            }
        }
        journalsPreferences.putList(JOURNAL_IDS, journalSubWindowFoldersFromPref);

        try {
            journalsPreferences.flush();
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
        controller.setup(windowPreferences, classicKometStage);
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
            KometPreferences journalPreferences = appPreferences.node(JOURNALS);
        }
    }

    private void openImport(Window owner) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        Stage importStage = new Stage(StageStyle.TRANSPARENT);
        importStage.initOwner(owner);
        //set up ImportViewModel
        Config importConfig = new Config(ImportController.class.getResource("import.fxml"))
                .updateViewModel("importViewModel", (importViewModel) ->
                        importViewModel.setPropertyValue(VIEW_PROPERTIES,
                                windowSettings.getView().makeOverridableViewProperties()));
        JFXNode<Pane, ImportController> importJFXNode = FXMLMvvmLoader.make(importConfig);

        Pane importPane = importJFXNode.node();
        Scene importScene = new Scene(importPane, Color.TRANSPARENT);
        importStage.setScene(importScene);
        importStage.show();
    }

    private void openExport(Window owner) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        Stage exportStage = new Stage(StageStyle.TRANSPARENT);
        exportStage.initOwner(owner);
        //set up ExportViewModel
        Config exportConfig = new Config(ExportController.class.getResource("export.fxml"))
                .updateViewModel("exportViewModel", (exportViewModel) ->
                        exportViewModel.setPropertyValue(VIEW_PROPERTIES,
                                windowSettings.getView().makeOverridableViewProperties()));
        JFXNode<Pane, ExportController> exportJFXNode = FXMLMvvmLoader.make(exportConfig);

        Pane exportPane = exportJFXNode.node();
        Scene exportScene = new Scene(exportPane, Color.TRANSPARENT);
        exportStage.setScene(exportScene);
        exportStage.show();
    }

    /**
     * Prompts the user for GitHub preferences and repository information.
     * <p>
     * This method displays a dialog where the user can enter their GitHub credentials
     * and repository URL. It creates a CompletableFuture that will be resolved with
     * {@code true} if the user successfully enters valid credentials and connects,
     * or {@code false} if they cancel the operation.
     *
     * @return A CompletableFuture that completes with true if valid GitHub preferences
     *         were successfully provided by the user, or false if the user canceled the operation
     */
    private CompletableFuture<Boolean> promptForGitHubPrefs() {
        // Create a CompletableFuture that will be completed when the user makes a choice
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Show dialog on JavaFX thread
        runOnFxThread(() -> {
            GlassPane glassPane = new GlassPane(landingPageController.getRoot());

            final JFXNode<Pane, GitHubPreferencesController> githubPreferencesNode = FXMLMvvmLoader
                    .make(GitHubPreferencesController.class.getResource("github-preferences.fxml"));
            final Pane dialogPane = githubPreferencesNode.node();
            final GitHubPreferencesController controller = githubPreferencesNode.controller();
            Optional<GitHubPreferencesViewModel> githubPrefsViewModelOpt = githubPreferencesNode
                    .getViewModel("gitHubPreferencesViewModel");

            controller.getConnectButton().setOnAction(actionEvent -> {
                controller.handleConnectButtonEvent(actionEvent);
                githubPrefsViewModelOpt.ifPresent(githubPrefsViewModel -> {
                    if (githubPrefsViewModel.validProperty().get()) {
                        glassPane.removeContent(dialogPane);
                        glassPane.hide();
                        future.complete(true); // Complete with true on successful connection
                    }
                });
            });

            controller.getCancelButton().setOnAction(_ -> {
                glassPane.removeContent(dialogPane);
                glassPane.hide();
                future.complete(false); // Complete with false on cancel
            });

            glassPane.addContent(dialogPane);
            glassPane.show();
        });

        return future;
    }

    /**
     * Sets up the UI to reflect a disconnected GitHub state.
     * <p>
     * This method updates the GitHub status hyperlink in the landing page to show that
     * the application is disconnected from GitHub. When clicked, the hyperlink will
     * attempt to connect to GitHub by calling the {@link #connectToGithub()} method.
     * <p>
     * The method runs on the JavaFX application thread to ensure thread safety when
     * updating UI components.
     */
    private void gotoGitHubDisconnectedState() {
        runOnFxThread(() -> {
            if (landingPageController != null) {
                Hyperlink githubStatusHyperlink = landingPageController.getGithubStatusHyperlink();
                githubStatusHyperlink.setText("Disconnected, Select to connect");
                githubStatusHyperlink.setOnAction(event -> connectToGithub());
            }
        });
    }

    /**
     * Sets up the UI to reflect a connected GitHub state.
     * <p>
     * This method updates the GitHub status hyperlink in the landing page to show that
     * the application is successfully connected to GitHub. When clicked, the hyperlink
     * will disconnect from GitHub by calling the {@link #disconnectFromGithub()} method.
     * <p>
     * The method runs on the JavaFX application thread to ensure thread safety when
     * updating UI components.
     */
    private void gotoGitHubConnectedState() {
        runOnFxThread(() -> {
            if (landingPageController != null) {
                Hyperlink githubStatusHyperlink = landingPageController.getGithubStatusHyperlink();
                githubStatusHyperlink.setText("Connected");
                githubStatusHyperlink.setOnAction(event -> disconnectFromGithub());
            }
        });
    }

    /**
     * Executes a Git task, ensuring preferences are valid first.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Verifies that the data store root is available</li>
     *   <li>Creates a changeset folder if it doesn't exist</li>
     *   <li>Validates GitHub preferences and prompts for them if missing</li>
     *   <li>Creates and runs the appropriate GitTask based on the operation mode</li>
     * </ol>
     * If GitHub preferences are missing or invalid, this method will prompt the user
     * to enter them before proceeding with the requested operation.
     *
     * @param mode The operation mode (CONNECT, PULL, or SYNC) that determines what
     *             Git operations will be performed
     */
    private void executeGitTask(GitTask.OperationMode mode) {
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        if (optionalDataStoreRoot.isEmpty()) {
            LOG.error("ServiceKeys.DATA_STORE_ROOT not provided.");
            return;
        }

        final File changeSetFolder = new File(optionalDataStoreRoot.get(), CHANGESETS_DIR);
        if (!changeSetFolder.exists()) {
            if (!changeSetFolder.mkdirs()) {
                LOG.error("Unable to create {} directory", CHANGESETS_DIR);
                return;
            }
        }

        // Check if GitHub preferences are valid first
        if (!gitHubPreferencesDao.validate()) {
            LOG.info("GitHub preferences missing or incomplete. Prompting user...");

            // Prompt for preferences before proceeding
            promptForGitHubPrefs().thenAccept(confirmed -> {
                if (confirmed) {
                    // Preferences entered successfully, now run the GitTask
                    createAndRunGitTask(mode, changeSetFolder);
                } else {
                    LOG.info("User cancelled the GitHub preferences dialog");
                }
            });
        } else {
            // Preferences already valid, run the GitTask directly
            createAndRunGitTask(mode, changeSetFolder);
        }
    }

    /**
     * Creates and runs a GitTask with the specified operation mode.
     * <p>
     * This helper method is called after GitHub preferences have been validated. It:
     * <ol>
     *   <li>Creates a new GitTask with the specified operation mode</li>
     *   <li>Registers a success callback to update the UI state</li>
     *   <li>Runs the task with progress tracking</li>
     *   <li>Handles errors and updates the UI accordingly</li>
     * </ol>
     * The task is executed asynchronously through the ProgressHelper service to
     * provide user feedback during long-running operations.
     *
     * @param operationMode The operation mode specifying which Git operations to perform
     * @param changeSetFolder The folder where the Git repository is located
     * @return A CompletableFuture that completes with true if the operation was successful,
     *         or false if it failed or was cancelled
     */
    private CompletableFuture<Boolean> createAndRunGitTask(GitTask.OperationMode operationMode, File changeSetFolder) {
        // Create a GitTask with only the connection success callback
        GitTask gitTask = new GitTask(operationMode, changeSetFolder.toPath(), this::gotoGitHubConnectedState);

        // Run the task
        return ProgressHelper.progress(LANDING_PAGE_TOPIC, gitTask)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        LOG.error("Error during {} operation", operationMode, throwable);
                        disconnectFromGithub();
                    } else if (!result) {
                        LOG.warn("{} operation did not complete successfully", operationMode);
                        disconnectFromGithub();
                    }
                });
    }

    /**
     * Initiates a connection to GitHub.
     * <p>
     * This method establishes a connection to GitHub by executing a GitTask in CONNECT mode.
     * If successful, the UI will be updated to reflect the connected state, and the local
     * Git repository will be initialized and configured with the remote origin.
     * <p>
     * If GitHub preferences are missing or invalid, the user will be prompted to
     * enter them before the connection is established.
     */
    private void connectToGithub() {
        LOG.info("Attempting to connect to GitHub...");
        executeGitTask(CONNECT);
    }

    /**
     * Disconnects from GitHub and cleans up local resources.
     * <p>
     * This method performs the following cleanup operations:
     * <ol>
     *   <li>Logs the disconnection attempt</li>
     *   <li>Removes all GitHub-related preferences from user preferences</li>
     *   <li>Deletes the local .git repository folder if it exists</li>
     *   <li>Deletes the README.md file if it exists</li>
     *   <li>Updates the UI to reflect the disconnected state</li>
     * </ol>
     * If any errors occur during this process, they are logged but do not prevent
     * the disconnection from completing.
     */
    private void disconnectFromGithub() {
        LOG.info("Disconnecting from GitHub...");

        // Delete stored user preferences related to GitHub
        try {
            gitHubPreferencesDao.delete();
            LOG.info("Successfully deleted GitHub preferences");
        } catch (BackingStoreException e) {
            LOG.error("Failed to delete GitHub preferences", e);
        }

        // Delete the .git folder and README.md if they exist
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        if (optionalDataStoreRoot.isPresent()) {
            final File changeSetFolder = new File(optionalDataStoreRoot.get(), CHANGESETS_DIR);

            // Delete .git folder
            final File gitDir = new File(changeSetFolder, ".git");
            if (gitDir.exists() && gitDir.isDirectory()) {
                try {
                    FileUtils.delete(gitDir, FileUtils.RECURSIVE);
                    LOG.info("Successfully deleted .git folder at: {}", gitDir.getAbsolutePath());
                } catch (IOException e) {
                    LOG.error("Failed to delete .git folder at: {}", gitDir.getAbsolutePath(), e);
                }
            }

            // Delete README.md file
            final File readmeFile = new File(changeSetFolder, README_FILENAME);
            if (readmeFile.exists() && readmeFile.isFile()) {
                try {
                    if (readmeFile.delete()) {
                        LOG.info("Successfully deleted {} file at: {}", README_FILENAME, readmeFile.getAbsolutePath());
                    } else {
                        LOG.error("Failed to delete {} file at: {}", README_FILENAME, readmeFile.getAbsolutePath());
                    }
                } catch (SecurityException e) {
                    LOG.error("Security exception while deleting {} file at: {}", readmeFile.getAbsolutePath(), e);
                }
            }
        } else {
            LOG.warn("Could not access data store root to delete .git folder and README.md");
        }

        // Update the UI state
        gotoGitHubDisconnectedState();
    }

    /**
     * Displays information about the current Git repository.
     * <p>
     * This method checks if a Git repository exists and displays basic information about it.
     * If no repository exists or is not properly configured, the user will be prompted to
     * enter GitHub preferences before proceeding. Upon successful connection to GitHub,
     * repository information will be fetched and displayed in a dialog.
     * <p>
     * The method performs the following operations:
     * <ol>
     *   <li>Verifies that the data store root is available</li>
     *   <li>Checks if a Git repository exists in the changeset folder</li>
     *   <li>If no repository exists, prompts for GitHub preferences and initiates connection</li>
     *   <li>Fetches and displays repository information</li>
     * </ol>
     */
    private void infoAction() {
        Optional<File> optionalDataStoreRoot = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT);
        if (optionalDataStoreRoot.isEmpty()) {
            LOG.error("ServiceKeys.DATA_STORE_ROOT not provided.");
            return;
        }

        final File changeSetFolder = new File(optionalDataStoreRoot.get(), CHANGESETS_DIR);
        final File gitDir = new File(changeSetFolder, ".git");

        if (gitDir.exists()) {
            fetchAndShowRepositoryInfo(changeSetFolder);
        } else {
            // Prompt for preferences before proceeding
            promptForGitHubPrefs().thenCompose(confirmed -> {
                if (confirmed) {
                    // Preferences entered successfully, now run the GitTask
                    return createAndRunGitTask(CONNECT, changeSetFolder);
                } else {
                    return CompletableFuture.completedFuture(false);
                }
            }).thenAccept(confirmed -> {
                if (confirmed) {
                    fetchAndShowRepositoryInfo(changeSetFolder);
                }
            });
        }
    }

    /**
     * Fetches repository information and displays it in a dialog.
     * <p>
     * This method asynchronously retrieves information about the Git repository
     * located in the specified folder using an {@code InfoTask}, then displays
     * the results in a dialog. The operation is performed on a background thread
     * to avoid blocking the UI.
     *
     * @param changeSetFolder The repository folder to fetch information from
     */
    private void fetchAndShowRepositoryInfo(File changeSetFolder) {
        CompletableFuture.supplyAsync(() -> {
                    try {
                        InfoTask task = new InfoTask(changeSetFolder.toPath());
                        return task.call();
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to fetch repository information", ex);
                    }
                }, TinkExecutor.threadPool())
                .thenCompose(repoInfo -> showRepositoryInfoDialog(repoInfo)
                        .thenAccept(confirmed -> {
                            if (confirmed) {
                                LOG.info("User closed the repository info dialog");
                            }
                        }));
    }

    /**
     * Displays the repository information dialog.
     * <p>
     * This method creates and displays a dialog showing Git repository information
     * including URL, username, email, and status. The dialog is displayed using a
     * glass pane overlay on top of the landing page.
     * <p>
     * The method returns a CompletableFuture that will be completed when the user
     * closes the dialog.
     *
     * @param repoInfo Map containing repository information with keys defined in {@code GitPropertyName}
     * @return A CompletableFuture that completes with {@code true} when the user closes the dialog
     */
    private CompletableFuture<Boolean> showRepositoryInfoDialog(Map<GitPropertyName, String> repoInfo) {
        // Create a CompletableFuture that will be completed when the user makes a choice
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Show dialog on JavaFX thread
        runOnFxThread(() -> {
            GlassPane glassPane = new GlassPane(landingPageController.getRoot());

            final JFXNode<Pane, GitHubInfoController> githubInfoNode = FXMLMvvmLoader
                    .make(GitHubInfoController.class.getResource("github-info.fxml"));
            final Pane dialogPane = githubInfoNode.node();
            final GitHubInfoController controller = githubInfoNode.controller();

            controller.getGitUrlTextField().setText(repoInfo.get(GIT_URL));
            controller.getGitUsernameTextField().setText(repoInfo.get(GIT_USERNAME));
            controller.getGitEmailTextField().setText(repoInfo.get(GIT_EMAIL));
            controller.getStatusTextArea().setText(repoInfo.get(GIT_STATUS));

            controller.getCloseButton().setOnAction(_ -> {
                glassPane.removeContent(dialogPane);
                glassPane.hide();
                future.complete(true); // Complete with true on close
            });

            glassPane.addContent(dialogPane);
            glassPane.show();
        });

        return future;
    }

    private void generateMsWindowsMenu(BorderPane kometRoot, Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem about = new MenuItem("About");
        about.setOnAction(_ -> showAboutDialog());
        fileMenu.getItems().add(about);

        // Importing data
        MenuItem importMenuItem = new MenuItem("Import Dataset");
        importMenuItem.setOnAction(actionEvent -> openImport(stage));
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

    private void showAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.showAndWait();
    }

    private void quit() {
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
        about.setOnAction(_ -> showAboutDialog());
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

        Menu exchangeMenu = createExchangeMenu();

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(viewMenu);
        menuBar.getMenus().add(windowMenu);
        menuBar.getMenus().add(exchangeMenu);
        landingPageRoot.setTop(menuBar);
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
