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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

/**
 * Enum representing property names used for Git-related configuration and operations.
 * These property names are used to identify various fields and states in the Git integration
 * components of the application.
 */
public enum GitPropertyName {

    /**
     * Property name for the Git repository URL field.
     */
    GIT_URL("Git Url"),

    /**
     * Property name for the GitHub email field.
     */
    GIT_EMAIL("Email"),

    /**
     * Property name for the username field.
     */
    GIT_USERNAME("Username"),

    /**
     * Property name for the password field.
     */
    GIT_PASSWORD("Password"),

    /**
     * Property name for the Git status field.
     */
    GIT_STATUS("Status"),

    /**
     * Property name for generic error messages.
     */
    ERROR("Error"),

    /**
     * Property name for connection error messages.
     */
    CONNECT_ERROR("Connect" + ERROR);

    /**
     * The display name of the property.
     */
    private final String fieldName;

    /**
     * Constructs a new GitPropertyName with the specified field name.
     *
     * @param fieldName the display name for this property
     */
    GitPropertyName(String fieldName) {
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