package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.id.Nid;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The concept navigator marks the row of its active component (the concept in the journal's focused
 * window) and draws the trail of ancestors that leads to it.
 */
@ExtendWith(ApplicationExtension.class)
class ConceptNavigatorActiveComponentUTestFX {

    private static final PseudoClass ACTIVE_COMPONENT = PseudoClass.getPseudoClass("active-component");
    private static final PseudoClass ANCESTOR_OF_ACTIVE = PseudoClass.getPseudoClass("cn-border-active");
    private static final PseudoClass SELECTION_SHOWN = PseudoClass.getPseudoClass("selection-shown");
    private static final PseudoClass ANCESTOR_OF_SELECTED = PseudoClass.getPseudoClass("cn-border-selected");
    private static final Map<Long, String> NAMES = new HashMap<>();

    private final FxRobot robot = new FxRobot();
    private KLConceptNavigatorControl control;
    private ConceptFacade hematein;
    private ConceptNavigatorTreeItem devicesItem;
    /** Stands in for a journal window: something outside the navigator that can take focus. */
    private Button focusTaker;

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("concept-navigator-active-test").toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
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
            ConceptFacade phenomenon = concept("Phenomenon");
            hematein = concept("Acid, Hematein");

            control = new KLConceptNavigatorControl();
            control.setHeader("Concept Header");
            control.setNavigator(navigator);
            ConceptNavigatorTreeItem rootItem = new ConceptNavigatorTreeItem(navigator, phenomenon, -1);
            devicesItem = item(navigator, "Medical Devices");
            ConceptNavigatorTreeItem hematologyItem = item(navigator, "Hematology");
            hematologyItem.getChildren().add(item(navigator, "Acid Hematin"));
            hematologyItem.getChildren().add(item(navigator, "Acid, Fuchsin"));
            hematologyItem.getChildren().add(new ConceptNavigatorTreeItem(navigator, hematein, -1));
            hematologyItem.getChildren().add(item(navigator, "Acid, Periodic"));
            devicesItem.getChildren().add(item(navigator, "Dental"));
            devicesItem.getChildren().add(item(navigator, "General Hospital"));
            devicesItem.getChildren().add(hematologyItem);
            rootItem.getChildren().add(devicesItem);
            rootItem.setExpanded(true);
            devicesItem.setExpanded(true);
            hematologyItem.setExpanded(true);
            control.setShowRoot(true);
            control.setRoot(rootItem);

            focusTaker = new Button("Window");
            VBox column = new VBox(focusTaker, control);
            column.setStyle("-fx-background-color: #E7EBF1;");
            Scene scene = new Scene(column, 420, 330);
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

    @Test
    void theActiveComponentsRowAndItsAncestorsAreMarkedUntilItIsCleared() {
        robot.interact(() -> control.setActiveComponent(hematein));
        WaitForAsyncUtils.waitForFxEvents();
        // The identicons are filled off the JavaFX thread; give them a moment so the picture shows them.
        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(control).getImage(),
                Path.of("target", "concept-navigator-active-component.png"));

        assertEquals(Set.of("Acid, Hematein"), namesOfTiles(ACTIVE_COMPONENT, false));
        assertEquals(Set.of("Phenomenon", "Medical Devices", "Hematology"), namesOfTiles(ANCESTOR_OF_ACTIVE, true));

        robot.interact(() -> control.setActiveComponent(null));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(Set.of(), namesOfTiles(ACTIVE_COMPONENT, false));
        assertEquals(Set.of(), namesOfTiles(ANCESTOR_OF_ACTIVE, true));
    }

    @Test
    void theSelectionAndItsTrailShowOnlyWhileTheNavigatorHasFocus() {
        robot.interact(focusTaker::requestFocus);
        robot.interact(() -> control.getSelectionModel().select(3)); // General Hospital, under Medical Devices
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(Set.of(), namesOfTiles(SELECTION_SHOWN, true), "selected, but the navigator has no focus");
        assertEquals(Set.of(), namesOfTiles(ANCESTOR_OF_SELECTED, true));

        robot.interact(control::requestFocus);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(Set.of("General Hospital"), namesOfTiles(SELECTION_SHOWN, true));
        assertEquals(Set.of("Phenomenon", "Medical Devices"), namesOfTiles(ANCESTOR_OF_SELECTED, true));

        robot.interact(focusTaker::requestFocus);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(Set.of(), namesOfTiles(SELECTION_SHOWN, true), "focus left the navigator");
        assertEquals(Set.of(), namesOfTiles(ANCESTOR_OF_SELECTED, true));
    }

    @Test
    void theIdenticonsAreLeftOutOfTheRowsWhileTheControlHidesThem() {
        assertEquals(Set.of(true), identiconsShown());

        robot.interact(() -> control.setShowIdenticons(false));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(Set.of(false), identiconsShown());

        robot.interact(() -> control.setShowIdenticons(true));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(Set.of(true), identiconsShown());
    }

    @Test
    void whileFollowingTheTreeScrollsToTheActiveComponentsRowAndLeavesTheRestAlone() {
        // Enough rows above "Acid, Hematein" to push it off the screen.
        robot.interact(() -> {
            for (int i = 0; i < 40; i++) {
                devicesItem.getChildren().add(i, item(control.getNavigator(), "Filler " + i));
            }
            control.getSelectionModel().select(2); // Filler 0, under Medical Devices
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(false, namesOfTiles().contains("Acid, Hematein"), "off screen to begin with");

        robot.interact(() -> control.setActiveComponent(hematein));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(false, namesOfTiles().contains("Acid, Hematein"), "not following: the tree stays where it is");

        robot.interact(() -> control.setFollowActiveComponent(true));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(true, namesOfTiles().contains("Acid, Hematein"), "following: the row is scrolled to");
        assertEquals("Filler 0", NAMES.get(control.getSelectionModel().getSelectedItem().getValue().nid()),
                "the selection is left alone");
        assertEquals(true, devicesItem.isExpanded(), "nothing is collapsed");
    }

    /** The names on the tiles of the rows the tree is showing. */
    private Set<String> namesOfTiles() {
        Set<String> names = new TreeSet<>();
        for (Node tile : robot.lookup(".concept-tile").queryAll()) {
            String name = ((Label) tile.lookup(".concept-label")).getText();
            if (name != null && tile.getParent() != null && tile.getParent().isVisible()) {
                names.add(name);
            }
        }
        return names;
    }

    /** Whether the identicons of the rows are shown and take room: one value when they all agree. */
    private Set<Boolean> identiconsShown() {
        Set<Boolean> shown = new TreeSet<>();
        for (Node identicon : robot.lookup(".concept-tile > .identicon").queryAll()) {
            shown.add(identicon.isVisible() && identicon.isManaged());
        }
        return shown;
    }

    /** The names on the tiles in the given state: the tile's own, or that of the cell it is in. */
    private Set<String> namesOfTiles(PseudoClass state, boolean onCell) {
        Set<String> names = new TreeSet<>();
        for (Node tile : robot.lookup(".concept-tile").queryAll()) {
            String name = ((Label) tile.lookup(".concept-label")).getText();
            Node holder = onCell ? tile.getParent() : tile;
            if (name != null && holder != null && holder.getPseudoClassStates().contains(state)) {
                names.add(name);
            }
        }
        return names;
    }

    private static ConceptNavigatorTreeItem item(Navigator navigator, String name) {
        return new ConceptNavigatorTreeItem(navigator, concept(name), -1);
    }

    private static ConceptFacade concept(String name) {
        ConceptFacade concept = EntityProxy.Concept.make(name, UUID.randomUUID());
        NAMES.put(concept.nid(), name);
        return concept;
    }

    /** A navigator that only names the concepts: the test builds the tree itself. */
    private static Navigator navigator() {
        ViewCalculator viewCalculator = (ViewCalculator) Proxy.newProxyInstance(
                ConceptNavigatorActiveComponentUTestFX.class.getClassLoader(), new Class<?>[]{ViewCalculator.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getDescriptionTextOrNid" -> NAMES.get(Nid.nidOf(args[0]));
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "ViewCalculator for the active component test";
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
                return false;
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
}
