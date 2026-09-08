package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.KLReadOnlyDiTreeControl;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeType;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hover definition peek of {@link KLReadOnlyDiTreeControl}: resting the pointer on a chip
 * opens the chip's concept definition in a popover, the chips in that popover open the next one,
 * and the chain closes on leaving, on Escape, or on the pinned popover's close button.
 *
 * <p>The tree is three concepts deep — the shown definition names A, A's definition names B,
 * B's names C, and C has none — resolved through the control's definition resolver alone; the
 * ephemeral store is only there so the term nids in the trees resolve.</p>
 */
@ExtendWith(ApplicationExtension.class)
class DiTreeDefinitionPeekUTestFX {

    private static final String POPOVER = ".ditree-peek-popover";
    private static final String PIN = ".ditree-peek-pin-button";
    private static final String CLOSE = ".ditree-peek-close-button";

    // Resolved once the store runs: a term nid asks the store.
    private static int A;
    private static int B;
    private static int C;

    private final FxRobot robot = new FxRobot();
    private StackPane away;

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("ditree-peek-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        A = TinkarTerm.ANONYMOUS_CONCEPT.nid();
        B = TinkarTerm.ACTIVE_STATE.nid();
        C = TinkarTerm.INACTIVE_STATE.nid();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            Map<Integer, DiTreeEntity> definitions = Map.of(A, definitionNaming(B), B, definitionNaming(C));
            Map<Integer, String> names = Map.of(A, "Concept A", B, "Concept B", C, "Concept C");

            KLReadOnlyDiTreeControl control = new KLReadOnlyDiTreeControl();
            control.setTitle("Definition");
            // No public id: the chip's glyph resolution stays off the (empty) store.
            control.setComponentItemResolver(nid -> new ComponentItem(names.get(nid),
                    Identicon.generateIdenticonImage(TinkarTerm.ANONYMOUS_CONCEPT.publicId()), null, true));
            control.setDescriptionResolver(names::get);
            control.setDefinitionResolver(definitions::get);
            control.setValue(definitionNaming(A));

            // A landing spot for "the pointer left": top-right, clear of where the popovers
            // open (below the chips, from their left edge).
            away = new StackPane();
            away.setMinSize(150, 100);
            away.setMaxSize(150, 100);
            control.setPrefWidth(400);
            HBox root = new HBox(control, away);
            root.setAlignment(Pos.TOP_LEFT);
            stage.setScene(new Scene(root, 700, 600));
            stage.show();
        });
    }

    @AfterEach
    void cleanup() throws Exception {
        FxToolkit.cleanupStages();
    }

    /** {@code NecessarySet(And(ConceptAxiom(nid)))}, the seed shape the concept window writes. */
    private static DiTreeEntity definitionNaming(int conceptNid) {
        LogicalExpressionBuilder builder = new LogicalExpressionBuilder();
        builder.NecessarySet(builder.And(builder.ConceptAxiom(conceptNid)));
        return switch (builder.build().sourceGraph()) {
            case DiTreeEntity tree -> tree;
            case DiTreeEntity.Builder treeBuilder -> treeBuilder.build();
            default -> throw new IllegalStateException("Unexpected source graph type");
        };
    }

    @Test
    @DisplayName("Resting on a chip opens its definition; leaving closes it")
    void hoverOpensAndLeavingCloses() throws Exception {
        robot.moveTo(chip("Concept A"));
        awaitPopovers(1);
        assertFalse(robot.from(popovers().iterator().next()).lookup(".ditree-readonly-indicator").query().isVisible(),
                "the popover frames the definition; the READ-ONLY marker stays out of it");
        Path frame = robot.lookup(".popover .border").queryAs(Path.class);
        assertEquals(Color.web("#b8c1d0"), frame.getStroke(), "the frame carries the peek outline");
        assertEquals(StrokeType.INSIDE, frame.getStrokeType());
        // The skin's content pane must not paint over that inside stroke (komet.css paints
        // every ".content" white; the peek's stylesheet takes it back to transparent).
        Region content = robot.lookup(".popover > .content").queryAs(Region.class);
        assertTrue(content.getBackground().getFills().stream()
                        .allMatch(fill -> Color.TRANSPARENT.equals(fill.getFill())),
                "the popover content pane stays transparent over the frame");

        robot.moveTo(away);
        awaitPopovers(0);
    }

    @Test
    @DisplayName("A chip inside the popover opens the next definition, and the chain stays while the pointer is in it")
    void chipsInsideThePopoverPeekToo() throws Exception {
        robot.moveTo(chip("Concept A"));
        awaitPopovers(1);

        robot.moveTo(chip("Concept B"));
        awaitPopovers(2);

        // C has no definition: nothing more opens, and the two open ones stay.
        robot.moveTo(chip("Concept C"));
        WaitForAsyncUtils.sleep(1200, TimeUnit.MILLISECONDS);
        assertEquals(2, popovers().size());

        robot.moveTo(away);
        awaitPopovers(0);
    }

    @Test
    @DisplayName("Escape closes the deepest popover of the chain")
    void escapeClosesTheDeepestPopover() throws Exception {
        robot.moveTo(chip("Concept A"));
        awaitPopovers(1);
        robot.moveTo(chip("Concept B"));
        awaitPopovers(2);

        robot.press(KeyCode.ESCAPE).release(KeyCode.ESCAPE);
        awaitPopovers(1);
        assertTrue(chip("Concept B").getScene().getWindow().isShowing(),
                "the outer popover, holding chip B, is the one left");
    }

    @Test
    @DisplayName("A pinned popover stays after the pointer leaves, until its close button")
    void pinnedPopoverStaysUntilClosed() throws Exception {
        robot.moveTo(chip("Concept A"));
        awaitPopovers(1);

        robot.clickOn(PIN);
        assertTrue(robot.lookup(PIN).queryAs(ToggleButton.class).isSelected());
        robot.moveTo(away);
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertEquals(1, popovers().size());

        robot.clickOn(CLOSE);
        awaitPopovers(0);
    }

    private Node chip(String name) {
        return robot.lookup(node -> node instanceof ComponentItemNode item
                && name.equals(item.getComponentItem().getText())).query();
    }

    private Set<Node> popovers() {
        return robot.lookup(POPOVER).queryAll();
    }

    private void awaitPopovers(int count) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> popovers().size() == count);
        WaitForAsyncUtils.waitForFxEvents();
    }
}
