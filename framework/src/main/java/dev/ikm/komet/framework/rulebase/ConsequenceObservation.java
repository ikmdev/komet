package dev.ikm.komet.framework.rulebase;

import dev.ikm.komet.framework.performance.Observation;

import java.util.UUID;

public record ConsequenceObservation(UUID consequenceUUID,
                                     UUID ruleUUID,
                                     Observation newObservation) implements Consequence<Observation> {
    @Override
    public Observation get() {
        return newObservation;
    }
}
