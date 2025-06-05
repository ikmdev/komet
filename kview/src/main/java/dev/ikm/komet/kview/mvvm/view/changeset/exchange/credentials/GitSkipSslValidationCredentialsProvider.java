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
 * A {@link CredentialsProvider} that will ignore any SSL validation errors that occur.
 */
public class GitSkipSslValidationCredentialsProvider extends CredentialsProvider {

    private static final Pattern FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("\\s*\\{\\d}\\s*");

    // Store session-level TRUST_NOW decisions per repository for the entire app session
    private static final ConcurrentHashMap<String, Boolean> sessionDecisions = new ConcurrentHashMap<>();

    private static String stripFormattingPlaceholders(String string) {
        return FORMAT_PLACEHOLDER_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Enum representing the user's SSL trust decision
     */
    public enum SslTrustDecision {
        TRUST_NOW("Trust Now"),
        TRUST_FOR_REPO("Trust for Repository"),
        TRUST_ALWAYS("Trust Always"),
        CANCEL("Cancel");

        private final String displayName;

        SslTrustDecision(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public boolean isInteractive() {
        return true;
    }

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
                "• Trust Now: Accept certificate for this session only\n" +
                     "• Trust for Repo: Accept certificate for this repository\n" +
                     "• Trust Always: Accept certificate permanently");
        explanationLabel.setFont(Font.font("Noto Sans", FontWeight.NORMAL, 10));
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