package dev.ikm.komet.framework.rulebase;

import java.util.UUID;

public record ConsequenceAction(UUID consequenceUUID,
                                UUID ruleUUID,
                                GeneratedAction generatedAction) implements Consequence<GeneratedAction> {
    @Override
    public GeneratedAction get() {
        return generatedAction;
    }
}
