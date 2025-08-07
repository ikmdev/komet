package dev.ikm.komet.app;

import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.view.login.LoginPageController;
import dev.ikm.komet.navigator.graph.GraphNavigatorNodeFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.search.SearchNodeFactory;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static dev.ikm.komet.app.App.IS_BROWSER;
import static dev.ikm.komet.app.App.IS_MAC;
import static dev.ikm.komet.app.util.CssFile.KOMET_CSS;
import static dev.ikm.komet.app.util.CssFile.KVIEW_CSS;
import static dev.ikm.komet.app.util.CssUtils.addStylesheets;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.JournalViewModel.WINDOW_VIEW;
import static dev.ikm.komet.preferences.JournalWindowPreferences.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.CAN_DELETE;

public class AppPages {
    private static final Logger LOG = LoggerFactory.getLogger(AppGithub.class);

    private final AppInterface app;

    public AppPages(AppInterface app) {
        this.app = app;
    }


    void launchLoginPage(Stage stage) {
        JFXNode<BorderPane, Void> loginNode = FXMLMvvmLoader.make(
                LoginPageController.class.getResource("login-page.fxml"));
        BorderPane loginPane = loginNode.node();
        app.getRootPane().getChildren().setAll(loginPane);
        stage.setTitle("KOMET Login");

        app.getAppMenu().setupMenus();
    }

    void launchSelectDataSourcePage(Stage stage) {
        try {
            FXMLLoader sourceLoader = new FXMLLoader(getClass().getResource("SelectDataSource.fxml"));
            BorderPane sourceRoot = sourceLoader.load();
            SelectDataSourceController sourceController = sourceLoader.getController();
            sourceController.getCancelButton().setOnAction(actionEvent -> {
                // Exit the application if the user cancels the data source selection
                Platform.exit();
                app.stopServer();
            });
            app.getRootPane().getChildren().setAll(sourceRoot);
            stage.setTitle("KOMET Startup");

            app.getAppMenu().setupMenus();
        } catch (IOException ex) {
            LOG.error("Failed to initialize the select data source window", ex);
        }
    }


    /**
     * When a user selects the menu option View/New Journal a new Stage Window is launched.
     * This method will load a navigation panel to be a publisher and windows will be connected
     * (subscribed) to the activity stream.
     *
     * @param journalWindowSettings if present will give the size and positioning of the journal window
     */
    void launchJournalViewPage(PrefX journalWindowSettings) {
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
        journalStage.getIcons().setAll(app.getAppIcon());
        journalStage.setScene(sourceScene);

        if (!IS_MAC) {
            app.getAppMenu().generateMsWindowsMenu(journalBorderPane, journalStage);
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
            app.saveJournalWindowsToPreferences();
            journalController.shutdown();
            app.getJournalControllersList().remove(journalController);

            journalWindowSettings.setValue(CAN_DELETE, true);
            app.getKViewEventBus().publish(JOURNAL_TOPIC,
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
        app.getKViewEventBus().publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
        app.getJournalControllersList().add(journalController);

        if (IS_BROWSER) {
            app.getWebAPI().openStageAsTab(journalStage, journalName.replace(" ", "_"));
        } else {
            journalStage.show();
        }
    }
}
