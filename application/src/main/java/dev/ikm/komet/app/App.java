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
import dev.ikm.komet.kview.mvvm.view.export.ExportController;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.view.journal.JournalViewFactory;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageController;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageViewFactory;
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
import static dev.ikm.komet.app.util.CssFile.*;
import static dev.ikm.komet.app.util.CssUtils.*;
import static dev.ikm.komet.framework.KometNodeFactory.KOMET_NODES;
import static dev.ikm.komet.framework.window.WindowSettings.Keys.*;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.preferences.JournalWindowPreferences.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.*;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final String OS_NAME_MAC = "mac";

    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    public static final SimpleObjectProperty<AppState> state = new SimpleObjectProperty<>(STARTING);
    private static Stage primaryStage;

    private static Stage classicKometStage;
    private static long windowCount = 1;
    private static KometPreferencesStage kometPreferencesStage;

    /**
     * An entry point to launch the newer UI panels.
     */
    private MenuItem createJournalViewMenuItem;

    /**
     * This is a list of new windows that have been launched. During shutdown, the application close each stage gracefully.
     */
    private static Stage landingPageWindow;
    private List<JournalController> journalControllersList = new ArrayList<>();

    private EvtBus kViewEventBus;

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "false");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Komet");
        // https://stackoverflow.com/questions/42598097/using-javafx-application-stop-method-over-shutdownhook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Starting shutdown hook");
            PrimitiveData.save();
            PrimitiveData.stop();
            LOG.info("Finished shutdown hook");
        }));
        launch();
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

    public void init() throws Exception {
        /*
"/" the local pathname separator
"%t" the system temporary directory
"%h" the value of the "user.home" system property
"%g" the generation number to distinguish rotated logs
"%u" a unique number to resolve conflicts
"%%" translates to a single percent sign "%"
         */
//        String pattern = "%h/Solor/komet/logs/komet%g.log";
//        int fileSizeLimit = 1024 * 1024; //the maximum number of bytes to write to any one file
//        int fileCount = 10;
//        boolean append = true;
//
//        FileHandler fileHandler = new FileHandler(pattern,
//                fileSizeLimit,
//                fileCount,
//                append);

//        File logDirectory = new File(System.getProperty("user.home"), "Solor/komet/logs");
//        logDirectory.mkdirs();
        LOG.info("Starting Komet");
        LoadFonts.load();

        // get the instance of the event bus
        kViewEventBus = EvtBusFactory.getInstance(EvtBus.class);
        Subscriber<CreateJournalEvent> detailsSubscriber = evt -> {

            String journalName = evt.getWindowSettingsObjectMap().getValue(JOURNAL_TITLE);
            // Inspects the existing journal windows to see if it is already open
            // So that we do not open duplicate journal windows
            journalControllersList.stream()
                    .filter(journalController -> journalController.getTitle().equals(journalName))
                    .findFirst()
                    .ifPresentOrElse(
                            journalController -> journalController.windowToFront(), /* Window already launched now make window to the front (so user sees window) */
                            () -> launchJournalViewWindow(evt.getWindowSettingsObjectMap()) /* launch new Journal view window */
                    );
        };
        // subscribe to the topic
        kViewEventBus.subscribe(JOURNAL_TOPIC, CreateJournalEvent.class, detailsSubscriber);
    }

    @Override
    public void start(Stage stage) {

        try {
            App.primaryStage = stage;
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> AlertStreams.getRoot().dispatch(AlertObject.makeError(e)));
            // Get the toolkit
            MenuToolkit tk = MenuToolkit.toolkit();
            Menu kometAppMenu = tk.createDefaultApplicationMenu("Komet");

            MenuItem prefsItem = new MenuItem("Komet preferences...");
            prefsItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
            prefsItem.setOnAction(event -> App.kometPreferencesStage.showPreferences());

            kometAppMenu.getItems().add(2, prefsItem);
            kometAppMenu.getItems().add(3, new SeparatorMenuItem());
            MenuItem appleQuit = kometAppMenu.getItems().getLast();
            appleQuit.setOnAction(event -> quit());

            tk.setApplicationMenu(kometAppMenu);

            // File Menu
            Menu fileMenu = new Menu("File");

            MenuItem importDatasetMenuItem = new MenuItem("Import Dataset");
            importDatasetMenuItem.setOnAction(actionEvent -> doImportDataSet(primaryStage));

            // Exporting data
            MenuItem exportDatasetMenuItem = new MenuItem("Export Dataset");
            exportDatasetMenuItem.setOnAction(actionEvent -> openExport());
            fileMenu.getItems().add(exportDatasetMenuItem);

            fileMenu.getItems().addAll(importDatasetMenuItem, exportDatasetMenuItem, new SeparatorMenuItem(), tk.createCloseWindowMenuItem());

            // Edit
            Menu editMenu = new Menu("Edit");
            editMenu.getItems().addAll(createMenuItem("Undo"), createMenuItem("Redo"), new SeparatorMenuItem(),
                    createMenuItem("Cut"), createMenuItem("Copy"), createMenuItem("Paste"), createMenuItem("Select All"));

            // View
            Menu viewMenu = new Menu("View");
            MenuItem classicKometPage = new MenuItem("Classic Komet");
            KeyCombination classicKometPageKeyCombo = new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN);
            classicKometPage.setOnAction(actionEvent -> {
                try {
                    launchClassicKomet();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (BackingStoreException e) {
                    throw new RuntimeException(e);
                }
            });
            classicKometPage.setAccelerator(classicKometPageKeyCombo);
            viewMenu.getItems().add(classicKometPage);

            // Window Menu
            Menu windowMenu = new Menu("Window");
            windowMenu.getItems().addAll(tk.createMinimizeMenuItem(), tk.createZoomMenuItem(), tk.createCycleWindowsItem(),
                    new SeparatorMenuItem(), tk.createBringAllToFrontItem());

            // Help Menu
            Menu helpMenu = new Menu("Help");
            helpMenu.getItems().addAll(new MenuItem("Getting started"));

            MenuBar bar = new MenuBar();
            bar.getMenus().addAll(kometAppMenu, fileMenu, editMenu, viewMenu, windowMenu, helpMenu);
            tk.setAppearanceMode(AppearanceMode.AUTO);
            tk.setDockIconMenu(createDockMenu());
            tk.autoAddWindowMenuItems(windowMenu);


            if(System.getProperty("os.name")!=null && System.getProperty("os.name").toLowerCase().startsWith(OS_NAME_MAC)) {
                tk.setGlobalMenuBar(bar);
            }

            tk.setTrayMenu(createSampleMenu());

            FXMLLoader sourceLoader = new FXMLLoader(getClass().getResource("SelectDataSource.fxml"));
            BorderPane sourceRoot = sourceLoader.load();
            SelectDataSourceController selectDataSourceController = sourceLoader.getController();
            selectDataSourceController.getCancelButton().setOnAction(actionEvent -> Platform.exit());
            Scene sourceScene = new Scene(sourceRoot);
            addStylesheets(sourceScene, KOMET_CSS, KVIEW_CSS);

            stage.setScene(sourceScene);
            stage.setTitle("KOMET Startup");

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

            // Ensure app is shutdown gracefully. Once state changes it calls appStateChangeListener.
            stage.setOnCloseRequest(windowEvent -> {
                state.set(SHUTDOWN);
            });
            stage.show();
            state.set(AppState.SELECT_DATA_SOURCE);
            state.addListener(this::appStateChangeListener);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            Platform.exit();
        }
    }

    private void doImportDataSet(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        // selectedFile is null if the user clicks cancel
        if (selectedFile != null) {
            LoadEntitiesFromProtobufFile loadEntities = new LoadEntitiesFromProtobufFile(selectedFile);
            ProgressHelper.progress(loadEntities, "Cancel Import");
        }
    }

    private void launchLandingPage() {
        if (landingPageWindow != null) {
            App.primaryStage = landingPageWindow;
            landingPageWindow.show();
            landingPageWindow.toFront();
            landingPageWindow.setMaximized(true);
            return;
        }
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node("main-komet-window");
        WindowSettings windowSettings = new WindowSettings(windowPreferences);

        Stage kViewStage = new Stage();

        try {
            FXMLLoader landingPageLoader = LandingPageViewFactory.createFXMLLoader();
            BorderPane landingPageBorderPane = landingPageLoader.load();
            // if NOT on Mac OS
            if(System.getProperty("os.name")!=null && !System.getProperty("os.name").toLowerCase().startsWith(OS_NAME_MAC)) {
                createMenuOptions(landingPageBorderPane);
            }
            LandingPageController landingPageController = landingPageLoader.getController();
            Scene sourceScene = new Scene(landingPageBorderPane, 1200, 800);

            kViewStage.setScene(sourceScene);
            kViewStage.setTitle("Landing Page");

            kViewStage.setMaximized(true);
            kViewStage.setOnCloseRequest(windowEvent -> {
                // call shutdown method on the view
                state.set(SHUTDOWN);
                landingPageController.cleanup();
                landingPageWindow.close();
                landingPageWindow = null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Launch windows window pane inside journal view
        kViewStage.setOnShown(windowEvent -> {
            // do stuff when shown.
        });

        landingPageWindow = kViewStage;
        kViewStage.show();
        kViewStage.setMaximized(true);
    }

    /**
     * When a user selects the menu option View/New Journal a new Stage Window is launched.
     * This method will load a navigation panel to be a publisher and windows will be connected (subscribed) to the activity stream.
     * @param journalWindowSettings if present will give the size and positioning of the journal window
     */
    private void launchJournalViewWindow(PrefX journalWindowSettings) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);

        WindowSettings windowSettings = new WindowSettings(windowPreferences);

        Stage journalStageWindow = new Stage();
        FXMLLoader journalLoader = JournalViewFactory.createFXMLLoader();
        JournalController journalController;
        try {
            BorderPane journalBorderPane = journalLoader.load();
            journalController = journalLoader.getController();
            Scene sourceScene = new Scene(journalBorderPane, 1200, 800);
            addStylesheets(sourceScene, KOMET_CSS, KVIEW_CSS);

            journalStageWindow.setScene(sourceScene);

            // if NOT on Mac OS
            if(System.getProperty("os.name")!=null && !System.getProperty("os.name").toLowerCase().startsWith(OS_NAME_MAC)) {
                generateMsWindowsMenu(journalBorderPane, journalStageWindow);
            }

            String journalName;
            if (journalWindowSettings != null) {
                // load journal specific window settings
                journalName = journalWindowSettings.getValue(JOURNAL_TITLE);
                journalStageWindow.setTitle(journalName);
                if (journalWindowSettings.getValue(JOURNAL_HEIGHT) != null) {
                    journalStageWindow.setHeight(journalWindowSettings.getValue(JOURNAL_HEIGHT));
                    journalStageWindow.setWidth(journalWindowSettings.getValue(JOURNAL_WIDTH));
                    journalStageWindow.setX(journalWindowSettings.getValue(JOURNAL_XPOS));
                    journalStageWindow.setY(journalWindowSettings.getValue(JOURNAL_YPOS));
                    journalController.recreateConceptWindows(journalWindowSettings);
                }else{
                    journalStageWindow.setMaximized(true);
                }
            }

            journalStageWindow.setOnCloseRequest(windowEvent -> {
                saveJournalWindowsToPreferences();
                // call shutdown method on the view
                journalController.shutdown();
                journalControllersList.remove(journalController);
                // enable Delete menu option
                journalWindowSettings.setValue(CAN_DELETE, true);
                kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        journalController.setWindowView(windowSettings.getView());

        // Launch windows window pane inside journal view
        journalStageWindow.setOnShown(windowEvent -> {
            //TODO: Refactor factory constructor calls below to use PluggableService (make constructors private)
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
        journalStageWindow.show();
    }

    private void saveJournalWindowsToPreferences() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences journalPreferences = appPreferences.node(JOURNAL_WINDOW);

        // Non launched journal windows should be preserved.
        List<String> journalSubWindowFoldersFromPref = journalPreferences.getList(JOURNAL_NAMES);

        // launched (journal Controllers List) will overwrite existing window preferences.
        List<String> journalSubWindowFolders = new ArrayList<>(journalControllersList.size());
        for(JournalController controller : journalControllersList) {
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
        for (String x : journalSubWindowFolders){
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

    @Override
    public void stop() {
        LOG.info("Stopping application\n\n###############\n\n");

        // close all journal windows
        journalControllersList.forEach(journalController -> journalController.close());
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
                    primaryStage.hide();
                    launchLandingPage();
                }
                case SHUTDOWN -> {
                    quit();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    private void launchClassicKomet() throws IOException, BackingStoreException {
        // If already launched bring to the front
        if (classicKometStage != null && classicKometStage.isShowing()) {
            classicKometStage.show();
            classicKometStage.toFront();
            return;
        }
        classicKometStage = new Stage();
        //Starting up preferences and getting configurations
        Preferences.start();
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        boolean appInitialized = appPreferences.getBoolean(AppKeys.APP_INITIALIZED, false);
        if (appInitialized) {
            LOG.info("Restoring configuration preferences. ");
        } else {
            LOG.info("Creating new configuration preferences. ");
        }

        MainWindowRecord mainWindowRecord = MainWindowRecord.make();

        BorderPane kometRoot = mainWindowRecord.root();
        KometStageController controller = mainWindowRecord.controller();

        //Loading/setting the Komet screen
        Scene kometScene = new Scene(kometRoot, 1800, 1024);
        addStylesheets(kometScene, KOMET_CSS);

        // if NOT on Mac OS
        if(System.getProperty("os.name")!=null && !System.getProperty("os.name").toLowerCase().startsWith(OS_NAME_MAC)) {
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
            windowPreferences.get(LEFT_TAB_PREFERENCES).ifPresent(leftTabPreferencesName -> {
                restoreTab(windowPreferences, leftTabPreferencesName, controller.windowView(), node -> controller.leftBorderPaneSetCenter(node));
            });
            windowPreferences.get(CENTER_TAB_PREFERENCES).ifPresent(centerTabPreferencesName -> {
                restoreTab(windowPreferences, centerTabPreferencesName, controller.windowView(), node -> controller.centerBorderPaneSetCenter(node));
            });
            windowPreferences.get(RIGHT_TAB_PREFERENCES).ifPresent(rightTabPreferencesName -> {
                restoreTab(windowPreferences, rightTabPreferencesName, controller.windowView(), node -> controller.rightBorderPaneSetCenter(node));
            });
        }
        //Setting X and Y coordinates for location of the Komet stage
        classicKometStage.setX(controller.windowSettings().xLocationProperty().get());
        classicKometStage.setY(controller.windowSettings().yLocationProperty().get());
        classicKometStage.setHeight(controller.windowSettings().heightProperty().get());
        classicKometStage.setWidth(controller.windowSettings().widthProperty().get());
        classicKometStage.show();

        App.kometPreferencesStage = new KometPreferencesStage(controller.windowView().makeOverridableViewProperties());

        windowPreferences.sync();
        appPreferences.sync();
        if (createJournalViewMenuItem != null) {
            createJournalViewMenuItem.setDisable(false);
            KeyCombination newJournalKeyCombo = new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN);
            createJournalViewMenuItem.setAccelerator(newJournalKeyCombo);
            KometPreferences journalPreferences = appPreferences.node(JOURNAL_WINDOW);
        }
    }

    private void openExport() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
        WindowSettings windowSettings = new WindowSettings(windowPreferences);
        Stage exportStage = new Stage();
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
        about.setOnAction(actionEvent -> showWindowsAboutScreen());
        fileMenu.getItems().add(about);

        MenuItem importMenuItem = new MenuItem("Import Dataset");
        importMenuItem.setOnAction(actionEvent -> doImportDataSet(stage));
        fileMenu.getItems().add(importMenuItem);

        // Exporting data
        MenuItem exportDatasetMenuItem = new MenuItem("Export Dataset");
        exportDatasetMenuItem.setOnAction(actionEvent -> openExport());
        fileMenu.getItems().add(exportDatasetMenuItem);

        MenuItem menuItemQuit = new MenuItem("Quit");
        KeyCombination quitKeyCombo = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        menuItemQuit.setOnAction(actionEvent -> quit());
        menuItemQuit.setAccelerator(quitKeyCombo);
        fileMenu.getItems().add(menuItemQuit);

        Menu editMenu = new Menu("Edit");
        MenuItem landingPage = new MenuItem("Landing Page");
        KeyCombination landingPageKeyCombo = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
        landingPage.setOnAction(actionEvent -> launchLandingPage());
        landingPage.setAccelerator(landingPageKeyCombo);
        editMenu.getItems().add(landingPage);

        Menu windowMenu = new Menu("Window");
        MenuItem minimizeWindow = new MenuItem("Minimize");
        KeyCombination minimizeKeyCombo = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);
        minimizeWindow.setOnAction(event -> {
            Stage obj = (Stage) kometRoot.getScene().getWindow();
            obj.setIconified(true);
        });
        minimizeWindow.setAccelerator(minimizeKeyCombo);
        windowMenu.getItems().add(minimizeWindow);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(editMenu);
        menuBar.getMenus().add(windowMenu);
        //hBox.getChildren().add(menuBar);
        Platform.runLater(() -> kometRoot.setTop(menuBar));
    }

    private void showWindowsAboutScreen() {
        Stage aboutWindow = new Stage();
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
        about.setOnAction(actionEvent -> showWindowsAboutScreen());
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