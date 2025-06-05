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
package dev.ikm.komet.kview.mvvm.view.changeset.exchange.credentials;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * A {@link CredentialsProvider} implementation that handles SSL certificate validation failures
 * by providing interactive user dialogs for trust decisions.
 *
 * <p>This provider specifically handles SSL-related credential items from JGit and presents
 * users with options to trust certificates at different scopes:
 * <ul>
 *   <li>Trust for current session only</li>
 *   <li>Trust for the specific repository</li>
 *   <li>Trust always (permanently)</li>
 * </ul>
 *
 * <p>The provider maintains session-level trust decisions in memory to avoid repeatedly
 * prompting users for the same repository within a single application session.
 *
 * @see CredentialsProvider
 * @see CredentialItem
 */
public class GitSkipSslValidationCredentialsProvider extends CredentialsProvider {

    /**
     * Pattern used to identify and strip formatting placeholders from JGit text messages.
     */
    private static final Pattern FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("\\s*\\{\\d}\\s*");

    /**
     * Session-level cache storing TRUST_NOW decisions per repository URI.
     * This prevents repeated prompting for the same repository within a single application session.
     *
     * <p>Key: Repository URI as string, Value: Boolean indicating if trust decision was made
     */
    private static final ConcurrentHashMap<String, Boolean> sessionDecisions = new ConcurrentHashMap<>();

    /**
     * Removes formatting placeholders from JGit internationalized text strings.
     *
     * <p>JGit uses placeholder patterns like {0}, {1} in its internationalized messages.
     * This method strips these placeholders and surrounding whitespace to enable
     * proper string matching.
     *
     * @param string the input string that may contain formatting placeholders
     * @return the string with all formatting placeholders removed
     */
    private static String stripFormattingPlaceholders(String string) {
        return FORMAT_PLACEHOLDER_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Enumeration representing the user's SSL certificate trust decision options.
     *
     * <p>Each decision has a different scope and persistence:
     * <ul>
     *   <li>{@link #TRUST_NOW} - Trust for current session only</li>
     *   <li>{@link #TRUST_FOR_REPO} - Trust for this specific repository</li>
     *   <li>{@link #TRUST_ALWAYS} - Trust permanently for all operations</li>
     *   <li>{@link #CANCEL} - Reject the certificate and cancel operation</li>
     * </ul>
     */
    public enum SslTrustDecision {
        /**
         * Trust the certificate for the current session only.
         */
        TRUST_NOW("Trust Now"),

        /**
         * Trust the certificate for this specific repository.
         */
        TRUST_FOR_REPO("Trust for Repository"),

        /**
         * Trust the certificate permanently for all future operations.
         */
        TRUST_ALWAYS("Trust Always"),

        /**
         * Cancel the operation and do not trust the certificate.
         */
        CANCEL("Cancel");

        /**
         * The human-readable display name for this decision option.
         */
        private final String displayName;

        /**
         * Constructs an SSL trust decision with the specified display name.
         *
         * @param displayName the human-readable name to display in UI components
         */
        SslTrustDecision(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns the human-readable display name for this trust decision.
         *
         * @return the display name suitable for showing in user interfaces
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Indicates whether this credentials provider supports interactive operations.
     *
     * <p>This provider requires user interaction to make SSL trust decisions,
     * so it always returns {@code true}.
     *
     * @return {@code true} always, as this provider requires user interaction
     */
    @Override
    public boolean isInteractive() {
        return true;
    }

    /**
     * Determines whether this provider can handle the given credential items.
     *
     * <p>This provider specifically supports:
     * <ul>
     *   <li>SSL failure informational messages</li>
     *   <li>SSL trust decision prompts (now, for repo, always)</li>
     * </ul>
     *
     * <p>Any credential items that are not SSL-related will cause this method
     * to return {@code false}.
     *
     * @param items the credential items to evaluate for support
     * @return {@code true} if all items are SSL-related and can be handled,
     * {@code false} otherwise
     */
    @Override
    public boolean supports(CredentialItem... items) {
        final MutableList<CredentialItem> unprocessedItems = Lists.mutable.empty();

        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.InformationalMessage && item.getPromptText() != null
                    && item.getPromptText().contains(JGitText.get().sslFailureTrustExplanation)) {
                continue;
            }

            if (item instanceof CredentialItem.YesNoType && item.getPromptText() != null
                    && (item.getPromptText().equals(JGitText.get().sslTrustNow)
                    || item.getPromptText()
                    .startsWith(stripFormattingPlaceholders(JGitText.get().sslTrustForRepo))
                    || item.getPromptText().equals(JGitText.get().sslTrustAlways))) {
                continue;
            }

            unprocessedItems.add(item);
        }

        return unprocessedItems.isEmpty();
    }

    /**
     * Processes the given credential items and provides values based on user decisions.
     *
     * <p>For SSL-related credential items, this method will:
     * <ol>
     *   <li>Check for existing session-level trust decisions</li>
     *   <li>Present an interactive dialog if no prior decision exists</li>
     *   <li>Store session-level decisions for future use</li>
     *   <li>Set appropriate values on the credential items</li>
     * </ol>
     *
     * <p>If the user cancels the SSL trust dialog, this method returns {@code false}
     * to indicate that credentials could not be provided.
     *
     * @param uri   the URI of the repository being accessed
     * @param items the credential items that need to be populated
     * @return {@code true} if all credential items were successfully processed,
     * {@code false} if the user cancelled or an error occurred
     * @throws UnsupportedCredentialItem if any credential items cannot be handled
     *                                   by this provider
     */
    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        final MutableList<CredentialItem> unprocessedItems = Lists.mutable.empty();

        // Check if we have SSL-related items that need user decision
        boolean hasSSLItems = false;
        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.YesNoType) {
                final String prompt = item.getPromptText();
                if (prompt != null && (prompt.equals(JGitText.get().sslTrustNow)
                        || prompt.startsWith(stripFormattingPlaceholders(JGitText.get().sslTrustForRepo))
                        || prompt.equals(JGitText.get().sslTrustAlways))) {
                    hasSSLItems = true;
                    break;
                }
            }
        }

        // If we have SSL items, check for existing session trust or show the user dialog
        SslTrustDecision userDecision = null;
        if (hasSSLItems) {
            String repoKey = uri.toString();

            // Check if we have a session-level TRUST_NOW decision for this repository
            if (sessionDecisions.getOrDefault(repoKey, false)) {
                userDecision = SslTrustDecision.TRUST_NOW;
            }

            // If no existing TRUST_NOW decision, show dialog
            if (userDecision == null) {
                userDecision = showSslTrustDialog(uri);

                // Store the decision for this session only if it's TRUST_NOW
                if (userDecision == SslTrustDecision.TRUST_NOW) {
                    sessionDecisions.put(repoKey, true);
                }

                if (userDecision == SslTrustDecision.CANCEL) {
                    return false;
                }
            }
        }

        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.YesNoType yesNoItem) {
                final String prompt = yesNoItem.getPromptText();

                if (prompt == null) {
                    unprocessedItems.add(item);
                } else if (prompt.equals(JGitText.get().sslTrustNow)) {
                    yesNoItem.setValue(userDecision == SslTrustDecision.TRUST_NOW);
                } else if (prompt.startsWith(stripFormattingPlaceholders(JGitText.get().sslTrustForRepo))) {
                    yesNoItem.setValue(userDecision == SslTrustDecision.TRUST_FOR_REPO);
                } else if (prompt.equals(JGitText.get().sslTrustAlways)) {
                    yesNoItem.setValue(userDecision == SslTrustDecision.TRUST_ALWAYS);
                } else {
                    unprocessedItems.add(item);
                }

            } else if (item instanceof CredentialItem.InformationalMessage
                    && item.getPromptText() != null
                    && item.getPromptText().contains(JGitText.get().sslFailureTrustExplanation)) {
                // Informational messages about SSL are handled by showing them in our dialog
                continue;
            } else {
                unprocessedItems.add(item);
            }
        }

        if (unprocessedItems.isEmpty()) {
            return true;
        }

        throw new UnsupportedCredentialItem(uri, unprocessedItems.size() + " credential items not supported");
    }

    /**
     * Displays an SSL trust decision dialog, handling JavaFX threading requirements.
     *
     * @param uri the repository URI for which SSL trust is being requested
     * @return the user's trust decision, or {@link SslTrustDecision#CANCEL}
     * if an error occurs or the operation is interrupted
     */
    private SslTrustDecision showSslTrustDialog(URIish uri) {
        if (Platform.isFxApplicationThread()) {
            return showDialogOnFxThread(uri);
        } else {
            final AtomicReference<SslTrustDecision> result = new AtomicReference<>();
            final CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    result.set(showDialogOnFxThread(uri));
                } catch (Exception e) {
                    result.set(SslTrustDecision.CANCEL);
                } finally {
                    latch.countDown();
                }
            });

            try {
                latch.await();
                return result.get();
            } catch (InterruptedException e) {
                return SslTrustDecision.CANCEL;
            }
        }
    }

    /**
     * Creates and displays the SSL trust dialog.
     *
     * <p>The dialog presents the user with four options:
     * <ul>
     *   <li>Trust Now - Accept certificate for current session</li>
     *   <li>Trust for Repository - Accept certificate for this specific repository</li>
     *   <li>Trust Always - Accept certificate permanently</li>
     *   <li>Cancel - Reject certificate and cancel operation</li>
     * </ul>
     *
     * <p>The dialog includes repository information and explanatory text to help
     * users make informed decisions about certificate trust.
     *
     * @param uri the repository URI for which SSL trust is being requested
     * @return the user's trust decision based on which button was clicked,
     * or {@link SslTrustDecision#CANCEL} if the dialog was closed
     * without a selection
     */
    private SslTrustDecision showDialogOnFxThread(URIish uri) {
        // Create custom button types
        ButtonType trustNowButtonType = new ButtonType(SslTrustDecision.TRUST_NOW.getDisplayName());
        ButtonType trustForRepoButtonType = new ButtonType(SslTrustDecision.TRUST_FOR_REPO.getDisplayName());
        ButtonType trustAlwaysButtonType = new ButtonType(SslTrustDecision.TRUST_ALWAYS.getDisplayName());
        ButtonType cancelButtonType = new ButtonType(SslTrustDecision.CANCEL.getDisplayName());

        // Create the alert dialog
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("SSL Certificate Validation");
        alert.setHeaderText("SSL Certificate validation failed");

        // Create custom content
        VBox content = new VBox(8);

        Label uriLabel = new Label("Repository: " + uri.toString());
        uriLabel.setFont(Font.font("Noto Sans Bold", FontWeight.BOLD, 13));

        Label messageLabel = new Label("How would you like to proceed?");
        messageLabel.setFont(Font.font("Noto Sans", FontWeight.NORMAL, 13));

        Label explanationLabel = new Label(
                """
                        • Trust Now: Accept certificate for this session only
                        • Trust for Repo: Accept certificate for this repository
                        • Trust Always: Accept certificate permanently""");
        explanationLabel.setFont(Font.font("Noto Sans", FontWeight.NORMAL, 11));
        explanationLabel.setStyle("-fx-text-fill: #666666;");

        content.getChildren().addAll(uriLabel, messageLabel, explanationLabel);
        alert.getDialogPane().setContent(content);

        // Set custom buttons
        alert.getButtonTypes().setAll(trustNowButtonType, trustForRepoButtonType, trustAlwaysButtonType, cancelButtonType);

        // Set default button
        Button trustNowButton = (Button) alert.getDialogPane().lookupButton(trustNowButtonType);
        trustNowButton.setDefaultButton(true);

        // Show dialog and get result
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            ButtonType buttonType = result.get();
            if (buttonType == trustNowButtonType) {
                return SslTrustDecision.TRUST_NOW;
            } else if (buttonType == trustForRepoButtonType) {
                return SslTrustDecision.TRUST_FOR_REPO;
            } else if (buttonType == trustAlwaysButtonType) {
                return SslTrustDecision.TRUST_ALWAYS;
            }
        }

        return SslTrustDecision.CANCEL;
    }
}