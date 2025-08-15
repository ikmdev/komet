package dev.ikm.komet.kview.mvvm.view.loginauthor;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchDescendentsOfConcept;
import static dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;

public class LoginAuthorController {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAuthorController.class);
    private static boolean isVisible = false;

    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<ConceptEntity> userChooser;
    @FXML
    private Button loginButton;
    @FXML
    private Label userErrorLabel;
    @FXML
    private ImageView visibilityIcon;
    @FXML
    private TextField passwordTextField;
    private CompletableFuture<LoginAuthorUserModel> onLoginFuture = new CompletableFuture<>();
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label loginErrorLabel;

    @InjectViewModel
    private LoginAuthorViewModel loginAuthorViewModel;

    @FXML
    public void initialize() {
        LoginAuthorDataModel.fakeusers();
        ViewProperties viewProperties = loginAuthorViewModel.getPropertyValue(VIEW_PROPERTIES);
        loginAuthorViewModel.getObservableList(AUTHORS).addAll(fetchDescendentsOfConcept(viewProperties, TinkarTerm.USER.publicId()));
        userChooser.setPromptText("Select a user");
        userChooser.setItems(loginAuthorViewModel.getObservableList(AUTHORS));
        userChooser.setConverter(new StringConverter<>() {
            @Override
            public String toString(ConceptEntity user) {
                return viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(user.nid());
            }

            @Override
            public ConceptEntity fromString(String s) {
                return null;
            }
        });
        passwordTextField.setVisible(false);
        loginButton.setDisable(true);

        userChooser.valueProperty().bindBidirectional(loginAuthorViewModel.getProperty(SELECTED_AUTHOR));
        loginErrorLabel.textProperty().bindBidirectional(loginAuthorViewModel.getProperty(LOGIN_ERROR));
    }

    @FXML
    public void signInAction(ActionEvent actionEvent) {
        if (isVisible) {
            swapVisibility();
        }
        String errormessage = "";
        boolean loginproceed = false;
        if (userChooser.getValue() == null) {
            errormessage = "Error: " + "Please select a User";
            userErrorLabel.setText(errormessage);
        } else {
            String username = userChooser.getValue().toString();
            String password = "";
            if (!isVisible) {
                password = passwordField.getText();
            } else {
                password = passwordTextField.getText();
            }
            userErrorLabel.setText("");
            if (password.length() > 4) {
                loginproceed = true;
                passwordErrorLabel.setText("");
            } else {
                errormessage = "ERROR: Password must be at least 5 characters long";
                passwordErrorLabel.setText(errormessage);
                passwordErrorLabel.setVisible(true);
            }
        }
        if (loginproceed) {
            boolean valid = LoginAuthorDataModel.validateUser(userChooser.getValue().toString(), passwordField.getText());
            if (valid) {
                loginErrorLabel.setText("");
                LOG.info("Author selected: " + userChooser.getValue().toString());
                onLoginFuture.complete(null);
            } else {
                loginErrorLabel.setText("Login failed, please check your credentials");
            }
        }
    }


    public void cleanErrorLabels() {
        userErrorLabel.setText("");
        passwordErrorLabel.setText("");
        loginErrorLabel.setText("");
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
        if (pwvalidate.length() >= 4) {
            passwordErrorLabel.setText("");
            loginButton.setDisable(false);
        }
    }

    public void swapVisibility() {
        if (!isVisible) {
            visibilityIcon.setImage(new javafx.scene.image.Image("dev.ikm.komet.kview.mvvm.view.images.view.png"));
            String a = passwordField.getText();
            passwordField.setVisible(false);
            passwordTextField.setText(a);
            passwordTextField.setPromptText("Password");
            passwordTextField.setVisible(true);
            isVisible = true;
        } else {
            visibilityIcon.setImage(new Image("dev.ikm.komet.kview.mvvm.view.images.hidden.png"));
            String a = passwordTextField.getText();
            passwordTextField.setVisible(false);
            passwordField.setText(a);
            passwordField.setVisible(true);
            isVisible = false;
        }
    }

    public CompletableFuture<LoginAuthorUserModel> onLogin() {
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
