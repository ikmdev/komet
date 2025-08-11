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

    private boolean Loginenabled=false;

    private CompletableFuture<LoginAuthorUserModel> onLoginFuture = new CompletableFuture<>();

    @FXML
    public void initialize() {



     // with this call we produce the fake users List and the fake passwords List. You need to implement your dataprovider instead of this to have a
     // real login functionality. This is just a mockup for later use.
     LoginAuthorDataModel.fakeusers();


        userchooser.setPromptText("Select a user");
        userchooser.setItems(LoginAuthorDataModel.list);
        passwordtextfield.setVisible(false);
LoginButton.setDisable(true);

        System.out.println("Initializing");

    }

    @FXML
    public void SignupAction(ActionEvent actionEvent) {

        if (isvisible) {
            SwapVisibility();
        }

        System.out.println("Signup");
        boolean error = false;
        String Errormessage = "";
        boolean loginproceed = false;

        System.out.println(userchooser.getValue());
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

            if (isvisible) {
                //visibleclicked(null);
            }

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
        //this method is used to clean the error labels, when the user clicks on the textfield or passwordfield
        usererrorlabel.setText("");
        passworderrorlabel.setText("");
        loginerrorlabel.setText("");
    }

    @FXML
    public void countlength(Event event) {
        clicked();//key typed in, counts before the adding to the string

    }

    @FXML
    public void clicked() {
        cleanerrorlabels();
        //vslidator for the password field, checks if the password is at least 5 characters long in visible and invisible mode
        String pwvalidate = "";
        if (!isvisible) {
            pwvalidate = passwordfield.getText();
        } else {
            pwvalidate = passwordtextfield.getText();
        }
        System.out.println(passwordfield.getText().length());
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
        //this method swaps the visibility of the password field and the password textfield for editing the password and switching invisible when login is clicked
        //it also swaps the visibility icon from hidden to visible and vice versa
        //this is used to show the password in plain text or as a password field with masked text
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
