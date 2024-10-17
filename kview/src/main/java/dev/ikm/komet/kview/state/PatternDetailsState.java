package dev.ikm.komet.kview.state;

import org.carlfx.axonic.State;

public enum PatternDetailsState implements State {

    NEW_PATTERN_INITIAL("NewPatternInitial"),

    DEFINTIONS_DONE("DefintionsDone"),

    ADDING_FQN("AddFQN"),

    ADDED_FQN("AddedFQN"),

    ADDING_OTHER_NAME("AddingOtherName"),

    ADDED_OTHER_NAME("AddedOtherName")
    ;

    final String name;

    PatternDetailsState(String name) { this.name = name; }

    @Override
    public String getName() {
        return name;
    }
}
