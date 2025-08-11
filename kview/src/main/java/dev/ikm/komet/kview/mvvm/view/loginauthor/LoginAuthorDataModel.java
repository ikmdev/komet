package dev.ikm.komet.kview.mvvm.view.loginauthor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

/**
This Class contains functionality for generating fake users for test implementation, original authorlist should be implemented for later use.
 */

public class LoginAuthorDataModel {


    private static final ArrayList<LoginAuthorUserModel> userList = new ArrayList<LoginAuthorUserModel>();
    protected static ObservableList<String> list = FXCollections.observableArrayList();

    protected static void fakeusers() {
        userList.clear();
        for (int i = 0; i < 10; i++) {
            LoginAuthorUserModel user = new LoginAuthorUserModel();
            user.userName = "KometUser" + (i + 1);
            user.userPassword = "KometUser" + (i + 1);
            userList.add(user);
            String userName = user.getUserName();
            list.add(userName);
        }
    }

    public static boolean validateUser(String username, String userpassword) {
        boolean result = false;
        for (LoginAuthorUserModel user : userList) {
            if (user.getUserName().equals(username) && user.getUserpassword().equals(userpassword)) {
                result = true;
                break;
            }
        }
        return result;
    }

}