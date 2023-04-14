package dev.ikm.komet.framework.builder;

import io.soabase.recordbuilder.core.RecordBuilder;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

@RecordBuilder
public record DescriptionBuilderRecord(ConceptFacade language, String text,
                                       ConceptFacade descriptionType,
                                       ConceptFacade caseSensitivity,
                                       AcceptabilityRecord... acceptabilityRecords)
        implements DescriptionBuilderRecordBuilder.With {
    public static DescriptionBuilderRecord makeRegularName(String text) {
        return new DescriptionBuilderRecord(TinkarTerm.ENGLISH_LANGUAGE,
                text,
                TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE,
                TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(TinkarTerm.US_DIALECT_PATTERN, TinkarTerm.PREFERRED),
                new AcceptabilityRecord(TinkarTerm.GB_DIALECT_PATTERN, TinkarTerm.PREFERRED));
    }

    public static DescriptionBuilderRecord makeFullyQualifiedName(String text) {
        return new DescriptionBuilderRecord(TinkarTerm.ENGLISH_LANGUAGE,
                text,
                TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE,
                TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(TinkarTerm.US_DIALECT_PATTERN, TinkarTerm.PREFERRED),
                new AcceptabilityRecord(TinkarTerm.GB_DIALECT_PATTERN, TinkarTerm.PREFERRED));
    }

    public static DescriptionBuilderRecord makeSynonym(String text) {
        return new DescriptionBuilderRecord(TinkarTerm.ENGLISH_LANGUAGE,
                text,
                TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE,
                TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                new AcceptabilityRecord(TinkarTerm.US_DIALECT_PATTERN, TinkarTerm.ACCEPTABLE),
                new AcceptabilityRecord(TinkarTerm.GB_DIALECT_PATTERN, TinkarTerm.ACCEPTABLE));
    }

}
