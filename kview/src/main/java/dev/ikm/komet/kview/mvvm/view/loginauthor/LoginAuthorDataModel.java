package dev.ikm.komet.kview.mvvm.view.loginauthor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

/*
This Class contains functionality for generting fake users for test implementation, origonal authorlist should be implemented for later use.
 */

public class LoginAuthorDataModel {


    private static final ArrayList<LoginAuthorUserModel> userList = new ArrayList<LoginAuthorUserModel>();
    protected static ObservableList<String> list = FXCollections.observableArrayList();

    protected static void fakeusers() {
        userList.clear();
        for (int i = 0; i < 10; i++) {
            LoginAuthorUserModel user = new LoginAuthorUserModel();
            user.username = "KometUser" + (i + 1);
            user.userpassword = "KometUser" + (i + 1);
            userList.add(user);
            String username = user.getUsername();
            list.add(username);
        }
    }

    public static boolean validateUser(String username, String userpassword) {
        boolean result = false;
        for (LoginAuthorUserModel user : userList) {
            if (user.getUsername().equals(username) && user.getUserpassword().equals(userpassword)) {
                result = true;
                break;
            }
        }
        return result;
    }

}