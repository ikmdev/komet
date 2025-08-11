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
    private PasswordField passwordfield;
    @FXML
    private ComboBox userchooser;
    @FXML
    private Button LoginButton;
    @FXML
    private Label passworderrorlabel;
    @FXML
    private Label usererrorlabel;
    @FXML
    private ImageView visibilityicon;
    @FXML
    private TextField passwordtextfield;
    @FXML
    private AnchorPane MainPane;
    @FXML
    private Label loginerrorlabel;

    private boolean Loginenabled = false;

    private CompletableFuture<LoginAuthorUserModel> onLoginFuture = new CompletableFuture<>();

    @FXML
    public void initialize() {
        LoginAuthorDataModel.fakeusers();
        userchooser.setPromptText("Select a user");
        userchooser.setItems(LoginAuthorDataModel.list);
        passwordtextfield.setVisible(false);
        LoginButton.setDisable(true);
    }

    @FXML
    public void SignupAction(ActionEvent actionEvent) {
        if (isvisible) {
            SwapVisibility();
        }
        boolean error = false;
        String Errormessage = "";
        boolean loginproceed = false;
        if (userchooser.getValue() == null) {
            error = true;
            Errormessage = "Error: " + "Please select a User";
            usererrorlabel.setText(Errormessage);
        } else {
            String username = userchooser.getValue().toString();
            String password = "";
            if (!isvisible) {
                password = passwordfield.getText();
            } else {
                password = passwordtextfield.getText();
            }
            usererrorlabel.setText("");
            if (password.length() > 4) {
                loginproceed = true;
                passworderrorlabel.setText("");
            } else {
                error = true;
                Errormessage = "ERROR: Password must be at least 5 characters long";
                passworderrorlabel.setText(Errormessage);
                passworderrorlabel.setVisible(true);
            }
        }
        if (loginproceed) {
            boolean valid = LoginAuthorDataModel.validateUser(userchooser.getValue().toString(), passwordfield.getText());
            if (valid) {
                loginerrorlabel.setText("");
                LOG.info("Author selected: " + userchooser.getValue().toString());
                onLoginFuture.complete(null);
            } else {
                loginerrorlabel.setText("Login failed, please check your credentials");
            }
        }
    }

    @FXML
    public void cleanerror(ActionEvent actionEvent) {
        cleanerrorlabels();
    }

    public void cleanerrorlabels() {
        usererrorlabel.setText("");
        passworderrorlabel.setText("");
        loginerrorlabel.setText("");
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
            pwvalidate = passwordfield.getText();
        } else {
            pwvalidate = passwordtextfield.getText();
        }
        if (pwvalidate.length() >= 4) {
            passworderrorlabel.setText("");
            LoginButton.setDisable(false);
        }
    }

    @FXML
    public void visibleclicked(Event event) {
        SwapVisibility();
        cleanerrorlabels();
    }

    public void SwapVisibility() {
       if (!isvisible) {
            visibilityicon.setImage(new javafx.scene.image.Image("dev.ikm.komet.kview.mvvm.view.images.view.png"));
            String a = passwordfield.getText();
            passwordfield.setVisible(false);
            passwordtextfield.setText(a);
            passwordtextfield.setPromptText("Password");
            passwordtextfield.setVisible(true);
            isvisible = true;
        } else {
            visibilityicon.setImage(new javafx.scene.image.Image("dev.ikm.komet.kview.mvvm.view.images.hidden.png"));
            String a = passwordtextfield.getText();
            passwordtextfield.setVisible(false);
            passwordfield.setText(a);
            passwordfield.setVisible(true);
            isvisible = false;
        }
    }

    public CompletableFuture<LoginAuthorUserModel> onLogin() {
        return onLoginFuture;
    }
}
