package dev.ikm.komet.kview.mvvm.view.loginauthor;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.Collections;

import static dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorViewModel.LoginProperties.*;
import dev.ikm.komet.kview.mvvm.viewmodel.ViewModelKey;

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
            }
        });
        addValidator(PASSWORD, PASSWORD.name, (ValidationResult validationResult, ViewModel viewModel) -> {
            String password = viewModel.getPropertyValue(PASSWORD);
            if (password.isBlank() || password.length() < 4) {
                validationResult.error("Login failed, please check your credentials");
            }
        });
    }

    /***
     * TODO Make a service call instead to validate the credentials. This is a
     * temporary workaround where the check is to see if username is same as password.
     * @return boolean
     */
    public boolean authenticateUser() {
        ViewProperties viewProperties = getPropertyValue(ViewModelKey.VIEW_PROPERTIES);
        ConceptEntity user = getPropertyValue(SELECTED_AUTHOR);
        String password = getPropertyValue(PASSWORD);
        return passwordMatches(viewProperties, user, password);
    }

    /**
     * Checks a password for an author the way the author screen does, so a command-line login
     * (IKE-Network/ike-issues#1139) accepts exactly what the screen accepts. Today that is the
     * placeholder check that the password equals the author's preferred name; real
     * authentication is IKE-Network/ike-issues#1150.
     *
     * @param viewProperties the view whose calculator names the author
     * @param author         the author logging in
     * @param password       the password given
     * @return true if the author screen would accept the password
     */
    public static boolean passwordMatches(ViewProperties viewProperties, ConceptEntity author, String password) {
        String username = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(author.nid());
        return password.equals(username);
    }
}
