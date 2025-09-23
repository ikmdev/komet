package dev.ikm.komet.app;

import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.mvvm.model.*;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageViewFactory;
import dev.ikm.komet.kview.mvvm.view.login.LoginPageController;
import dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorController;
import dev.ikm.komet.navigator.graph.GraphNavigatorNodeFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.search.SearchNodeFactory;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.view.calculator.*;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static dev.ikm.komet.app.App.IS_BROWSER;
import static dev.ikm.komet.app.App.IS_MAC;
import static dev.ikm.komet.app.AppState.SHUTDOWN;
import static dev.ikm.komet.app.util.CssFile.KOMET_CSS;
import static dev.ikm.komet.app.util.CssFile.KVIEW_CSS;
import static dev.ikm.komet.app.util.CssUtils.addStylesheets;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorViewModel.LoginProperties.SELECTED_AUTHOR;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CURRENT_JOURNAL_WINDOW_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.JournalViewModel.JOURNAL_NAME;
import static dev.ikm.komet.kview.mvvm.viewmodel.JournalViewModel.WINDOW_SETTINGS;
import static dev.ikm.komet.preferences.JournalWindowPreferences.*;
import static dev.ikm.komet.preferences.JournalWindowSettings.*;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class AppPages {
    private static final Logger LOG = LoggerFactory.getLogger(AppPages.class);

    private final App app;

    /**
     * Property dev_author is a property passed into the system to bypass the user login screen.
     * -Ddev_author=Gretel
     * -Ddev_author=1c0023ed-559e-3311-9e55-bd4bd9e5628f
     */
    private final static String DEV_AUTHOR = "dev_author";

    public AppPages(App app) {
        this.app = app;
    }

    void launchLoginPage(Stage stage) {
        JFXNode<BorderPane, Void> loginNode = FXMLMvvmLoader.make(
                LoginPageController.class.getResource("login-page.fxml"));
        BorderPane loginPane = loginNode.node();
        app.rootPane.getChildren().setAll(loginPane);
        stage.setTitle("KOMET Login");

        app.appMenu.setupMenus(loginPane);
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
            app.rootPane.getChildren().setAll(sourceRoot);
            stage.setTitle("KOMET Startup");

            app.appMenu.setupMenus(app.rootPane);
        } catch (IOException ex) {
            LOG.error("Failed to initialize the select data source window", ex);
        }
    }

    void launchLoginAuthor(Stage stage){
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences windowPreferences = appPreferences.node(AUTHOR_LOGIN_WINDOW);
        final WindowSettings windowSettings = new WindowSettings(windowPreferences);
        ViewProperties viewProperties = windowSettings.getView().makeOverridableViewProperties("login-author");

        // Bypass the login screen if developer has specified -Ddev_author=<username or uuid string>
        if (bypassLogin(viewProperties)) {
            return;
        }

        Config loginConfig = new Config(LoginAuthorController.class.getResource("LoginAuthor.fxml"))
                .updateViewModel("loginAuthorViewModel", loginAuthorViewModel -> {
                    loginAuthorViewModel.setPropertyValue(VIEW_PROPERTIES, viewProperties);
                });


        JFXNode<StackPane, LoginAuthorController> journalJFXNode = FXMLMvvmLoader.make(loginConfig);
        StackPane authorLoginBorderPane = journalJFXNode.node();
        stage.getIcons().setAll(app.appIcon);
        stage.setTitle("KOMET Author selection");
        stage.setWidth(authorLoginBorderPane.prefWidth(USE_COMPUTED_SIZE));
        stage.setHeight(authorLoginBorderPane.prefHeight(USE_COMPUTED_SIZE));
        app.rootPane.getChildren().setAll(authorLoginBorderPane);

        LoginAuthorController loginAuthorController = journalJFXNode.controller();

        loginAuthorController.onLogin().thenAccept(loginAuthorViewModel -> {
            ConceptEntity userConceptEntity = loginAuthorViewModel.getPropertyValue(SELECTED_AUTHOR);
            ConceptFacade loggedInUser = ConceptFacade.make(userConceptEntity.nid());
            App.userProperty.set(loggedInUser);
            App.state.set(AppState.RUNNING);
        });
    }

    /**
     * Returns true if developer specifies a valid user or uuid of concept (decendents of user) otherwise false.
     * @return Returns true if developer specifies a valid user or uuid of concept (decendents of user) otherwise false.
     */
    private boolean bypassLogin(ViewProperties viewProperties) {
        // Check for developer bypass using a known user.
        String devAuthorPropStr = System.getProperty(DEV_AUTHOR);
        if (devAuthorPropStr != null) {
            // Create new instance of ViewCalculator to have stated navigation along with inferred.
            ViewCalculator viewCalculator = ViewCoordinateHelper.createNavigationCalculatorWithPatternNidsLatest(viewProperties, TinkarTerm.STATED_NAVIGATION_PATTERN.nid());
            Set<ConceptEntity> conceptEntitySet = fetchDescendentsOfConcept(viewCalculator, TinkarTerm.USER.publicId());
            if (conceptEntitySet.isEmpty()) {
                // add default user into set of available users
                conceptEntitySet.add(EntityService.get().getEntityFast(TinkarTerm.USER));
            }

            // check for name or public id
            Optional<ConceptEntity> conceptEntityOpt = conceptEntitySet.stream().filter(conceptEntity -> {
                // if found bypass
                Optional<String> devAuthor = viewCalculator.getDescriptionText(conceptEntity.nid());
                // LOG.info("author name = {}, and idstring = {}", devAuthor.orElse("no name"), conceptEntity.publicId().idString());
                UUID uuid = null;
                try {
                    uuid = UUID.fromString(devAuthorPropStr);
                } catch (IllegalArgumentException ex) {
                    // ignore
                }

                // check if developer passed in uuid or a name description.
                return uuid != null
                        && conceptEntity.publicId().contains(UUID.fromString(devAuthorPropStr))
                        || devAuthor.isPresent()
                        && devAuthor.get().equals(devAuthorPropStr);
            }).findFirst();

            // if a match is found go and by pass
            conceptEntityOpt.ifPresentOrElse(conceptEntity -> {
                        // bypass login screen
                        LOG.info("Developer By Pass {} = {}, name = {}", DEV_AUTHOR, devAuthorPropStr, viewCalculator.getDescriptionTextOrNid(conceptEntity.nid()));
                        App.userProperty.set(conceptEntity.toProxy());
                        App.state.set(AppState.RUNNING);
                    }, ()->
                            // Developer entered a non existing user
                            LOG.warn("No concept entity found for user id {}. Will be showing login screen.", devAuthorPropStr)
            );

            // if found then avoid loading login screen.
            if (conceptEntityOpt.isPresent()) {
                return true;
            }
        }
        return false;
    }

    public void launchLandingPage(Stage stage, ConceptFacade loggedInUser) {
        try {
            app.rootPane.getChildren().clear(); // Clear the root pane before adding new content

            KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
            KometPreferences windowPreferences = appPreferences.node("main-komet-window");

            WindowSettings windowSettings = new WindowSettings(windowPreferences);
            FXMLLoader landingPageLoader = LandingPageViewFactory.createFXMLLoader();
            BorderPane landingPageBorderPane = landingPageLoader.load();

            if (!IS_MAC) {
                app.appMenu.createMenuOptions(landingPageBorderPane);
            }

            String username = windowSettings.getView().calculator().getPreferredDescriptionTextWithFallbackOrNid(loggedInUser.nid());
            app.landingPageController = landingPageLoader.getController();
            app.landingPageController.getWelcomeTitleLabel().setText("Welcome " + username);
            app.landingPageController.setSelectedDatasetTitle(PrimitiveData.get().name());
            app.landingPageController.getGithubStatusHyperlink().setOnAction(_ -> app.appGithub.connectToGithub());

            stage.setTitle("Landing Page");
            stage.setMaximized(true);
            stage.setOnCloseRequest(windowEvent -> {
                // This is called only when the user clicks the close button on the window
                App.state.set(SHUTDOWN);
                app.landingPageController.cleanup();
            });

            app.rootPane.getChildren().add(landingPageBorderPane);

            app.appMenu.setupMenus(app.rootPane);
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
    void launchJournalViewPage(PrefX journalWindowSettings, ConceptFacade loggedInUser) {
        Objects.requireNonNull(journalWindowSettings, "journalWindowSettings cannot be null");
        final KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        final KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);
        final WindowSettings windowSettings = new WindowSettings(windowPreferences);
        final UUID journalTopic = journalWindowSettings.getValue(JOURNAL_TOPIC);
        Objects.requireNonNull(journalTopic, "journalTopic cannot be null");

        ObservableEditCoordinate editCoordinate = windowSettings.getView().editCoordinate();
        editCoordinate.authorForChangesProperty().setValue(loggedInUser);

        Config journalConfig = new Config(JournalController.class.getResource("journal.fxml"))
                .updateViewModel("journalViewModel", journalViewModel -> {
                    journalViewModel.setPropertyValue(CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic);
                    journalViewModel.setPropertyValue(WINDOW_SETTINGS, windowSettings);
                    journalViewModel.setPropertyValue(JOURNAL_NAME, journalWindowSettings.getValue(JOURNAL_TITLE));
                });
        JFXNode<BorderPane, JournalController> journalJFXNode = FXMLMvvmLoader.make(journalConfig);
        BorderPane journalBorderPane = journalJFXNode.node();
        JournalController journalController = journalJFXNode.controller();

        journalController.setup(windowPreferences);
        
        Scene sourceScene = new Scene(journalBorderPane, DEFAULT_JOURNAL_WIDTH, DEFAULT_JOURNAL_HEIGHT);
        addStylesheets(sourceScene, KOMET_CSS, KVIEW_CSS);

        Stage journalStage = new Stage();
        journalStage.getIcons().setAll(app.appIcon);
        journalStage.setScene(sourceScene);

        if (!IS_MAC) {
            app.appMenu.generateMsWindowsMenu(journalBorderPane, journalStage);
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
            journalController.restoreWindows(windowSettings, journalWindowSettings);
        } else {
            journalStage.setMaximized(true);
        }

        journalStage.setOnHidden(windowEvent -> {
            app.saveJournalWindowsToPreferences();
            journalController.shutdown();
            app.journalControllersList.remove(journalController);

            journalWindowSettings.setValue(CAN_DELETE, true);
            app.kViewEventBus.publish(JOURNAL_TOPIC,
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
        app.kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
        app.journalControllersList.add(journalController);

        if (IS_BROWSER) {
            app.webAPI.openStageAsTab(journalStage, journalName.replace(" ", "_"));
        } else {
            journalStage.show();
        }
    }
}
