package dev.ikm.komet.kview.state.pattern;

import static dev.ikm.komet.kview.state.PatternDetailsState.ADD_FQN;
import static dev.ikm.komet.kview.state.PatternDetailsState.DEFINTIONS_DONE;
import static dev.ikm.komet.kview.state.PatternDetailsState.NEW_PATTERN_INITIAL;
import org.carlfx.axonic.StatePattern;

public class PatternDetailsPattern extends StatePattern {

    public PatternDetailsPattern() {
        this.initial(NEW_PATTERN_INITIAL)
                .t("completeDefintions", NEW_PATTERN_INITIAL, DEFINTIONS_DONE)
                // we can navigate to add fqn from the second pencil>context menu
                // therefore it can come from any previous state
                .t("addFqn", NEW_PATTERN_INITIAL, ADD_FQN)
                .t("addFqn", DEFINTIONS_DONE, ADD_FQN)
                ;

    }
}
