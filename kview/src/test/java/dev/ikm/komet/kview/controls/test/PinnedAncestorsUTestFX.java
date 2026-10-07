/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.controls.test;

import dev.ikm.tinkar.common.id.Nid;
import org.testfx.api.FxService;
import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
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

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The concept navigator pins the ancestors of the first visible concept on top of the tree
 * (ikmdev/komet-desktop#151): the nearest three, and all of them on demand.
 * <p>The tree is a chain of six nested concepts, "Level 0" to "Level 5", each one followed by a few
 * sibling leaves, with forty leaves under the deepest one. Thirty top level leaves come before the chain, and thirty more after it.
 */
@ExtendWith(ApplicationExtension.class)
class PinnedAncestorsUTestFX {

    private static final String PINNED = ".pinned-ancestors";
    private static final int LEVELS = 6;
    private static final int LEAVES = 40;
    private static final int FILLERS = 30;

    private static final Map<Long, String> NAMES = new HashMap<>();
    private static final Set<Long> LEAF_NIDS = new HashSet<>();

    private final FxRobot robot = new FxRobot();
    private KLConceptNavigatorControl control;
    private final ConceptNavigatorTreeItem[] levels = new ConceptNavigatorTreeItem[LEVELS];
    private final ConceptNavigatorTreeItem[] leaves = new ConceptNavigatorTreeItem[LEAVES];

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("pinned-ancestors-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            Navigator navigator = navigator();
            control = new KLConceptNavigatorControl();
            control.setHeader("Concept Header");
            control.setNavigator(navigator);

            ConceptNavigatorTreeItem root = item(navigator, "Root", false);
            for (int i = 0; i < FILLERS; i++) {
                root.getChildren().add(item(navigator, "Before " + i, true));
            }
            ConceptNavigatorTreeItem parent = root;
            for (int i = 0; i < LEVELS; i++) {
                levels[i] = item(navigator, "Level " + i, false);
                parent.getChildren().add(levels[i]);
                for (int j = 0; j < 3; j++) {
                    parent.getChildren().add(item(navigator, "Sibling " + i + "." + j, true));
                }
                parent = levels[i];
            }
            for (int i = 0; i < LEAVES; i++) {
                leaves[i] = item(navigator, "Leaf " + i, true);
                parent.getChildren().add(leaves[i]);
            }
            for (int i = 0; i < FILLERS; i++) {
                root.getChildren().add(item(navigator, "After " + i, true));
            }
            root.setExpanded(true);
            for (ConceptNavigatorTreeItem level : levels) {
                level.setExpanded(true);
            }
            control.setRoot(root);

            Scene scene = new Scene(new StackPane(control), 480, 520);
            // the palette of the application
            scene.getStylesheets().add(KLConceptNavigatorControl.class
                    .getResource("/dev/ikm/komet/kview/mvvm/view/kview.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void cleanup() throws Exception {
        FxToolkit.cleanupStages();
    }

    private static ConceptNavigatorTreeItem item(Navigator navigator, String name, boolean leaf) {
        ConceptFacade concept = EntityProxy.Concept.make(name, UUID.randomUUID());
        NAMES.put(concept.nid(), name);
        if (leaf) {
            LEAF_NIDS.add(concept.nid());
        }
        return new ConceptNavigatorTreeItem(navigator, concept, -1);
    }

    /** A navigator that only names the concepts and tells the leaves: the test builds the tree itself. */
    private static Navigator navigator() {
        ViewCalculator viewCalculator = (ViewCalculator) Proxy.newProxyInstance(
                PinnedAncestorsUTestFX.class.getClassLoader(), new Class<?>[]{ViewCalculator.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getDescriptionTextOrNid" -> NAMES.get(Nid.nidOf(args[0]));
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "ViewCalculator for the pinned ancestors test";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        return new Navigator() {
            @Override
            public long[] getParentNids(long childNid) {
                return new long[0];
            }

            @Override
            public long[] getChildNids(long parentNid) {
                return new long[0];
            }

            @Override
            public ImmutableList<Edge> getParentEdges(long childNid) {
                return Lists.immutable.empty();
            }

            @Override
            public ImmutableList<Edge> getChildEdges(long parentNid) {
                return Lists.immutable.empty();
            }

            @Override
            public boolean isLeaf(long conceptNid) {
                return LEAF_NIDS.contains(conceptNid);
            }

            @Override
            public boolean isChildOf(long childNid, long parentNid) {
                return false;
            }

            @Override
            public boolean isDescendentOf(long descendantNid, long ancestorNid) {
                return false;
            }

            @Override
            public long[] getRootNids() {
                return new long[0];
            }

            @Override
            public ViewCalculator getViewCalculator() {
                return viewCalculator;
            }
        };
    }

    private void scrollTo(ConceptNavigatorTreeItem item) {
        robot.interact(() -> control.scrollTo(control.getRow(item)));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Region pinned() {
        return robot.lookup(PINNED).queryAs(Region.class);
    }

    private List<String> pinnedNames() {
        return robot.lookup(PINNED + " .navigator-tree-cell .concept-label").queryAllAs(Label.class).stream()
                .sorted((a, b) -> Double.compare(sceneY(a), sceneY(b)))
                .map(Label::getText)
                .toList();
    }

    private Button toggle() {
        return robot.lookup(PINNED + " .toggle-row .button").queryAs(Button.class);
    }

    private String count() {
        return robot.lookup(PINNED + " .toggle-row .count-label").queryAs(Label.class).getText();
    }

    private static double sceneY(Node node) {
        return node.localToScene(0, 0).getY();
    }

    /** The cell of the tree, not a pinned one, that shows the item. */
    @SuppressWarnings("unchecked")
    private TreeCell<ConceptFacade> cellOf(ConceptNavigatorTreeItem item) {
        return robot.lookup(".virtual-flow .navigator-tree-cell").queryAllAs(TreeCell.class).stream()
                .filter(cell -> cell.getTreeItem() == item && cell.isVisible())
                .findFirst()
                .orElseThrow();
    }

    private double pinnedBottom() {
        Region pinned = pinned();
        return pinned.localToScene(0, pinned.getHeight()).getY();
    }

    private void capture(String name) {
        Path path = Path.of("target", "pinned-ancestors", name + ".png");
        robot.interact(() -> {
            path.getParent().toFile().mkdirs();
            FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(control).getImage(), path);
        });
    }

    @Test
    @DisplayName("At the top of the tree nothing is pinned")
    void nothingIsPinnedAtTheTop() {
        assertFalse(pinned().isVisible());
        capture("top");
    }

    @Test
    @DisplayName("Scrolling into a deep branch pins the nearest three ancestors, and offers the rest")
    void deepBranchPinsTheNearestThree() {
        scrollTo(leaves[20]);

        assertTrue(pinned().isVisible());
        assertEquals(List.of("Level 3", "Level 4", "Level 5"), pinnedNames());
        assertEquals("Show 3 more ancestors", toggle().getText());
        assertEquals("3 of 6 shown", count());
        capture("collapsed");
    }

    @Test
    @DisplayName("The concept that is scrolled to shows up right below its pinned ancestors")
    void scrollTargetIsNotCovered() {
        scrollTo(leaves[20]);

        double top = sceneY(cellOf(leaves[20]));
        assertTrue(top >= pinnedBottom() - 0.5, "the concept is not covered: its top is " + top +
                ", and the bottom of the pinned ancestors is " + pinnedBottom());
        assertTrue(top < pinnedBottom() + cellOf(leaves[20]).getHeight(), "the concept is the first one below");
    }

    @Test
    @DisplayName("The toggle pins all the ancestors, and back")
    void toggleExpandsAndCollapses() {
        scrollTo(leaves[20]);

        robot.clickOn(toggle());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(List.of("Level 0", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5"), pinnedNames());
        assertEquals("Show fewer ancestors", toggle().getText());
        assertEquals("6 of 6 shown", count());
        capture("expanded");

        robot.clickOn(toggle());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(List.of("Level 3", "Level 4", "Level 5"), pinnedNames());
        assertEquals("Show 3 more ancestors", toggle().getText());
    }

    @Test
    @DisplayName("The expanded state is kept while scrolling")
    void expandedStateIsKeptWhileScrolling() {
        scrollTo(leaves[20]);
        robot.clickOn(toggle());
        WaitForAsyncUtils.waitForFxEvents();

        scrollTo(leaves[30]);
        assertEquals(6, pinnedNames().size());
    }

    @Test
    @DisplayName("Three ancestors or fewer are pinned without a toggle")
    void shallowBranchHasNoToggle() {
        // the siblings of "Level 2" are children of "Level 1", and come after the whole branch of "Level 2"
        scrollTo((ConceptNavigatorTreeItem) levels[2].nextSibling());

        assertEquals(List.of("Level 0", "Level 1"), pinnedNames());
        assertTrue(robot.lookup(PINNED + " .toggle-row").queryAll().isEmpty());
    }

    @Test
    @DisplayName("Clicking a pinned ancestor scrolls the tree to it")
    void clickingAPinnedAncestorScrollsToIt() {
        scrollTo(leaves[20]);

        Node label = robot.lookup(PINNED + " .navigator-tree-cell .concept-label").queryAllAs(Label.class).stream()
                .filter(l -> "Level 4".equals(l.getText()))
                .findFirst()
                .orElseThrow();
        robot.clickOn(label);
        WaitForAsyncUtils.waitForFxEvents();

        // the ancestors of the ancestor are the rows right above it: the tree shows them, so nothing
        // has to be pinned
        assertTrue(cellOf(levels[4]).isVisible());
        assertTrue(cellOf(levels[1]).isVisible());
        assertFalse(pinned().isVisible());
        capture("after-click");
    }

    @Test
    @DisplayName("The mouse wheel scrolls the tree over the pinned ancestors too")
    void wheelScrollsOverThePinnedAncestors() {
        scrollTo(leaves[20]);
        double top = sceneY(cellOf(leaves[25]));

        robot.moveTo(pinned());
        robot.scroll(2, VerticalDirection.DOWN);
        WaitForAsyncUtils.waitForFxEvents();

        assertNotEquals(top, sceneY(cellOf(leaves[25])), 0.5, "the tree scrolled");
        assertEquals(List.of("Level 3", "Level 4", "Level 5"), pinnedNames());
    }

    @Test
    @DisplayName("A pinned ancestor is as wide as the concepts of the tree")
    void pinnedAncestorIsAsWideAsTheConcepts() {
        scrollTo(leaves[20]);

        Node pinnedCell = robot.lookup(PINNED + " .navigator-tree-cell").query();
        assertEquals(cellOf(leaves[20]).getWidth(), pinnedCell.getLayoutBounds().getWidth(), 0.5);
    }

    @Test
    @DisplayName("Back at the top, the pinned ancestors go away")
    void scrollingBackUnpins() {
        scrollTo(leaves[20]);
        assertTrue(pinned().isVisible());

        scrollTo(levels[0]);
        assertFalse(pinned().isVisible());
    }
}
