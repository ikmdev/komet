package dev.ikm.komet.kview.mvvm.view.loginauthor;

import dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.PasswordField;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.Collections;

import static dev.ikm.komet.kview.mvvm.view.loginauthor.LoginAuthorDataModel.validateUser;
import static dev.ikm.komet.kview.mvvm.viewmodel.StampViewModel2.StampProperties.IS_STAMP_VALUES_THE_SAME;

public class LoginAuthorViewModel extends FormViewModel {


    public static final String AUTHORS = "authors";
    public static final String SELECTED_AUTHOR = "selected-author";
    public static final String LOGIN_ERROR = "login-error";

    public LoginAuthorViewModel() {
        super();
        addProperty(AUTHORS, Collections.emptyList())
                .addProperty(SELECTED_AUTHOR, (LoginAuthorUserModel) null)
                .addProperty(LOGIN_ERROR, "");

        addValidator(SELECTED_AUTHOR, "selected-author", (ValidationResult vr, ViewModel vm) -> {
            LoginAuthorUserModel loginAuthorUserModel = vm.getPropertyValue(SELECTED_AUTHOR);
            boolean valid = validateUser(loginAuthorUserModel.userName, loginAuthorUserModel.userPassword);
            if (!valid) {
                // if UI’s stamp is the same as the previous stamp than it is invalid.
                vm.setPropertyValue(LOGIN_ERROR, "Login failed, please check your credentials");
            }
        });
    }
}
