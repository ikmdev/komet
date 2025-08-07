package dev.ikm.komet.app;

import dev.ikm.komet.app.aboutdialog.AboutDialog;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.app.WebApp.IS_BROWSER;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask.OperationMode.PULL;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitTask.OperationMode.SYNC;

public class AppMenu {

    private final AppInterface app;

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
        importMenuItem.setOnAction(actionEvent -> app.openImport(stage));
        fileMenu.getItems().add(importMenuItem);

        // Exporting data
        MenuItem exportDatasetMenuItem = new MenuItem("Export Dataset");
        exportDatasetMenuItem.setOnAction(actionEvent -> app.openExport(stage));
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
}
