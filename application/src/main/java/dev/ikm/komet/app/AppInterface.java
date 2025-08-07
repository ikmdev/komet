package dev.ikm.komet.app;

import com.jpro.webapi.WebAPI;
import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import one.jpro.platform.auth.core.authentication.User;

/**
 * Contains the shared elements between App and WebApp.
 * Ideally, this will be removed and only one App is left - because they become the same.
 */
public interface AppInterface {


    AppMenu getAppMenu();

    AppGithub getAppGithub();

    AppClassicKomet getAppClassicKomet();

    /**
     * Returns the default App icon.
     *
     * @return the {@link Image} representing the app icon
     */
    Image getAppIcon();

    WebAPI getWebAPI();


    SimpleObjectProperty<AppState> getState();

    /**
     * Returns the controller for the landing page.
     *
     * @return the {@link LandingPageController} instance
     */
    LandingPageController getLandingPageController();

    /**
     * Returns the GitHub preferences DAO.
     *
     * @return the {@link GitHubPreferencesDao} instance
     */
    GitHubPreferencesDao getGitHubPreferencesDao();

    /**
     * Quits the application.
     */
    void quit();

    void launchLandingPage(Stage stage, User user);

    Stage getPrimaryStage();
}
