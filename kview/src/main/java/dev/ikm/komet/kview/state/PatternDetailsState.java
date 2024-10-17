package dev.ikm.komet.kview.state;

import org.carlfx.axonic.State;

public enum PatternDetailsState implements State {

    NEW_PATTERN_INITIAL("NewPatternInitial"),

    DEFINTIONS_DONE("DefintionsDone"),

    ADD_FQN("AddFQN")
    ;

    final String name;

    PatternDetailsState(String name) { this.name = name; }

    @Override
    public String getName() {
        return name;
    }
}
