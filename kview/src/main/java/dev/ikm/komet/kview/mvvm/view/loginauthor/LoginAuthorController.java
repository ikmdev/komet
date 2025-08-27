package dev.ikm.komet.kview.mvvm.view.loginauthor;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.model.ViewCoordinateHelper;
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

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorViewModel.LoginProperties.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

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

    @FXML
    public void initialize() {
        ViewProperties viewProperties = getViewProperties();
        // Create new instance of ViewCalculator to have stated navigation along with inffered.
        ViewCalculator viewCalculator = ViewCoordinateHelper.createNavigationCalculatorWithPatternNidsLatest(viewProperties, TinkarTerm.STATED_NAVIGATION_PATTERN.nid());
        Set<ConceptEntity> conceptEntitySet = fetchDescendentsOfConcept(viewCalculator, TinkarTerm.USER.publicId());

        //If there are no authors mentioned in the stated or inferred then we use the default tinkar term user.
        if (conceptEntitySet.isEmpty()) {
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
            onLoginFuture.complete(loginAuthorViewModel);
        } else {
            loginAuthorViewModel.setPropertyValue(LOGIN_ERROR, "Login failed, please check your credentials");
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
}
