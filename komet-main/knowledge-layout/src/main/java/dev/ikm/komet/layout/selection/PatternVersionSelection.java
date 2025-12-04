package dev.ikm.komet.layout.selection;

import dev.ikm.komet.layout.KlTerms;
import dev.ikm.komet.layout.selection.element.FieldDefinitionSelection;
import dev.ikm.komet.layout.selection.element.PatternAttributeSelection;
import dev.ikm.komet.layout.selection.element.StampElementSelection;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptEnumerationFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Optional;

/**
 * Represents the selectedAttribute of a particular version of a pattern identified by a stamp.
 * This record includes multiple attributes that define the characteristics and metadata of the pattern version.
 * It implements the VersionSelection interface, ensuring compliance with version selectedAttribute criteria.
 *
 * @param stampPublicId the public identifier of the version's stamp.
 * @param selectedStampElements an immutable list of fields selected for the version's stamp.
 * @param selectedPatternAttributes an immutable list of attributes that further describe the selected pattern.
 * @param selectedFieldDefinitions an immutable list of field definitions that are selected for this pattern version.
 */
public record PatternVersionSelection(PublicId componentPublicId, PublicId stampPublicId,
                                      ImmutableList<StampElement> selectedStampElements,
                                      ImmutableList<PatternAttribute> selectedPatternAttributes,
                                      ImmutableList<FieldDefinitionSelection> selectedFieldDefinitions)
        implements VersionSelection {

    private static ImmutableList<FieldAttribute> selectableFieldAttributes = Lists.immutable.of(FieldAttribute.values());

    /**
     * The PatternAttribute enum represents the various attributes that can be
     * selected within a pattern version. These attributes provide
     * additional context and metadata for the pattern.
     *
     * It includes the following constants:
     * - PATTERN_MEANING_ATTRIBUTE: Represents the meaning attribute of the pattern.
     * - PATTERN_PURPOSE_ATTRIBUTE: Represents the purpose attribute of the pattern.
     */
    public enum PatternAttribute implements ConceptEnumerationFacade<PatternAttribute> {

            PATTERN_MEANING(KlTerms.PATTERN_MEANING_ATTRIBUTE),
            PATTERN_PURPOSE(KlTerms.PATTERN_PURPOSE_ATTRIBUTE);

            final EntityProxy.Concept conceptForEnum;

            PatternAttribute(EntityProxy.Concept conceptForEnum) {
                this.conceptForEnum = conceptForEnum;
            }

            @Override
            public EntityProxy.Concept conceptForEnum() {
                return conceptForEnum;
            }

        @Decoder
        public static PatternAttribute decode(DecoderInput in) {
            return ConceptEnumerationFacade.decode(in, PatternAttribute.class);
        }

    }

    public enum FieldAttribute implements ConceptEnumerationFacade<FieldAttribute> {

        FIELD_MEANING(KlTerms.FIELD_MEANING),
        FIELD_PURPOSE(KlTerms.FIELD_PURPOSE),
        FIELD_DATA_TYPE(KlTerms.FIELD_DATA_TYPE);

        final EntityProxy.Concept conceptForEnum;

        FieldAttribute(EntityProxy.Concept conceptForEnum) {
            this.conceptForEnum = conceptForEnum;
        }

        @Override
        public EntityProxy.Concept conceptForEnum() {
            return conceptForEnum;
        }

        @Decoder
        public static FieldAttribute decode(DecoderInput in) {
            return ConceptEnumerationFacade.decode(in, FieldAttribute.class);
        }

    }

    @Override
    public ImmutableList<SelectableElement> dataElements() {
        MutableList<SelectableElement> selectableElements = Lists.mutable.of();
        for (StampElement selection: StampElement.values()) {
            selectableElements.add(new StampElementSelection(selection));
        }
        for (PatternAttribute patternAttribute: PatternAttribute.values()) {
            selectableElements.add(new PatternAttributeSelection(patternAttribute));
        }
        Optional<PatternEntity<PatternEntityVersion>> optionalPattern = EntityService.get().getEntity(componentPublicId);
        optionalPattern.ifPresent(patternEntity -> {
            patternEntity.getVersion(stampPublicId).ifPresent(patternVersion -> {
                selectableElements.add(new PatternAttributeSelection(PatternAttribute.PATTERN_MEANING));
                selectableElements.add(new PatternAttributeSelection(PatternAttribute.PATTERN_PURPOSE));
                for (int index = 0; index < patternVersion.fieldDefinitions().size(); index++) {
                    for (FieldDefinitionSelection fieldDefinitionSelection: selectedFieldDefinitions) {
                        selectableElements.add(fieldDefinitionSelection);
                    }
                }
            });
        });

        return selectableElements.toImmutable();
    }

    @Override
    public ImmutableList<SelectableElement> selectedDataElements() {
        MutableList<SelectableElement> selectedDataElements = Lists.mutable.of();
        for (StampElement selection: selectedStampElements) {
            selectedDataElements.add(new StampElementSelection(selection));
        }
        for (PatternAttribute patternAttribute: selectedPatternAttributes) {
            selectedDataElements.add(new PatternAttributeSelection(patternAttribute));

        }
        for (FieldDefinitionSelection fieldDefinitionSelection: selectedFieldDefinitions) {
            selectedDataElements.add(fieldDefinitionSelection);
        }
        return selectedDataElements.toImmutable();
    }
}
