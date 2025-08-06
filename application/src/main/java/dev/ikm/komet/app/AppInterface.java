package dev.ikm.komet.app;

import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageController;

/**
 * Contains the shared elements between App and WebApp.
 * Ideally, this will be removed and only one App is left - because they become the same.
 */
public interface AppInterface {

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
}
