package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.terms.KometTerm;
import dev.ikm.tinkar.terms.KernelTerm;

import java.util.Optional;

public enum KlFieldType {

    STRING(KernelTerm.STRING.nid()),
    INTEGER(KernelTerm.INTEGER_FIELD.nid()),
    FLOAT(KernelTerm.FLOAT_FIELD.nid()),
    BOOLEAN(KernelTerm.BOOLEAN_FIELD.nid()),

    IMAGE(KometTerm.IMAGE_FIELD.nid()),

    COMPONENT(KernelTerm.COMPONENT_FIELD.nid()),
    C_SET(KernelTerm.COMPONENT_ID_SET_FIELD.nid()),
    C_LIST(KernelTerm.COMPONENT_ID_LIST_FIELD.nid());

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