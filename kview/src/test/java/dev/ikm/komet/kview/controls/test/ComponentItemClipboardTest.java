package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.framework.dnd.KometClipboard;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.ComponentItemActions;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.KernelConcept;
import dev.ikm.tinkar.terms.KernelPattern;
import dev.ikm.tinkar.terms.ProxyFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.OptionalLong;
import java.util.UUID;

import static dev.ikm.komet.framework.dnd.KometClipboard.COMPONENT_DRAG_FORMAT;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_CONCEPT_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_PATTERN_PROXY;
import static dev.ikm.komet.framework.dnd.KometClipboard.KOMET_SEMANTIC_PROXY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a component row puts on a drag, against the starter set in a memory-only store. A row drags
 * as a koncept does, so the navigator's button and the classic navigator, which read the proxies,
 * take it as they take a badge; the format and the text it always carried stay.
 */
class ComponentItemClipboardTest {

    private static final File PB_STARTER_DATA = new File("target/data/ike-starter-set-reasoned-pb.zip");

    @BeforeAll
    static void startStore() {
        assertTrue(PB_STARTER_DATA.exists(), "Starter data must be present at " + PB_STARTER_DATA.getAbsolutePath());
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        assertTrue(new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute().getTotalCount() > 0);
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    void aConceptRowCarriesTheConceptAsWellAsItsComponentFormat() {
        long concept = KernelConcept.ENGLISH_LANGUAGE.nid();
        ClipboardContent content = contentOf(concept);

        assertEquals(OptionalLong.of(concept), nidOf(content, KOMET_CONCEPT_PROXY));
        assertEquals(KometClipboard.encodePublicId(PrimitiveData.publicId(concept)), content.get(COMPONENT_DRAG_FORMAT));
        assertEquals("the row's title", content.getString());
    }

    @Test
    void aSemanticRowCarriesItselfAndTheConceptItRefersTo() {
        long concept = KernelConcept.ENGLISH_LANGUAGE.nid();
        long semantic = EntityService.get()
                .semanticsForComponentOfPattern(concept, KernelPattern.DESCRIPTION_PATTERN.nid())
                .toList().getFirst().nid();
        ClipboardContent content = contentOf(semantic);

        assertEquals(OptionalLong.of(semantic), nidOf(content, KOMET_SEMANTIC_PROXY));
        assertEquals(OptionalLong.of(concept), nidOf(content, KOMET_CONCEPT_PROXY));
    }

    @Test
    void aPatternRowCarriesNoConcept() {
        long pattern = KernelPattern.DESCRIPTION_PATTERN.nid();
        ClipboardContent content = contentOf(pattern);

        assertEquals(OptionalLong.of(pattern), nidOf(content, KOMET_PATTERN_PROXY));
        assertFalse(content.containsKey(KOMET_CONCEPT_PROXY));
    }

    @Test
    void aRowForAComponentTheStoreDoesNotHoldKeepsItsFormatAndTakesNoNid() {
        UUID unknown = UUID.randomUUID();
        PublicId publicId = PublicIds.of(unknown);
        ClipboardContent content = ComponentItemActions.buildClipboardContent(
                new ComponentItem("the row's title", null, publicId, true));

        assertEquals(KometClipboard.encodePublicId(publicId), content.get(COMPONENT_DRAG_FORMAT));
        assertFalse(content.containsKey(KOMET_CONCEPT_PROXY));
        assertFalse(PrimitiveData.get().hasUuid(unknown), "a drag minted a nid");
    }

    private static ClipboardContent contentOf(long nid) {
        return ComponentItemActions.buildClipboardContent(
                new ComponentItem("the row's title", null, PrimitiveData.publicId(nid), true));
    }

    private static OptionalLong nidOf(ClipboardContent content, DataFormat format) {
        return content.containsKey(format)
                ? OptionalLong.of(ProxyFactory.<EntityProxy>fromXmlFragment((String) content.get(format)).nid())
                : OptionalLong.empty();
    }
}
