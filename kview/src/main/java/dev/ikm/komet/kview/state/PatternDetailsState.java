package dev.ikm.komet.kview.state;

import org.carlfx.axonic.State;

public class PatternDetailsState implements State {


    final String name;

    PatternDetailsState(String name) { this.name = name; }

    @Override
    public String getName() {
        return name;
    }
}
