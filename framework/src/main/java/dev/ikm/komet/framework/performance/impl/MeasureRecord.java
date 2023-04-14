package dev.ikm.komet.framework.performance.impl;

import dev.ikm.komet.framework.performance.Measure;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.util.Optional;

public record MeasureRecord(Float lowerBound, boolean includeLowerBound, Float upperBound, boolean includeUpperBound,
                            Float resolution, ConceptFacade measureSemantic) implements Measure {
    @Override
    public float getLowerBound() {
        return lowerBound;
    }

    @Override
    public float getUpperBound() {
        return upperBound;
    }

    @Override
    public Optional<Float> getResolution() {
        return Optional.ofNullable(resolution);
    }

    @Override
    public Optional<ConceptFacade> getMeasureSemantic() {
        return Optional.ofNullable(measureSemantic);
    }
}
