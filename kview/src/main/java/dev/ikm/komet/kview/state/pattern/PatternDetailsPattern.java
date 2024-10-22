package dev.ikm.komet.kview.state.pattern;

import static dev.ikm.komet.kview.state.PatternDetailsState.ADDED_DEFINTIONS;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDED_FQN;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDED_OTHER_NAME;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDING_FIELD;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDING_FQN;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDING_OTHER_NAME;
import static dev.ikm.komet.kview.state.PatternDetailsState.ADDING_DEFINITIONS;
import static dev.ikm.komet.kview.state.PatternDetailsState.EDITING_FIELD;
import static dev.ikm.komet.kview.state.PatternDetailsState.NEW_PATTERN_INITIAL;
import org.carlfx.axonic.StatePattern;

public class PatternDetailsPattern extends StatePattern {

    public PatternDetailsPattern() {
        this.initial(NEW_PATTERN_INITIAL)
                .t("addDefinitions", NEW_PATTERN_INITIAL, ADDING_DEFINITIONS)

                // we can navigate to add definitions from the first pencil
                // therefore it can come from these other states
                .t("addDefinitions", ADDING_FQN, ADDING_DEFINITIONS)
                .t("addDefinitions", ADDED_FQN, ADDING_DEFINITIONS)
                .t("addDefinitions", ADDING_OTHER_NAME, ADDING_DEFINITIONS)
                .t("addDefinitions", ADDED_OTHER_NAME, ADDING_DEFINITIONS)
                .t("addDefinitions", ADDING_FIELD, ADDING_DEFINITIONS)

                .t("definitionsDone", ADDING_DEFINITIONS, ADDED_DEFINTIONS)

                // we can navigate to add fqn from the second pencil>context menu
                // therefore it can come from these other states
                .t("addFqn", NEW_PATTERN_INITIAL, ADDING_FQN)
                .t("addFqn", ADDING_DEFINITIONS, ADDING_FQN)
                .t("addFqn", ADDED_DEFINTIONS, ADDING_FQN)
                .t("addFqn", ADDING_OTHER_NAME, ADDING_FQN)
                .t("addFqn", ADDED_OTHER_NAME, ADDING_FQN)

                // after we add the FQN, we want to be in the state ADDED_FQN
                .t("fqnDone", ADDING_FQN, ADDED_FQN)

                // we can navigate to add other name from the second pencil>context menu
                // therefore it can come from these other states
                .t("addOtherName", NEW_PATTERN_INITIAL, ADDING_OTHER_NAME)
                .t("addOtherName", ADDING_DEFINITIONS, ADDING_OTHER_NAME)
                .t("addOtherName", ADDED_FQN, ADDING_OTHER_NAME)
                .t("addOtherName", ADDING_OTHER_NAME, ADDING_OTHER_NAME)
                .t("addOtherName", ADDING_FIELD, ADDING_OTHER_NAME)

                // after we add the Other Name, we want to be in the state ADDED_OTHER_NAME
                .t("otherNameDone", ADDING_OTHER_NAME, ADDED_OTHER_NAME)

                // we can navigate to the add field from the third pencil
                // therefore it can come from these other states
                .t("addField", NEW_PATTERN_INITIAL, ADDING_FIELD)
                .t("addField", ADDING_FQN, ADDING_FIELD)
                .t("addField", ADDED_FQN, ADDING_FIELD)
                .t("addField", ADDING_OTHER_NAME, ADDING_FIELD)
                .t("addField", ADDING_DEFINITIONS, ADDING_FIELD)
                .t("addField", ADDED_DEFINTIONS, ADDING_FIELD)
                .t("addField", ADDING_FIELD, ADDING_FIELD)
                // adding field gets you to a continuous loop to a confirmation panel
                // to keep adding fields, that is why there is no ADDED_FIELD

                // we can navigate to the edit field by right-clicking and choosing edit field
                // therefore it can come from these other states
                .t("editField", NEW_PATTERN_INITIAL, EDITING_FIELD)
                .t("editField", ADDING_DEFINITIONS, EDITING_FIELD)
                .t("editField", ADDED_DEFINTIONS, EDITING_FIELD)
                .t("editField", ADDING_FQN, EDITING_FIELD)
                .t("editField", ADDED_FQN, EDITING_FIELD)
                .t("editField", ADDING_OTHER_NAME, EDITING_FIELD)
                .t("editField", ADDED_OTHER_NAME, EDITING_FIELD)
                .t("editField", ADDING_FIELD, EDITING_FIELD)
                ;

    }
}
