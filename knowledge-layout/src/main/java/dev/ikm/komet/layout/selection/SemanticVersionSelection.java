package dev.ikm.komet.layout.selection;

import dev.ikm.komet.layout.KlTerms;
import dev.ikm.komet.layout.selection.element.FieldValueSelection;
import dev.ikm.komet.layout.selection.element.StampElementSelection;
import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.ConceptEnumerationFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;

import java.util.Optional;

/**
 * Represents the selectedAttribute of a particular version of a semantic component identified by a stamp.
 * This record includes multiple attributes that define the characteristics and metadata of the semantic version.
 * It implements the VersionSelection interface, ensuring compliance with version selectedAttribute criteria.
 *
 * @param stampPublicId           the public identifier of the version's stamp.
 * @param selectedStampElements     an immutable list of fields selected for the version's stamp.
 * @param selectedFieldIndexes    an immutable list of field indexes selected for this semantic version.
 */
public record SemanticVersionSelection(PublicId componentPublicId, PublicId stampPublicId,
                                       ImmutableList<StampElement> selectedStampElements,
                                       ImmutableIntList selectedFieldIndexes)
        implements VersionSelection {

    public enum FieldValue implements ConceptEnumerationFacade<FieldValue> {

        FIELD_VALUE(KlTerms.FIELD_VALUE);

        final EntityProxy.Concept conceptForEnum;

        FieldValue(EntityProxy.Concept conceptForEnum) {
            this.conceptForEnum = conceptForEnum;
        }

        @Override
        public EntityProxy.Concept conceptForEnum() {
            return conceptForEnum;
        }

        @Decoder
        public static FieldValue decode(DecoderInput in) {
            return ConceptEnumerationFacade.decode(in, FieldValue.class);
        }
    }

    @Override
    public ImmutableList<SelectableElement> dataElements() {
        MutableList<SelectableElement> selectableProperties = Lists.mutable.of();
        for (StampElement selection: StampElement.values()) {
            selectableProperties.add(new StampElementSelection(selection));
        }
        Optional<SemanticEntity<SemanticEntityVersion>> optionalSemantic = EntityService.get().getEntity(componentPublicId);
        optionalSemantic.ifPresent(semanticEntity -> {
           // Attribute vs FIELD vs Property... Use JavaFx properties? AttributeProperties? FieldProperties?
            semanticEntity.getVersion(stampPublicId).ifPresent(entityVersion -> {
                for (int index = 0; index < entityVersion.fieldValues().size(); index++) {
                    selectableProperties.add(new FieldValueSelection(index, FieldValue.FIELD_VALUE));
                }
            });
        });
        return selectableProperties.toImmutable();
    }

    @Override
    public ImmutableList<SelectableElement> selectedDataElements() {
        MutableList<SelectableElement> selectedDataElements = Lists.mutable.of();
        for (StampElement selection: selectedStampElements) {
            selectedDataElements.add(new StampElementSelection(selection));
        }
        selectedFieldIndexes.forEach(index ->
                selectedDataElements.add(new FieldValueSelection(index, FieldValue.FIELD_VALUE)));

        return selectedDataElements.toImmutable();
    }

}
