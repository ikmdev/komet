package dev.ikm.komet.kview.mvvm.view.loginauthor;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;

public class LoginAuthorController {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAuthorController.class);
    private static boolean isvisible = false;

    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox userChooser;
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

    @FXML
    public void initialize() {
        LoginAuthorDataModel.fakeusers();
        userChooser.setPromptText("Select a user");
        userChooser.setItems(LoginAuthorDataModel.list);
        passwordTextField.setVisible(false);
        loginButton.setDisable(true);
    }

    @FXML
    public void signupAction(ActionEvent actionEvent) {
        if (isvisible) {
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
            if (!isvisible) {
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
        if (!isvisible) {
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
        if (!isvisible) {
            visibilityIcon.setImage(new javafx.scene.image.Image("dev.ikm.komet.kview.mvvm.view.images.view.png"));
            String a = passwordField.getText();
            passwordField.setVisible(false);
            passwordTextField.setText(a);
            passwordTextField.setPromptText("Password");
            passwordTextField.setVisible(true);
            isvisible = true;
        } else {
            visibilityIcon.setImage(new javafx.scene.image.Image("dev.ikm.komet.kview.mvvm.view.images.hidden.png"));
            String a = passwordTextField.getText();
            passwordTextField.setVisible(false);
            passwordField.setText(a);
            passwordField.setVisible(true);
            isvisible = false;
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
