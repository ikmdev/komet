/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.login;

/**
 * Enum representing property names used in the Login View.
 * <p>
 * This enum provides constants that represent various properties
 * related to user authentication, such as username, password,
 * sign-in button state, and error messages.
 * </p>
 */
public enum LoginViewPropertyName {

    /**
     * Property name for the username field.
     */
    USERNAME("Username"),

    /**
     * Property name for the password field.
     */
    PASSWORD("Password"),

    /**
     * Property name for the sign-in button state.
     */
    SIGN_IN_BUTTON_STATE("SignIn Button State"),

    /**
     * Property name indicating whether fields are not populated.
     */
    IS_NOT_POPULATED("Is Not Populated"),

    /**
     * Property name for generic error messages.
     */
    ERROR("Error"),

    /**
     * Property name for username error messages.
     */
    USERNAME_ERROR(USERNAME.getPropertyName() + ERROR.getPropertyName()),

    /**
     * Property name for password error messages.
     */
    PASSWORD_ERROR(PASSWORD.getPropertyName() + ERROR.getPropertyName()),

    /**
     * Property name for authentication error messages.
     */
    AUTH_ERROR("Auth" + ERROR);

    private final String fieldName;

    /**
     * Constructs a new {@code LoginViewPropertyName} with the specified field name.
     *
     * @param fieldName the string value of the property name
     */
    LoginViewPropertyName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Returns the string value of the property name.
     *
     * @return the property name as a string
     */
    public String getPropertyName() {
        return fieldName;
    }
}
