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
import dev.ikm.komet.framework.events.Evt;
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
import static dev.ikm.komet.framework.events.FrameworkTopics.IMPORT_TOPIC;
import static dev.ikm.komet.framework.events.FrameworkTopics.LANDING_PAGE_TOPIC;
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
public class WebApp extends Application implements AppInterface  {

    private static final Logger LOG = LoggerFactory.getLogger(WebApp.class);
    public static final String ICON_LOCATION = "/icons/Komet.png";
    public static final SimpleObjectProperty<AppState> state = new SimpleObjectProperty<>(STARTING);
    public static final SimpleObjectProperty<User> userProperty = new SimpleObjectProperty<>();

    private static Stage primaryStage;

    private static WebAPI webAPI;
    static final boolean IS_BROWSER = WebAPI.isBrowser();
    static final boolean IS_DESKTOP = !IS_BROWSER && PlatformUtils.isDesktop();
    static final boolean IS_MAC = !IS_BROWSER && PlatformUtils.isMac();
    static final boolean IS_MAC_AND_NOT_TESTFX_TEST = IS_MAC && !isTestFXTest();
    private final StackPane rootPane = createRootPane();
    private Image appIcon;
    private LandingPageController landingPageController;
    private EvtBus kViewEventBus;

    AppGithub appGithub;
    AppClassicKomet appClassicKomet;
    AppMenu appMenu;

    @Override
    public AppGithub getAppGithub() {
        return appGithub;
    }

    @Override
    public AppMenu getAppMenu() {
        return appMenu;
    }

    @Override
    public AppClassicKomet getAppClassicKomet() {
        return appClassicKomet;
    }

    @Override
    public WebAPI getWebAPI() {
        return webAPI;
    }

    @Override
    public Image getAppIcon() {
        return appIcon;
    }
    @Override
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    @Override
    public SimpleObjectProperty<AppState> getState() {
        return state;
    }
    @Override
    public LandingPageController getLandingPageController() {
        return landingPageController;
    }

    @Override
    public GitHubPreferencesDao getGitHubPreferencesDao() {
        return gitHubPreferencesDao;
    }

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
        appGithub = new AppGithub(this);
        appClassicKomet = new AppClassicKomet(this);
        appMenu = new AppMenu(this);

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

        appGithub.disconnectFromGithub();

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

    private void launchLoginPage(Stage stage) {
        JFXNode<BorderPane, Void> loginNode = FXMLMvvmLoader.make(
                LoginPageController.class.getResource("login-page.fxml"));
        BorderPane loginPane = loginNode.node();
        rootPane.getChildren().setAll(loginPane);
        stage.setTitle("KOMET Login");

        appMenu.setupMenus();
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

            appMenu.setupMenus();
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

    public void launchLandingPage(Stage stage, User user) {
        try {
            rootPane.getChildren().clear(); // Clear the root pane before adding new content

            KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
            KometPreferences windowPreferences = appPreferences.node("main-komet-window");
            WindowSettings windowSettings = new WindowSettings(windowPreferences);

            FXMLLoader landingPageLoader = LandingPageViewFactory.createFXMLLoader();
            BorderPane landingPageBorderPane = landingPageLoader.load();

            if (!IS_MAC) {
                appMenu.createMenuOptions(landingPageBorderPane);
            }

            landingPageController = landingPageLoader.getController();
            landingPageController.getWelcomeTitleLabel().setText("Welcome " + user.getName());
            landingPageController.setSelectedDatasetTitle(PrimitiveData.get().name());
            landingPageController.getGithubStatusHyperlink().setOnAction(_ -> appGithub.connectToGithub());

            stage.setTitle("Landing Page");
            stage.setMaximized(true);
            stage.setOnCloseRequest(windowEvent -> {
                // This is called only when the user clicks the close button on the window
                state.set(SHUTDOWN);
                landingPageController.cleanup();
            });

            rootPane.getChildren().add(landingPageBorderPane);

            getAppMenu().setupMenus();
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
            appMenu.generateMsWindowsMenu(journalBorderPane, journalStage);
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

    public void openImport(Stage owner) {
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

    public void openExport(Stage owner) {
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

    public void quit() {
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

    public enum AppKeys {
        APP_INITIALIZED
    }
}
