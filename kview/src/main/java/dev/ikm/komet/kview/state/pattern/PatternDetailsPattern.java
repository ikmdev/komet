package dev.ikm.komet.kview.state.pattern;

import static dev.ikm.komet.kview.state.PatternDetailsState.ADDED_DEFINTIONS;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDED_FQN;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDED_OTHER_NAME;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDING_FQN;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDING_OTHER_NAME;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDING_DEFINITIONS;
import static dev.ikm.komet.kview.state.PatternDetailsState.NEW_PATTERN_INITIAL;
import org.carlfx.axonic.StatePattern;

public class PatternDetailsPattern extends StatePattern {

    public PatternDetailsPattern() {
        this.initial(NEW_PATTERN_INITIAL)
                .t("addDefinitions", NEW_PATTERN_INITIAL, ADDING_DEFINITIONS)
                .t("definitionsDone", ADDING_DEFINITIONS, ADDED_DEFINTIONS)

                // we can navigate to add fqn from the second pencil>context menu
                // therefore it can come from these other states
                .t("addFqn", NEW_PATTERN_INITIAL, ADDING_FQN)
                .t("addFqn", ADDING_DEFINITIONS, ADDING_FQN)
                .t("addFqn", ADDED_OTHER_NAME, ADDING_FQN)

                // after we add the FQN, we want to be in the state ADDED_FQN
                .t("fqnDone", ADDING_FQN, ADDED_FQN)

                // we can navigate to add other name from the second pencil>context menu
                // therefore it can come from these other states
                .t("addOtherName", NEW_PATTERN_INITIAL, ADDING_OTHER_NAME)
                .t("addOtherName", ADDING_DEFINITIONS, ADDING_OTHER_NAME)
                .t("addOtherName", ADDED_FQN, ADDING_OTHER_NAME)

                // after we add the Other Name, we want to be in the state ADDED_OTHER_NAME
                .t("otherNameDone", ADDING_OTHER_NAME, ADDED_OTHER_NAME)
                ;

    }
}
