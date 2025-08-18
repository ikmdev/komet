package dev.ikm.komet.kview.mvvm.view.loginauthor;

import dev.ikm.komet.framework.view.ViewProperties;
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
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
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
        loginAuthorViewModel.getObservableList(AUTHORS).addAll(fetchDescendentsOfConcept(viewProperties, TinkarTerm.USER.publicId()));
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
        loginErrorLabel.textProperty().bindBidirectional(loginAuthorViewModel.getProperty(LOGIN_ERROR));

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
        loginErrorLabel.setText("");
        loginAuthorViewModel.setPropertyValue(LOGIN_ERROR, "");
        ValidationViewModel validationViewModel = loginAuthorViewModel.validate();
        if (!validationViewModel.hasErrorMsgs()) {
            loginErrorLabel.setText("");
            LOG.info("Author selected: " + userChooser.getValue().toString());
            onLoginFuture.complete(loginAuthorViewModel);
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
