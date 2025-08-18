package dev.ikm.komet.kview.mvvm.view.loginauthor;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.PasswordField;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorDataModel.validateUser;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.IS_STAMP_VALUES_THE_SAME;

public class LoginAuthorViewModel extends FormViewModel {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAuthorViewModel.class);

    public static final String AUTHORS = "authors";
    public static final String SELECTED_AUTHOR = "selected-author";
    public static final String LOGIN_ERROR = "login-error";
    public static final String PASSWORD = "password";

    public LoginAuthorViewModel() {
        super();
        addProperty(AUTHORS, Collections.emptyList())
                .addProperty(SELECTED_AUTHOR, (ConceptEntity) null) // new LoginAuthorUserModel())
                .addProperty(PASSWORD, "")
                .addProperty(LOGIN_ERROR, "");

        addValidator(SELECTED_AUTHOR, "selected-author", (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
            if (prop.isNull().get()) {
                validationResult.error("Error: Please select a User");
                setPropertyValue(LOGIN_ERROR, "Error: Please select a User");
            }
        });
        addValidator(PASSWORD, "password", (ValidationResult validationResult, ViewModel viewModel) -> {
            String password = viewModel.getPropertyValue(PASSWORD);
            if (password.isBlank() || password.length() < 4 || !isPasswordValid()) {
                validationResult.error("Login failed, please check your credentials");
                setPropertyValue(LOGIN_ERROR, "Login failed, please check your credentials");
            }
        });
    }

    /***
     * TODO Make a service call instead to validate the credentials. This is a
     * temporary workaround where the check is to see if uername is same as password.
     * @return boolean
     */
    private boolean isPasswordValid() {
        ViewProperties viewProperties = getPropertyValue(VIEW_PROPERTIES);
        ConceptEntity user = getPropertyValue(SELECTED_AUTHOR);
        String username = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(user.nid());
        String password = getPropertyValue(PASSWORD);
        return password.equals(username);
    }
}
