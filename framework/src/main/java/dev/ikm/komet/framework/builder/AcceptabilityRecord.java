package dev.ikm.komet.framework.builder;

import io.soabase.recordbuilder.core.RecordBuilder;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.PatternFacade;

@RecordBuilder
public record AcceptabilityRecord(PatternFacade acceptabilityPattern, ConceptFacade acceptabilityValue)
        implements AcceptabilityRecordBuilder.With {

}
