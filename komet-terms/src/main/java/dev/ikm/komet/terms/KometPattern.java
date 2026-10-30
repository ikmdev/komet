package dev.ikm.komet.terms;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.terms.PatternEnumerationFacade;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.UUID;

/**
 * The Komet bindings, generated apart and committed where they are used, of the knowledge set {@code d890e06f-ec35-429a-b541-d0ead19695e2} — DO NOT EDIT.
 * A component's identity is {@code T5(setUuid, fullyQualifiedNameAtBirth)} unless the ledger
 * adopted an established one; regenerate from the ledger.
 */
public enum KometPattern implements PatternEnumerationFacade<KometPattern> {

    /**
     * The definition of the action concept it references: the pattern each run's input takes, the pattern its result takes, and the instructions a model needs when it performs the action.
     */
    ACTION_DEFINITION_PATTERN("Action definition pattern (IkeFoundation)", "e2b99626-2ba5-5e24-a5ac-03378d63de0c"),

    /**
     * The input of an action that takes nothing but its subject, the referenced component: the kind of action performed on it.
     */
    COMPONENT_ACTION_INPUT_PATTERN("Component action input pattern (IkeFoundation)", "6fde6318-1f30-531c-8b0c-374baaf4a9e5"),

    /**
     * The input of an action that replaces the text of a document block, the referenced component, which is the block's element semantic: the kind of action performed on it and the block's new text.
     */
    DOCUMENT_BLOCK_EDIT_INPUT_PATTERN("Document block edit input pattern (IkeFoundation)", "4f7e66dc-ecad-5e59-a684-480a07be21ed"),

    /**
     * The input of an action that writes the tree of an ANF statement: the kind of action performed and the statement's tree. Its referenced component is the statement to edit, or the document block a new statement is added to.
     */
    ANF_STATEMENT_TREE_INPUT_PATTERN("ANF statement tree input pattern (IkeFoundation)", "d500cc84-978b-543b-9011-0fefa0166507"),

    /**
     * The input of an action that promotes an ANF statement's composed topic to a complex concept: the kind of action performed on the statement, the referenced component, and the name of the complex concept it creates.
     */
    COMPLEX_CONCEPT_PROMOTION_INPUT_PATTERN("Complex concept promotion input pattern (IkeFoundation)", "3891e739-23c3-51e3-b048-580330566298"),

    /**
     * The result of an action that writes a new version of its subject, referencing the run's input: the outcome, the STAMP of the version written (the outcome concept itself when nothing was written), and a message.
     */
    VERSION_CHANGE_RESULT_PATTERN("Version change result pattern (IkeFoundation)", "c0f13898-fa2a-5b10-aa22-cf0885144af0");

    private final EntityProxy.Pattern proxy;

    KometPattern(String fullyQualifiedName, String uuids) {
        this.proxy = EntityProxy.Pattern.make(fullyQualifiedName, uuids(uuids));
    }

    @Override
    public EntityProxy.Pattern patternForEnum() {
        return proxy;
    }

    @Override
    public PublicId publicId() {
        return proxy.publicId();
    }

    /**
     * @return the nid, resolved once and cached, as the proxy does
     */
    @Override
    public long nid() {
        return proxy.nid();
    }

    /**
     * @return the fully qualified name at birth, with no store lookup
     */
    @Override
    public String description() {
        return proxy.description();
    }

    /**
     * @return the proxy's text, so logs and debugging read as they did before enums
     */
    @Override
    public String toString() {
        return proxy.toString();
    }

    private static UUID[] uuids(String text) {
        String[] parts = text.split(",");
        UUID[] uuids = new UUID[parts.length];
        for (int i = 0; i < parts.length; i++) {
            uuids[i] = UUID.fromString(parts[i]);
        }
        return uuids;
    }
}
