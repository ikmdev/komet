package dev.ikm.komet.layout.expand;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Covers the parts of {@link KlExpansionHost.Rung} that need no JavaFX toolkit: the display name is
 * resolved lazily, late, and defensively. The ladder walk itself needs live {@code Pane}s and is
 * exercised by the app-level verification.
 */
class KlExpansionHostRungTest {

    @Test
    void nameIsResolvedLazilyOnEachCall() {
        AtomicInteger calls = new AtomicInteger();
        KlExpansionHost.Rung rung = new KlExpansionHost.Rung(null, () -> "call " + calls.incrementAndGet(), false);

        assertEquals(0, calls.get(), "the supplier is not called when the rung is built");
        assertEquals("call 1", rung.name());
        assertEquals("call 2", rung.name(), "the name is re-read, so a card renamed after stamping is current");
    }

    @Test
    void nameToleratesAnAbsentOrNullSupplier() {
        assertEquals("", new KlExpansionHost.Rung(null, null, false).name());
        assertEquals("", new KlExpansionHost.Rung(null, () -> null, false).name());
    }

    @Test
    void onlyAWindowDeclaresFullScreenBeyondIt() {
        assertEquals(true, new KlExpansionHost.Rung(null, () -> "Journal window", true).osFullScreenBeyond());
        assertEquals(false, new KlExpansionHost.Rung(null, () -> "Document", false).osFullScreenBeyond());
    }

    @Test
    void theSupplierIsCarriedVerbatim() {
        java.util.function.Supplier<String> name = () -> "Card";
        assertSame(name, new KlExpansionHost.Rung(null, name, false).displayName());
    }
}
