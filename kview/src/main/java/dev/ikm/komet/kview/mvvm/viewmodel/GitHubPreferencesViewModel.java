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
package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.kview.mvvm.model.GitHubPreferencesDao;
import javafx.beans.property.ReadOnlyStringProperty;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.util.prefs.BackingStoreException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.*;

/**
 * View model for GitHub preferences that handles validation of GitHub credentials and repository information.
 * <p>
 * This class manages and validates the following GitHub-related information:
 * <ul>
 *   <li>Git repository URL - Must be a valid Git URL format (HTTPS or SSH)</li>
 *   <li>Git email - Optional, but must be a valid email format if provided</li>
 *   <li>GitHub username - Must follow GitHub username conventions</li>
 *   <li>GitHub password - Must meet security requirements</li>
 * </ul>
 * <p>
 * The class implements validation rules for each field and manages the state of the connect button
 * based on the validity of all fields. It also handles connection error messages.
 * <p>
 * The view model follows the MVVM (Model-View-ViewModel) pattern and extends {@code ValidationViewModel}
 * to provide validation functionality.
 *
 * @see ValidationViewModel
 * @see ValidationResult
 */
public class GitHubPreferencesViewModel extends ValidationViewModel {

    /**
     * Regular expression pattern for validating Git repository URLs.
     * Supports common formats including HTTPS and SSH URLs.
     */
    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?://|git@)([\\w.-]+)(:\\d+)?([/:])[\\w.-]+(/[\\w.-]+)+(\\.git)?$");

    /**
     * Regular expression pattern for validating email addresses.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * Regular expression pattern for validating GitHub usernames.
     * - May only contain alphanumeric characters or hyphens
     * - Cannot have multiple consecutive hyphens
     * - Cannot begin or end with a hyphen
     */
    private static final Pattern GITHUB_USERNAME_PATTERN =
            Pattern.compile("(?i)^[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}$");

    private final GitHubPreferencesDao gitHubPreferencesDao;

    public GitHubPreferencesViewModel() {
        addProperty(GIT_URL, "")
                .addValidator(GIT_URL, GIT_URL.name(), (ReadOnlyStringProperty prop, ValidationResult vr, ViewModel vm) -> {
                    if (prop.isEmpty().get()) {
                        vr.error("%s is required.".formatted(GIT_URL.getPropertyName()));
                    }

                    String url = prop.get();
                    Matcher matcher = URL_PATTERN.matcher(url);
                    if (!matcher.matches()) {
                        vr.error("%s is not a valid Git repository URL.".formatted(url));
                    }

                    // Clear any previous authentication errors
                    setPropertyValue(CONNECT_ERROR, "");
                });

        addProperty(GIT_EMAIL, "")
                .addValidator(GIT_EMAIL, GIT_EMAIL.name(), (ReadOnlyStringProperty prop, ValidationResult vr, ViewModel vm) -> {
                    if (prop.isNotEmpty().get()) {
                        String email = prop.get();
                        Matcher matcher = EMAIL_PATTERN.matcher(email);
                        if (!matcher.matches()) {
                            vr.error("%s is not a valid email address.".formatted(email));
                        }
                    }

                    // Clear any previous authentication errors
                    setPropertyValue(CONNECT_ERROR, "");
                });

        addProperty(GIT_USERNAME, "")
                .addValidator(GIT_USERNAME, GIT_USERNAME.name(), (ReadOnlyStringProperty prop, ValidationResult vr, ViewModel viewModel) -> {
                    if (prop.isEmpty().get()) {
                        vr.error("%s is required.".formatted(GIT_USERNAME.getPropertyName()));
                    }

                    final String username = prop.get();

                    // Check username length (max 39 characters)
                    if (username.length() > 39) {
                        vr.error("%s cannot exceed 39 characters.".formatted(GIT_USERNAME.getPropertyName()));
                    }

                    // Validate username format using regex pattern
                    final Matcher matcher = GITHUB_USERNAME_PATTERN.matcher(username);
                    if (!matcher.matches()) {
                        vr.error(("%s may only contain alphanumeric characters or single hyphens, and cannot begin " +
                                "or end with a hyphen.").formatted(GIT_USERNAME.getPropertyName()));
                    }

                    // Clear any previous authentication errors
                    setPropertyValue(CONNECT_ERROR, "");
                });

        addProperty(GIT_PASSWORD, "")
                .addValidator(GIT_PASSWORD, GIT_PASSWORD.name(), (ReadOnlyStringProperty prop, ValidationResult vr, ViewModel viewModel) -> {
                    if (prop.isEmpty().get()) {
                        vr.error("%s is required.".formatted(GIT_PASSWORD.getPropertyName()));
                    }

                    final String password = prop.get();

                    // Check minimum length (8 characters)
                    if (password.length() < 8) {
                        vr.error("%s must be at least 8 characters long.".formatted(GIT_PASSWORD.getPropertyName()));
                    }

                    // Check maximum length (100 characters)
                    if (password.length() > 100) {
                        vr.error("%s cannot exceed 100 characters.".formatted(GIT_PASSWORD.getPropertyName()));
                    }

                    // Check for uppercase letters
                    if (!password.matches(".*[A-Z].*")) {
                        vr.error("%s must contain at least one uppercase letter.".formatted(GIT_PASSWORD.getPropertyName()));
                    }

                    // Check for lowercase letters
                    if (!password.matches(".*[a-z].*")) {
                        vr.error("%s must contain at least one lowercase letter.".formatted(GIT_PASSWORD.getPropertyName()));
                    }

                    // Check for at least 2 digits
                    int digitCount = 0;
                    for (char c : password.toCharArray()) {
                        if (Character.isDigit(c)) {
                            digitCount++;
                        }
                    }
                    if (digitCount < 2) {
                        vr.error("%s must contain at least 2 digits.".formatted(GIT_PASSWORD.getPropertyName()));
                    }

                    // Check for spaces (not allowed)
                    if (password.contains(" ")) {
                        vr.error("%s cannot contain spaces.".formatted(GIT_PASSWORD.getPropertyName()));
                    }

                    // Clear any previous authentication errors
                    setPropertyValue(CONNECT_ERROR, "");
                });

        addProperty(CONNECT_ERROR, "");

        gitHubPreferencesDao = new GitHubPreferencesDao();
    }

    @Override
    public ValidationViewModel save() {
        final ValidationViewModel validationViewModel = super.save();

        try {
            gitHubPreferencesDao.save(getPropertyValue(GIT_URL),
                    getPropertyValue(GIT_EMAIL),
                    getPropertyValue(GIT_USERNAME),
                    getPropertyValue(GIT_PASSWORD).toString().toCharArray());
            reset();
        } catch (BackingStoreException ex) {
            setPropertyValue(CONNECT_ERROR, "Failed to save GitHub preferences: " + ex.getMessage());
        }

        return validationViewModel;
    }

    /**
     * Updates the error message for a specific property based on the provided validation message.
     *
     * @param validationMessage The validation message containing the property name and error details.
     */
    public void updateErrors(ValidationMessage validationMessage) {
        setPropertyValue(validationMessage.propertyName() + ERROR, validationMessage.interpolate(this));
    }
}