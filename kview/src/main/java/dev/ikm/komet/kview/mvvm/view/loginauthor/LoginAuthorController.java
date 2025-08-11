package dev.ikm.komet.kview.mvvm.view.loginauthor;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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
    private Label passwordErrorlabel;
    @FXML
    private Label userErrorLabel;
    @FXML
    private ImageView visibilityIcon;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Label loginerrorLabel;

    private CompletableFuture<LoginAuthorUserModel> onLoginFuture = new CompletableFuture<>();

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
                passwordErrorlabel.setText("");
            } else {
                errormessage = "ERROR: Password must be at least 5 characters long";
                passwordErrorlabel.setText(errormessage);
                passwordErrorlabel.setVisible(true);
            }
        }
        if (loginproceed) {
            boolean valid = LoginAuthorDataModel.validateUser(userChooser.getValue().toString(), passwordField.getText());
            if (valid) {
                loginerrorLabel.setText("");
                LOG.info("Author selected: " + userChooser.getValue().toString());
                onLoginFuture.complete(null);
            } else {
                loginerrorLabel.setText("Login failed, please check your credentials");
            }
        }
    }

    @FXML
    public void cleanerror(ActionEvent actionEvent) {
        cleanerrorlabels();
    }

    public void cleanerrorlabels() {
        userErrorLabel.setText("");
        passwordErrorlabel.setText("");
        loginerrorLabel.setText("");
    }

    @FXML
    public void countlength(Event event) {
        clicked();
    }

    @FXML
    public void clicked() {
        cleanerrorlabels();
        String pwvalidate = "";
        if (!isvisible) {
            pwvalidate = passwordField.getText();
        } else {
            pwvalidate = passwordTextField.getText();
        }
        if (pwvalidate.length() >= 4) {
            passwordErrorlabel.setText("");
            loginButton.setDisable(false);
        }
    }

    @FXML
    public void visibleclicked(Event event) {
        swapVisibility();
        cleanerrorlabels();
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
}
