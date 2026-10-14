package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.terms.KometConcept;
import dev.ikm.tinkar.terms.KernelConcept;

import java.util.Optional;

public enum KlFieldType {

    STRING(KernelConcept.STRING.nid()),
    INTEGER(KernelConcept.INTEGER_FIELD.nid()),
    FLOAT(KernelConcept.FLOAT_FIELD.nid()),
    BOOLEAN(KernelConcept.BOOLEAN_FIELD.nid()),

    IMAGE(KometConcept.IMAGE_FIELD.nid()),

    COMPONENT(KernelConcept.COMPONENT_FIELD.nid()),
    C_SET(KernelConcept.COMPONENT_ID_SET_FIELD.nid()),
    C_LIST(KernelConcept.COMPONENT_ID_LIST_FIELD.nid());

    private long nid;

    KlFieldType(long nid) {
        this.nid = nid;
    }

    public static Optional<KlFieldType> of(long nid) {
        for (KlFieldType klFieldType : values()) {
            if (nid == klFieldType.nid) {
                return Optional.of(klFieldType);
            }
        }
        return Optional.empty();
    }

    public long getNid() { return nid; }
}