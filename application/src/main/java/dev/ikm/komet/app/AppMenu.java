package dev.ikm.komet.app;

import de.jangassen.MenuToolkit;
import de.jangassen.model.AppearanceMode;
import dev.ikm.komet.app.aboutdialog.AboutDialog;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.preferences.KometPreferencesStage;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.mvvm.view.changeset.ExportController;
import dev.ikm.komet.kview.mvvm.view.changeset.ImportController;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.app.AppState.RUNNING;
import static dev.ikm.komet.app.WebApp.IS_BROWSER;
import static dev.ikm.komet.app.WebApp.IS_MAC_AND_NOT_TESTFX_TEST;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask.OperationMode.PULL;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask.OperationMode.SYNC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;

public class AppMenu {

    private static final Logger LOG = LoggerFactory.getLogger(AppMenu.class);

    private final AppInterface app;
    KometPreferencesStage kometPreferencesStage;

    private static long windowCount = 1;

    public AppMenu(AppInterface app) {
        this.app = app;
    }

    void generateMsWindowsMenu(BorderPane kometRoot, Stage stage) {
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
        menuItemQuit.setOnAction(actionEvent -> app.quit());
        menuItemQuit.setAccelerator(quitKeyCombo);
        fileMenu.getItems().add(menuItemQuit);

        Menu editMenu = new Menu("Edit");
        MenuItem landingPage = new MenuItem("Landing Page");
        KeyCombination landingPageKeyCombo = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
        landingPage.setOnAction(actionEvent -> app.launchLandingPage(app.getPrimaryStage(), null /* userProperty.get()*/));
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

    Menu createExchangeMenu() {
        Menu exchangeMenu = new Menu("Exchange");

        MenuItem infoMenuItem = new MenuItem("Info");
        infoMenuItem.setOnAction(actionEvent -> app.getAppGithub().infoAction());
        MenuItem pullMenuItem = new MenuItem("Pull");
        pullMenuItem.setOnAction(actionEvent -> app.getAppGithub().executeGitTask(PULL));
        MenuItem pushMenuItem = new MenuItem("Sync");
        pushMenuItem.setOnAction(actionEvent -> app.getAppGithub().executeGitTask(SYNC));

        exchangeMenu.getItems().addAll(infoMenuItem, pullMenuItem, pushMenuItem);
        return exchangeMenu;
    }

    public void showAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.showAndWait();
    }

    public void createMenuOptions(BorderPane landingPageRoot) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem about = new MenuItem("About");
        about.setOnAction(_ -> showAboutDialog());
        fileMenu.getItems().add(about);

        MenuItem menuItemQuit = new MenuItem("Quit");
        KeyCombination quitKeyCombo = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        menuItemQuit.setOnAction(actionEvent -> app.quit());
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
                app.getAppClassicKomet().launchClassicKomet();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }
        });
        classicKometMenuItem.setAccelerator(classicKometKeyCombo);
        return classicKometMenuItem;
    }


    void setupMenus() {
        Menu kometAppMenu;

        if (IS_MAC_AND_NOT_TESTFX_TEST) {
            MenuToolkit menuToolkit = MenuToolkit.toolkit();
            kometAppMenu = menuToolkit.createDefaultApplicationMenu("Komet");
        } else {
            kometAppMenu = new Menu("Komet");
        }

        MenuItem prefsItem = new MenuItem("Komet preferences...");
        prefsItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
        prefsItem.setOnAction(event -> kometPreferencesStage.showPreferences());

        if (IS_MAC_AND_NOT_TESTFX_TEST) {
            kometAppMenu.getItems().add(2, prefsItem);
            kometAppMenu.getItems().add(3, new SeparatorMenuItem());
            MenuItem appleQuit = kometAppMenu.getItems().getLast();
            appleQuit.setOnAction(event -> app.quit());
        } else {
            kometAppMenu.getItems().addAll(prefsItem, new SeparatorMenuItem());
        }

        MenuBar menuBar = new MenuBar(kometAppMenu);

        if (app.getState().get() == RUNNING) {
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
        importDatasetMenuItem.setOnAction(actionEvent -> openImport(app.getPrimaryStage()));

        // Export Dataset Menu Item
        MenuItem exportDatasetMenuItem = new MenuItem("Export Dataset...");
        exportDatasetMenuItem.setOnAction(actionEvent -> openExport(app.getPrimaryStage()));

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
                app.getAppClassicKomet().launchClassicKomet();
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

    private Menu createHelpMenu() {
        Menu helpMenu = new Menu("Help");
        helpMenu.getItems().add(new MenuItem("Getting started"));
        return helpMenu;
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

    private static void createNewStage() {
        Stage stage = new Stage();
        stage.setScene(new Scene(new StackPane()));
        stage.setTitle("New stage" + " " + (windowCount++));
        stage.show();
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

}
