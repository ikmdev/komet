package dev.ikm.komet.layout.selection;

import dev.ikm.tinkar.common.binary.Decoder;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.common.binary.Encodable;
import dev.ikm.tinkar.common.binary.EncoderOutput;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.terms.*;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * The VersionSelection interface defines a contract for entities that provide
 * version selectedAttribute based on various criteria. It is a sealed interface that is
 * only permitted to be implemented by specific classes such as
 * ConceptVersionSelection, PatternVersionSelection, and SemanticVersionSelection.
 */
public sealed interface VersionSelection
        permits ConceptVersionSelection, PatternVersionSelection, SemanticVersionSelection {
    /**
     * The StampElement enum represents the different fields that can be used to
     * specify aspects of a version stamp within the versioning system.
     *
     * It includes the following constants:
     * - STATUS: Represents the status field of a version stamp.
     * - TIME: Represents the time field of a version stamp.
     * - AUTHOR: Represents the author field of a version stamp.
     * - MODULE: Represents the module field of a version stamp.
     * - PATH: Represents the path field of a version stamp.
     */
    enum StampElement implements ConceptEnumerationFacade<StampElement> {
        STATUS(TinkarTerm.STATUS_FOR_VERSION),
        TIME(TinkarTerm.TIME_FOR_VERSION),
        AUTHOR(TinkarTerm.AUTHOR_FOR_VERSION),
        MODULE(TinkarTerm.MODULE_FOR_VERSION),
        PATH(TinkarTerm.PATH_FOR_VERSION);

        final ConceptFacade conceptForEnum;

        StampElement(EntityProxy.Concept conceptForEnum) {
            this.conceptForEnum = conceptForEnum;
        }

        public ConceptFacade conceptForEnum() {
            return conceptForEnum;
        }

        @Decoder
        public static StampElement decode(DecoderInput in) {
            return ConceptEnumerationFacade.decode(in, StampElement.class);
        }
    }

    /**
     * Returns the PublicId of the version's stamp.
     *
     * @return the PublicId of the version's stamp.
     */
    PublicId stampPublicId();

    /**
     * Returns an immutable list of selected stamp fields.
     *
     * @return an ImmutableList of selected StampElement.
     */
    ImmutableList<StampElement> selectedStampElements();

    ImmutableList<SelectableElement> dataElements();

    ImmutableList<SelectableElement> selectedDataElements();

}
