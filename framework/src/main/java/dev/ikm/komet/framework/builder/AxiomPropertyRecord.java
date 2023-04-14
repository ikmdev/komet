package dev.ikm.komet.framework.builder;

import io.soabase.recordbuilder.core.RecordBuilder;
import dev.ikm.tinkar.terms.ConceptFacade;

/**
 * TODO: Deprecate?
 */
@RecordBuilder
public record AxiomPropertyRecord(ConceptFacade propertyMeaning, Object propertyValue)
        implements AxiomPart, AxiomPropertyRecordBuilder.With {
}
