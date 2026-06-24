package dev.ikm.komet.kview.mvvm.view.loginauthor;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.ViewCoordinateHelper;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.ComponentWithNid;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchLeafDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorViewModel.LoginProperties.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey.VIEW_PROPERTIES;

public class LoginAuthorController {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAuthorController.class);
    private static boolean isVisible = false;

    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<ComponentWithNid> userChooser;
    @FXML
    private Button loginButton;
    @FXML
    private Label userErrorLabel;
    @FXML
    private ImageView visibilityIcon;
    @FXML
    private TextField passwordTextField;
    private CompletableFuture<LoginAuthorViewModel> onLoginFuture = new CompletableFuture<>();
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label loginErrorLabel;

    @InjectViewModel
    private LoginAuthorViewModel loginAuthorViewModel;

    @SuppressWarnings("removal")
    @FXML
    public void initialize() {
        ViewProperties viewProperties = getViewProperties();
        // Create new instance of ViewCalculator to have stated navigation along with inferred.
        ViewCalculator viewCalculator = ViewCoordinateHelper.createNavigationCalculatorWithPatternNidsLatest(viewProperties, TinkarTerm.STATED_NAVIGATION_PATTERN.nid());
        // Only leaf descendants of USER are named users; grouping concepts in the subtree are excluded (ike-issues#754).
        Set<ConceptEntity> conceptEntitySet = fetchLeafDescendentsOfConcept(viewCalculator, TinkarTerm.USER.publicId());

        //If there are no authors mentioned in the stated or inferred then we use the default tinkar term user.
        if (conceptEntitySet.isEmpty()) {
            //TODO further refactoring should be done to be more abstract and UI should only use light entity facade to be more abstract.
            conceptEntitySet.add(EntityService.get().getEntityFast(TinkarTerm.USER));
        }

        loginAuthorViewModel.getObservableList(AUTHORS).addAll(conceptEntitySet);
        userChooser.setPromptText("Select a user");
        userChooser.setItems(loginAuthorViewModel.getObservableList(AUTHORS));
        userChooser.getItems().sort(Comparator.comparing(this::getUserName));

        userChooser.setConverter(new StringConverter<>() {
            @Override
            public String toString(ComponentWithNid componentWithNid) {
                return getUserName(componentWithNid);
            }

            @Override
            public ComponentWithNid fromString(String s) {
                return null;
            }
        });

        userChooser.valueProperty().bindBidirectional(loginAuthorViewModel.getProperty(SELECTED_AUTHOR));
        passwordField.textProperty().bindBidirectional(loginAuthorViewModel.getProperty(PASSWORD));

        // Default the picker to the last-used author from preferences (ike-issues#754).
        ComponentWithNid defaultAuthor = resolveDefaultAuthor();
        if (defaultAuthor != null) {
            userChooser.setValue(defaultAuthor);
        }

        passwordTextField.setVisible(false);
        loginButton.setDisable(true);
        loginButton.disableProperty().bind(loginAuthorViewModel.invalidProperty());
        loginErrorLabel.textProperty().bind(loginAuthorViewModel.getStringProperty(LOGIN_ERROR));
        loginAuthorViewModel.doOnChange(() -> loginAuthorViewModel.validate(), SELECTED_AUTHOR, PASSWORD);
        loginAuthorViewModel.save(true);
    }

    private ViewProperties getViewProperties() {
        return loginAuthorViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    private String getUserName(ComponentWithNid componentWithNid) {
        ViewProperties viewProperties = getViewProperties();
        return viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(componentWithNid.nid());
    }

    @FXML
    public void signInAction(ActionEvent actionEvent) {
        if (isVisible) {
            swapVisibility();
        }
        if (loginAuthorViewModel.validProperty().get() && loginAuthorViewModel.authenticateUser()) {
            loginAuthorViewModel.setPropertyValue(LOGIN_ERROR, "");
            LOG.info("Author selected: " + userChooser.getValue().toString());
            persistSelectedAuthor(userChooser.getValue());
            onLoginFuture.complete(loginAuthorViewModel);
        } else {
            loginAuthorViewModel.setPropertyValue(LOGIN_ERROR, "Login failed, please check your credentials");
        }
    }

    private static final String AUTHOR_LOGIN_NODE = "author-login";
    private static final String LAST_AUTHOR_KEY = "last-author-uuid";
    private static final String SELECTED_AUTHORS_KEY = "selected-author-uuids";

    /**
     * Resolves the author to pre-select: the last-used author if it is present in the knowledge base, else the
     * first preference-listed author present in the KB, else the first available leaf (the items are already
     * sorted by name). Returns {@code null} only when no authors are available (ike-issues#754).
     *
     * @return the author to pre-select, or {@code null} if none are available
     */
    private ComponentWithNid resolveDefaultAuthor() {
        java.util.List<ComponentWithNid> available = userChooser.getItems();
        if (available.isEmpty()) {
            return null;
        }
        KometPreferences authorPrefs = KometPreferencesImpl.getConfigurationRootPreferences().node(AUTHOR_LOGIN_NODE);
        ComponentWithNid lastUsed = authorPrefs.get(LAST_AUTHOR_KEY).flatMap(uuid -> findByUuid(available, uuid)).orElse(null);
        if (lastUsed != null) {
            return lastUsed;
        }
        for (String uuid : authorPrefs.get(SELECTED_AUTHORS_KEY).orElse("").split(",")) {
            ComponentWithNid match = findByUuid(available, uuid).orElse(null);
            if (match != null) {
                return match;
            }
        }
        return available.get(0);
    }

    private java.util.Optional<ComponentWithNid> findByUuid(java.util.List<ComponentWithNid> available, String uuidText) {
        try {
            java.util.UUID uuid = java.util.UUID.fromString(uuidText.trim());
            return available.stream()
                    .filter(author -> EntityService.get().getEntityFast(author.nid()).publicId().asUuidList().contains(uuid))
                    .findFirst();
        } catch (IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }

    /**
     * Persists the signed-in author as the last-used author and accumulates it into the selected-authors list,
     * so the next launch defaults to it (ike-issues#754).
     *
     * @param author the author the user signed in as
     */
    private void persistSelectedAuthor(ComponentWithNid author) {
        if (author == null) {
            return;
        }
        try {
            KometPreferences authorPrefs = KometPreferencesImpl.getConfigurationRootPreferences().node(AUTHOR_LOGIN_NODE);
            String uuid = EntityService.get().getEntityFast(author.nid()).publicId().asUuidList().get(0).toString();
            authorPrefs.put(LAST_AUTHOR_KEY, uuid);
            java.util.LinkedHashSet<String> selected = new java.util.LinkedHashSet<>();
            for (String existing : authorPrefs.get(SELECTED_AUTHORS_KEY).orElse("").split(",")) {
                if (!existing.isBlank()) {
                    selected.add(existing.trim());
                }
            }
            selected.add(uuid);
            authorPrefs.put(SELECTED_AUTHORS_KEY, String.join(",", selected));
            authorPrefs.flush();
        } catch (Exception e) {
            LOG.warn("Could not persist last-used author preference", e);
        }
    }


    public void cleanErrorLabels() {
        userErrorLabel.setText("");
        passwordErrorLabel.setText("");
        loginAuthorViewModel.setPropertyValue(LOGIN_ERROR, "");
    }

    @FXML
    public void clicked() {
        cleanErrorLabels();
        String pwvalidate = "";
        if (!isVisible) {
            pwvalidate = passwordField.getText();
        } else {
            pwvalidate = passwordTextField.getText();
        }
    }

    public void swapVisibility() {
        if (!isVisible) {
            visibilityIcon.setImage(new Image(getClass().getResource("/dev/ikm/komet/kview/mvvm/view/images/view.png").toString()));
            String a = passwordField.getText();
            passwordField.setVisible(false);
            passwordTextField.setText(a);
            passwordTextField.setPromptText("Password");
            passwordTextField.setVisible(true);
            isVisible = true;
        } else {
            visibilityIcon.setImage(new Image(getClass().getResource("/dev/ikm/komet/kview/mvvm/view/images/hidden.png").toString()));
            String a = passwordTextField.getText();
            passwordTextField.setVisible(false);
            passwordField.setText(a);
            passwordField.setVisible(true);
            isVisible = false;
        }
    }

    public CompletableFuture<LoginAuthorViewModel> onLogin() {
        return onLoginFuture;
    }

    @FXML
    public void cleanError(ActionEvent actionEvent) {
        cleanErrorLabels();
    }

    @FXML
    public void visibleClicked(Event event) {
        swapVisibility();
        cleanErrorLabels();
    }

    @FXML
    public void countLength(Event event) {
        clicked();
    }

    @FXML
    public void openIkeNetwork() {
        new Thread(() -> {
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://ike.network"));
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("Could not open ike.network", e);
            }
        }, "open-ike-network").start();
    }
}
