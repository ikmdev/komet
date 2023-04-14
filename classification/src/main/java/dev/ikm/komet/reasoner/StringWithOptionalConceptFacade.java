package dev.ikm.komet.reasoner;

import dev.ikm.tinkar.terms.ConceptFacade;

import java.util.Optional;

public class StringWithOptionalConceptFacade {
    final String label;
    final ConceptFacade conceptFacade;

    public StringWithOptionalConceptFacade(String label, ConceptFacade conceptFacade) {
        this.label = label;
        this.conceptFacade = conceptFacade;
    }
    public StringWithOptionalConceptFacade(String label) {
        this.label = label;
        this.conceptFacade = null;
    }

    public String getLabel() {
        return label;
    }

    public Optional<ConceptFacade> getOptionalConceptSpecification() {
        return Optional.ofNullable(conceptFacade);
    }

    @Override
    public String toString() {
        return label;
    }
}