package dev.ikm.komet.kview.klfields;

import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.Optional;

public enum KlFieldType {

    STRING(TinkarTerm.STRING.nid()),
    INTEGER(TinkarTerm.INTEGER_FIELD.nid()),
    FLOAT(TinkarTerm.FLOAT_FIELD.nid()),
    BOOLEAN(TinkarTerm.BOOLEAN_FIELD.nid()),

    IMAGE(TinkarTerm.IMAGE_FIELD.nid()),

    COMPONENT(TinkarTerm.COMPONENT_FIELD.nid()),
    C_SET(TinkarTerm.COMPONENT_ID_SET_FIELD.nid()),
    C_LIST(TinkarTerm.COMPONENT_ID_LIST_FIELD.nid());

    private int nid;

    KlFieldType(int nid) {
        this.nid = nid;
    }

    public static Optional<KlFieldType> of(int nid) {
        for (KlFieldType klFieldType : values()) {
            if (nid == klFieldType.nid) {
                return Optional.of(klFieldType);
            }
        }
        return Optional.empty();
    }

    public int getNid() { return nid; }
}