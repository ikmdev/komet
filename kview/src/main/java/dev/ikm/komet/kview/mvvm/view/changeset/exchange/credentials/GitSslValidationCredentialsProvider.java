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
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Interactive credentials provider that handles SSL certificate validation failures.
 * <p>
 * This provider presents user dialogs when SSL certificate validation fails, allowing users to
 * choose appropriate trust levels for their Git operations. It maintains session-level decisions
 * to avoid repeated prompts for the same repository during the application session, creating a
 * smoother user experience while maintaining security awareness.
 *
 * @see CredentialsProvider
 * @see CredentialItem
 */
public class GitSslValidationCredentialsProvider extends CredentialsProvider {

    /**
     * Pattern for removing JGit message formatting placeholders from internationalized text.
     */
    private static final Pattern FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("\\s*\\{\\d}\\s*");

    /**
     * Session-level SSL trust decisions per repository URL.
     * This prevents repeated prompting for the same repository within a single application session.
     *
     * <p>Key: Repository URI as string, Value: Boolean indicating if trust decision was made
     */
    private static final ConcurrentHashMap<String, Boolean> sessionDecisions = new ConcurrentHashMap<>();

    /**
     * Removes JGit formatting placeholders from internationalized message strings.
     * <p>
     * JGit uses placeholder patterns like {0}, {1} in its internationalized messages
     * that need to be cleaned up for proper string matching in our SSL detection logic.
     *
     * @param string the input string that may contain formatting placeholders
     * @return the string with all formatting placeholders removed
     */
    private static String stripFormattingPlaceholders(String string) {
        return FORMAT_PLACEHOLDER_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * SSL certificate trust decision options available to users.
     * <p>
     * These options provide a graduated approach to SSL trust, allowing users to choose
     * the appropriate level of trust based on their security requirements and the
     * specific context of their Git operations.
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

    @Override
    public boolean supports(CredentialItem... items) {
        return Lists.immutable.of(items).allSatisfy(this::isItemSupported);
    }

    /**
     * Determines if a credential item is SSL-related and supported by this provider.
     * <p>
     * This provider specifically handles SSL certificate validation failures, so it
     * only supports credential items related to SSL trust decisions. Other credential
     * types (like username/password) are handled by different providers.
     *
     * @param item the credential item to check
     * @return {@code true} if the item is SSL-related and supported
     */
    private boolean isItemSupported(CredentialItem item) {
        return isSslInformationalMessage(item) || isSslYesNoType(item);
    }

    /**
     * Checks if the item is an SSL-related informational message.
     * <p>
     * JGit provides informational messages explaining SSL trust failures before
     * presenting trust options. We support these to provide complete SSL handling.
     *
     * @param item the credential item to check
     * @return {@code true} if the item contains SSL trust explanation text
     */
    private boolean isSslInformationalMessage(CredentialItem item) {
        return item instanceof CredentialItem.InformationalMessage &&
                item.getPromptText() != null &&
                item.getPromptText().contains(JGitText.get().sslFailureTrustExplanation);
    }

    /**
     * Checks if the item is SSL-related yes/no prompt.
     * <p>
     * JGit presents SSL trust decisions as yes/no questions with specific prompt text
     * patterns. This method identifies those patterns using JGit's internationalized
     * text constants to ensure compatibility across different locales.
     *
     * @param item the credential item to check
     * @return {@code true} if the item is an SSL trust prompt
     */
    private boolean isSslYesNoType(CredentialItem item) {
        if (!(item instanceof CredentialItem.YesNoType) || item.getPromptText() == null) {
            return false;
        }

        final String prompt = item.getPromptText();
        return prompt.equals(JGitText.get().sslTrustNow) ||
                prompt.startsWith(stripFormattingPlaceholders(JGitText.get().sslTrustForRepo)) ||
                prompt.equals(JGitText.get().sslTrustAlways);
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        final boolean hasSSLItems = Lists.immutable.of(items)
                .selectInstancesOf(CredentialItem.YesNoType.class)
                .anySatisfy(this::isSSLYesNoItem);

        // Get user decision for SSL items if present
        SslTrustDecision userDecision = null;
        if (hasSSLItems) {
            userDecision = determineUserDecision(uri);
            if (userDecision == SslTrustDecision.CANCEL) {
                // User chose to cancel - abort the Git operation
                return false;
            }
        }

        // Apply the user's decision to all SSL yes/no items
        final SslTrustDecision finalDecision = userDecision;
        Lists.immutable.of(items)
                .selectInstancesOf(CredentialItem.YesNoType.class)
                .forEach(item -> applySslDecision(item, finalDecision));

        // Validate that this provider supports all credential items
        final ImmutableList<CredentialItem> unsupportedItems = Lists.immutable.of(items).reject(this::isItemSupported);
        if (unsupportedItems.notEmpty()) {
            throw new UnsupportedCredentialItem(uri,
                    unsupportedItems.size() + " credential items not supported");
        }

        return true;
    }

    /**
     * Determines if a YesNoType item is specifically SSL-related.
     * <p>
     * This method provides more specific SSL detection for YesNoType items,
     * complementing the general SSL detection in isSslYesNoType.
     *
     * @param item the yes/no credential item to check
     * @return {@code true} if the item is SSL-related
     */
    private boolean isSSLYesNoItem(CredentialItem.YesNoType item) {
        final String prompt = item.getPromptText();
        return prompt != null && (prompt.equals(JGitText.get().sslTrustNow) ||
                prompt.startsWith(stripFormattingPlaceholders(JGitText.get().sslTrustForRepo)) ||
                prompt.equals(JGitText.get().sslTrustAlways));
    }

    /**
     * Determines the user's SSL trust decision, checking cache first, then prompting if needed.
     * <p>
     * This method implements a caching strategy to avoid repeatedly prompting users for
     * the same repository during a session. If no cached decision exists, it presents
     * the SSL trust dialog to get a fresh decision from the user.
     *
     * @param uri the repository URI requiring SSL trust decision
     * @return the user's trust decision
     */
    private SslTrustDecision determineUserDecision(URIish uri) {
        final String repoKey = uri.toString();

        // Check if we have a cached session decision for this repository
        // If found, return TRUST_NOW since that's what session caching represents
        return sessionDecisions.getOrDefault(repoKey, false)
                ? SslTrustDecision.TRUST_NOW : getUserDecisionFromDialog(uri, repoKey);
    }

    /**
     * Gets user decision from dialog and manages session-level caching.
     * <p>
     * This method coordinates between the dialog presentation and the session cache,
     * ensuring that TRUST_NOW decisions are cached to avoid repeated prompts during
     * the same application session.
     *
     * @param uri     the repository URI
     * @param repoKey the repository cache key (typically the URI string)
     * @return the user's trust decision
     */
    private SslTrustDecision getUserDecisionFromDialog(URIish uri, String repoKey) {
        final SslTrustDecision decision = showSslTrustDialog(uri);

        // Cache TRUST_NOW decisions for the current session to improve user experience
        // Other decisions (TRUST_FOR_REPO, TRUST_ALWAYS) are handled by JGit itself
        if (decision == SslTrustDecision.TRUST_NOW) {
            sessionDecisions.put(repoKey, true);
        }

        return decision;
    }

    /**
     * Applies the SSL trust decision to a yes/no credential item.
     * <p>
     * This method translates the user's high-level trust decision into the specific
     * yes/no responses that JGit expects for each type of SSL trust prompt. The
     * switch expression provides clear mapping between decision types and responses.
     *
     * @param item     the credential item to update with the decision
     * @param decision the user's trust decision, may be {@code null} for non-SSL items
     */
    private void applySslDecision(CredentialItem.YesNoType item, SslTrustDecision decision) {
        final String prompt = item.getPromptText();
        if (prompt == null) {
            return;
        }

        // Map the user's decision to the appropriate yes/no response for each prompt type
        final boolean value = switch (prompt) {
            case String p when p.equals(JGitText.get().sslTrustNow) -> decision == SslTrustDecision.TRUST_NOW;
            case String p when p.startsWith(stripFormattingPlaceholders(JGitText.get().sslTrustForRepo)) ->
                    decision == SslTrustDecision.TRUST_FOR_REPO;
            case String p when p.equals(JGitText.get().sslTrustAlways) -> decision == SslTrustDecision.TRUST_ALWAYS;
            default -> false;
        };

        item.setValue(value);
    }

    /**
     * Shows the SSL trust dialog, handling JavaFX threading appropriately.
     *
     * @param uri the repository URI requiring SSL trust decision
     * @return the user's trust decision
     */
    private SslTrustDecision showSslTrustDialog(URIish uri) {
        if (Platform.isFxApplicationThread()) {
            return safeShowDialog(uri);
        }

        CompletableFuture<SslTrustDecision> dialogResult = new CompletableFuture<>();
        Platform.runLater(() -> dialogResult.complete(safeShowDialog(uri)));
        return dialogResult.join();
    }

    /**
     * Safely shows the SSL dialog, handling any exceptions that might occur.
     *
     * @param uri the repository URI requiring SSL trust decision
     * @return the user's trust decision, or CANCEL if an error occurs
     */
    private SslTrustDecision safeShowDialog(URIish uri) {
        try {
            return showDialogOnFxThread(uri);
        } catch (Exception e) {
            return SslTrustDecision.CANCEL;
        }
    }

    /**
     * Shows the SSL trust dialog on the JavaFX Application Thread.
     *
     * @param uri the repository URI requiring SSL trust decision
     * @return the user's trust decision
     */
    private SslTrustDecision showDialogOnFxThread(URIish uri) {
        final Alert alert = createSslAlert(uri);

        return alert.showAndWait()
                .map(this::mapButtonTypeToDecision)
                .orElse(SslTrustDecision.CANCEL);
    }

    /**
     * Creates the SSL validation alert dialog with custom content and styling.
     *
     * @param uri the repository URI with SSL issues
     * @return the configured alert dialog ready for display
     */
    private Alert createSslAlert(URIish uri) {
        final Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("SSL Certificate Validation");
        alert.setHeaderText("SSL Certificate validation failed");
        alert.getDialogPane().setStyle("-fx-background-color: -Grey-1");

        final VBox content = new VBox(8);
        content.getChildren().addAll(createRepositoryLabel(uri), createMessageLabel(), createExplanationLabel());
        alert.getDialogPane().setContent(content);

        final ImmutableList<ButtonType> buttonTypes = Lists.immutable.of(SslTrustDecision.values())
                .collect(decision -> new ButtonType(decision.getDisplayName()));
        alert.getButtonTypes().setAll(buttonTypes.castToList());

        // Set "Trust Now" as the default button since it's the most secure option
        // that still allows the operation to proceed
        buttonTypes.detectOptional(buttonType ->
                        buttonType.getText().equals(SslTrustDecision.TRUST_NOW.getDisplayName()))
                .ifPresent(defaultButtonType -> {
                    final Button defaultButton = (Button) alert.getDialogPane().lookupButton(defaultButtonType);
                    if (defaultButton != null) {
                        defaultButton.setDefaultButton(true);
                    }
                });

        return alert;
    }

    /**
     * Creates a styled repository label for the SSL dialog.
     *
     * @param uri the repository URI to display
     * @return the configured label with appropriate styling
     */
    private Label createRepositoryLabel(URIish uri) {
        final Label label = new Label("Repository: " + uri.toString());
        label.setFont(Font.font("Noto Sans Bold", FontWeight.BOLD, 13));
        return label;
    }

    /**
     * Creates the main message label for the SSL dialog.
     *
     * @return the configured message label
     */
    private Label createMessageLabel() {
        final Label label = new Label("How would you like to proceed?");
        label.setFont(Font.font("Noto Sans", FontWeight.NORMAL, 12));
        return label;
    }

    /**
     * Creates the explanation label describing trust options.
     *
     * @return the configured explanation label
     */
    private Label createExplanationLabel() {
        final Label explanationLabel = new Label(
                """
                        • Trust Now: Accept certificate for this session only
                        • Trust for Repo: Accept certificate for this repository
                        • Trust Always: Accept certificate permanently""");
        explanationLabel.setFont(Font.font("Noto Sans", FontWeight.NORMAL, 12));
        return explanationLabel;
    }

    /**
     * Maps a dialog ButtonType to the corresponding SslTrustDecision.
     *
     * @param buttonType the button that was pressed by the user
     * @return the corresponding trust decision, or null if no match found
     */
    private SslTrustDecision mapButtonTypeToDecision(ButtonType buttonType) {
        return Lists.immutable.of(SslTrustDecision.values())
                .detect(decision -> decision.getDisplayName().equals(buttonType.getText()));
    }
}