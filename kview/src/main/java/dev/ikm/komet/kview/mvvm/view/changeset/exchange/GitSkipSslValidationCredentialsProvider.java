package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * A {@link CredentialsProvider} that will ignore any SSL validation errors that occur.
 */
public class GitSkipSslValidationCredentialsProvider extends CredentialsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GitSkipSslValidationCredentialsProvider.class);

    private static final Pattern FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("\\s*\\{\\d}\\s*");

    private static String stripFormattingPlaceholders(String string) {
        return FORMAT_PLACEHOLDER_PATTERN.matcher(string).replaceAll("");
    }

    @Override
    public boolean isInteractive() {
        return false;
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

        LOG.info("Skipping SSL validation for URI: {}", uri);

        for (CredentialItem item : items) {
            if (item instanceof CredentialItem.YesNoType) {
                final CredentialItem.YesNoType yesNoItem = (CredentialItem.YesNoType) item;
                final String prompt = yesNoItem.getPromptText();
                if (prompt == null) {
                    unprocessedItems.add(item);
                }
                else if (prompt.equals(JGitText.get().sslTrustNow)
                        || prompt.startsWith(stripFormattingPlaceholders(JGitText.get().sslTrustForRepo))) {
                    yesNoItem.setValue(true);
                }
                else if (prompt.equals(JGitText.get().sslTrustAlways)) {
                    yesNoItem.setValue(false);
                }
                else {
                    unprocessedItems.add(item);
                }
                LOG.info("Prompt: {}, YesNoType: {}", prompt, yesNoItem.getValue());
            }
            else if (!item.getPromptText().contains(JGitText.get().sslFailureTrustExplanation)) {
                unprocessedItems.add(item);
            }
        }

        if (unprocessedItems.isEmpty()) {
            return true;
        }

        throw new UnsupportedCredentialItem(uri, unprocessedItems.size() + " credential items not supported");
    }
}
