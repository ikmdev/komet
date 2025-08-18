package dev.ikm.komet.kview.mvvm.view.loginauthor;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.Collections;

import static dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorViewModel.LoginProperties.*;

public class LoginAuthorViewModel extends FormViewModel {

    public enum LoginProperties {

        AUTHORS("authors"),
        SELECTED_AUTHOR("selected author"),
        LOGIN_ERROR("login-error"),
        PASSWORD("password");

        public final String name;

        LoginProperties(String name) {
            this.name = name;
        }

        LoginProperties() {
            this.name = this.name();
        }
    }

    public LoginAuthorViewModel() {
        super();
        addProperty(AUTHORS, Collections.emptyList(), true)
                .addProperty(SELECTED_AUTHOR, (ConceptEntity) null)
                .addProperty(PASSWORD, "")
                .addProperty(LOGIN_ERROR, "");

        addValidator(SELECTED_AUTHOR, SELECTED_AUTHOR.name(), (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
            if (prop.isNull().get()) {
                validationResult.error("Error: Please select a User");
                setPropertyValue(LOGIN_ERROR, "Error: Please select a User");
            }
        });
        addValidator(PASSWORD, PASSWORD.name, (ValidationResult validationResult, ViewModel viewModel) -> {
            String password = viewModel.getPropertyValue(PASSWORD);
            if (password.isBlank() || password.length() < 4 || !authenticateUser()) {
                validationResult.error("Login failed, please check your credentials");
                setPropertyValue(LOGIN_ERROR, "Login failed, please check your credentials");
            }
        });
    }

    /***
     * TODO Make a service call instead to validate the credentials. This is a
     * temporary workaround where the check is to see if username is same as password.
     * @return boolean
     */
    private boolean authenticateUser() {
        ViewProperties viewProperties = getPropertyValue(VIEW_PROPERTIES);
        ConceptEntity user = getPropertyValue(SELECTED_AUTHOR);
        String username = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(user.nid());
        String password = getPropertyValue(PASSWORD);
        return password.equals(username);
    }
}
