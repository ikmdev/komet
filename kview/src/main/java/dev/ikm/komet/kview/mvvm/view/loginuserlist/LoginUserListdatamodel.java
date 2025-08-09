package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

//important: this is a mockup for later uses without any security concerns but the availability of a userList. This is done while
//we got the order to generate it so as a Login sample for later use.
//so there is no Binding, neither in controller class or here. When refactoring with security relevant data, change the data structure
//to provide the needed secure login.

public class LoginUserListdatamodel {


    private static final ArrayList<LoginUserListUsermodel> userList = new ArrayList<LoginUserListUsermodel>();
    protected static ObservableList<String> list = FXCollections.observableArrayList();

    protected static void fakeusers() {
        //this users are fake users fot validating the login process data against something. Instead of this Fakeuserclass, you can use a database or any other data source.
        //this is just a mockup for later use. Please do not forget to remove this class and the call of this class in Scene initilizing Controller!
        userList.clear();
        for (int i = 0; i < 10; i++) {
            LoginUserListUsermodel user = new LoginUserListUsermodel();
            user.username = "KometUser" + (i + 1);
            user.userpassword = "KometUser" + (i + 1);
            userList.add(user);
            String username = user.getUsername();
            list.add(username);
        }

        //generating the userlist ffor the combobox from the usermoel
        //which is used in this case: for a real validation scenario there should be a database solution or at least a data source
        //this is only a placeholder-mockup!
    }

    public static boolean validateUser(String username, String userpassword) {
        boolean result = false;
        for (LoginUserListUsermodel user : userList) {
            if (user.getUsername().equals(username) && user.getUserpassword().equals(userpassword)) {
                result = true;
                break;
            }
        }
        return result;
    }


}