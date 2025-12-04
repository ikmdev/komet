package dev.ikm.komet.kview.state;

import org.carlfx.axonic.State;

public enum PatternDetailsState implements State {

    NEW_PATTERN_INITIAL("NewPatternInitial"),

    ADDING_DEFINITIONS("DefintionsDone"),

    ADDED_DEFINTIONS("AddedDefinitions"),

    ADDING_FQN("AddFQN"),

    ADDED_FQN("AddedFQN"),

    ADDING_OTHER_NAME("AddingOtherName"),

    ADDED_OTHER_NAME("AddedOtherName"),

    ADDING_FIELD("AddingField"),

    EDITING_FIELD("EditingField"),

    EDITING_OTHERNAME("EditingOtherName")
    ;

    final String name;

    PatternDetailsState(String name) { this.name = name; }

    @Override
    public String getName() {
        return name;
    }
}
