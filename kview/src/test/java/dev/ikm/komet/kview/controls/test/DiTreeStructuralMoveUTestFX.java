package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLDiTreeControl;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.entity.graph.adaptor.axiom.LogicalExpressionBuilder;
import dev.ikm.tinkar.terms.KernelConcept;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
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
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structural move and undo of {@link KLDiTreeControl}: a row's move handle re-parents its axiom
 * into a set or role group EL++ admits it to, keeping its vertex identity; illegal
 * moves are refused; an emptied role group goes with the move; and every applied edit can be
 * undone and redone, until a change from outside the control clears the history.
 *
 * <p>The moves run through the control's move entry point on the FX thread rather than through
 * a synthesized platform drag, which TestFX cannot drive reliably; the drag handling only decides
 * the target and calls that same entry point. The definition under test is
 * {@code Necessary(And(is-a A, ∃R1→B, RoleGroup(And(∃R2→C)))) , Sufficient(And(is-a C))}.</p>
 */
@ExtendWith(ApplicationExtension.class)
class DiTreeStructuralMoveUTestFX {

    private static final UUID NECESSARY = UUID.fromString("6f0b6f1e-0000-4000-8000-000000000001");
    private static final UUID IS_A = UUID.fromString("6f0b6f1e-0000-4000-8000-000000000002");
    private static final UUID ROLE_1 = UUID.fromString("6f0b6f1e-0000-4000-8000-000000000003");
    private static final UUID GROUP = UUID.fromString("6f0b6f1e-0000-4000-8000-000000000004");
    private static final UUID ROLE_2 = UUID.fromString("6f0b6f1e-0000-4000-8000-000000000005");
    private static final UUID SUFFICIENT = UUID.fromString("6f0b6f1e-0000-4000-8000-000000000006");

    private static long A;
    private static long B;
    private static long C;

    private final FxRobot robot = new FxRobot();
    private KLDiTreeControl control;

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("ditree-move-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        A = KernelConcept.ANONYMOUS_CONCEPT.nid();
        B = KernelConcept.ACTIVE_STATE.nid();
        C = KernelConcept.INACTIVE_STATE.nid();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            Map<Long, String> names = Map.of(A, "Concept A", B, "Concept B", C, "Concept C",
                    KernelConcept.ROLE_GROUP.nid(), "Role group");
            control = new KLDiTreeControl();
            control.setTitle("Definition");
            control.setComponentItemResolver(nid -> new ComponentItem(names.getOrDefault(nid, "Concept"),
                    Identicon.generateIdenticonImage(KernelConcept.ANONYMOUS_CONCEPT.publicId()), null, true));
            control.setDescriptionResolver(nid -> names.getOrDefault(nid, "Concept"));
            control.setComponentSlotFactory(KLComponentControl::new);
            control.setValue(definition());
            stage.setScene(new Scene(new StackPane(control), 600, 500));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void cleanup() throws Exception {
        FxToolkit.cleanupStages();
    }

    private static DiTreeEntity definition() {
        LogicalExpressionBuilder builder = new LogicalExpressionBuilder();
        builder.NecessarySet(NECESSARY, builder.And(
                builder.ConceptAxiom(IS_A, A),
                builder.SomeRole(ROLE_1, KernelConcept.ACTIVE_STATE, builder.ConceptAxiom(B)),
                builder.SomeRole(GROUP, KernelConcept.ROLE_GROUP, builder.And(
                        builder.SomeRole(ROLE_2, KernelConcept.INACTIVE_STATE, builder.ConceptAxiom(C))))));
        builder.SufficientSet(SUFFICIENT, builder.And(builder.ConceptAxiom(C)));
        return switch (builder.build().sourceGraph()) {
            case DiTreeEntity tree -> tree;
            case DiTreeEntity.Builder treeBuilder -> treeBuilder.build();
            default -> throw new IllegalStateException("Unexpected source graph type");
        };
    }

    private static EntityVertex vertex(DiTreeEntity tree, UUID uuid) {
        for (EntityVertex vertex : tree.vertexMap()) {
            if (vertex != null && vertex.asUuid().equals(uuid)) {
                return vertex;
            }
        }
        return null;
    }

    /** The clause (set or role group) holding a vertex: the vertex above its AND. */
    private static EntityVertex clauseOf(DiTreeEntity tree, UUID uuid) {
        EntityVertex and = tree.predecessor(vertex(tree, uuid)).orElseThrow();
        return tree.predecessor(and).orElseThrow();
    }

    private static <T> T onFx(Callable<T> work) throws Exception {
        return WaitForAsyncUtils.asyncFx(work).get();
    }

    private void move(UUID child, UUID clause) throws Exception {
        onFx(() -> {
            DiTreeEntity tree = control.getValue();
            control.moveVertex(vertex(tree, child), vertex(tree, clause));
            return null;
        });
    }

    private boolean canMove(UUID child, UUID clause) throws Exception {
        return onFx(() -> {
            DiTreeEntity tree = control.getValue();
            return control.canMove(vertex(tree, child), vertex(tree, clause));
        });
    }

    @Test
    @DisplayName("Every leaf row and role group carries a move handle")
    void rowsCarryMoveHandles() {
        // is-a A, ∃R1, role group, ∃R2 (necessary set) and is-a C (sufficient set).
        assertEquals(5, robot.lookup(".ditree-move-handle").queryAll().size());
    }

    @Test
    @DisplayName("A role moves into a role group and keeps its vertex identity")
    void roleMovesIntoRoleGroup() throws Exception {
        assertTrue(canMove(ROLE_1, GROUP));
        move(ROLE_1, GROUP);

        DiTreeEntity tree = control.getValue();
        assertNotNull(vertex(tree, ROLE_1), "the moved role keeps its UUID");
        assertEquals(GROUP, clauseOf(tree, ROLE_1).asUuid());
        assertEquals(GROUP, clauseOf(tree, ROLE_2).asUuid());
        assertEquals(NECESSARY, clauseOf(tree, IS_A).asUuid());
    }

    @Test
    @DisplayName("Admission refuses illegal moves")
    void illegalMovesAreRefused() throws Exception {
        assertFalse(canMove(IS_A, GROUP), "an is-a never goes into a role group");
        assertFalse(canMove(GROUP, GROUP), "nothing moves into itself");
        assertFalse(canMove(ROLE_2, GROUP), "a move to where the axiom already is, is no move");
        assertFalse(canMove(ROLE_1, NECESSARY), "the role's own set");
        assertFalse(canMove(NECESSARY, SUFFICIENT), "a set is not an axiom of another set");
        assertTrue(canMove(IS_A, SUFFICIENT));
        assertTrue(canMove(GROUP, SUFFICIENT));

        DiTreeEntity before = control.getValue();
        move(IS_A, GROUP);
        assertEquals(before, control.getValue(), "a refused move leaves the tree as it was");
    }

    @Test
    @DisplayName("Moving a role group's last role out removes the emptied group")
    void emptiedRoleGroupGoes() throws Exception {
        move(ROLE_2, NECESSARY);
        DiTreeEntity tree = control.getValue();
        assertNull(vertex(tree, GROUP), "the emptied role group is removed");
        assertEquals(NECESSARY, clauseOf(tree, ROLE_2).asUuid());
    }

    @Test
    @DisplayName("Undo and redo step through applied edits")
    void undoAndRedo() throws Exception {
        DiTreeEntity original = control.getValue();
        move(ROLE_1, GROUP);
        DiTreeEntity moved = control.getValue();
        move(IS_A, SUFFICIENT);

        assertTrue(onFx(control::undo));
        assertEquals(moved, control.getValue());
        assertTrue(onFx(control::undo));
        assertEquals(original, control.getValue());
        assertFalse(onFx(control::undo), "nothing left to undo");

        assertTrue(onFx(control::redo));
        assertEquals(moved, control.getValue());

        // A new edit after an undo drops what could have been redone.
        move(IS_A, SUFFICIENT);
        assertFalse(onFx(control::canRedo));
    }

    @Test
    @DisplayName("A change of the value from outside clears the history")
    void outsideChangeClearsHistory() throws Exception {
        move(ROLE_1, GROUP);
        assertTrue(onFx(control::canUndo));
        onFx(() -> {
            control.setValue(definition());
            return null;
        });
        assertFalse(onFx(control::canUndo));
    }

    @Test
    @DisplayName("Shortcut+Z undoes and Shift+Shortcut+Z redoes")
    void undoKeys() throws Exception {
        DiTreeEntity original = control.getValue();
        move(ROLE_1, GROUP);
        DiTreeEntity moved = control.getValue();

        robot.clickOn(".ditree-clause-label");
        robot.push(KeyCode.SHORTCUT, KeyCode.Z);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(original, control.getValue());

        robot.push(KeyCode.SHORTCUT, KeyCode.SHIFT, KeyCode.Z);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(moved, control.getValue());
    }
}
