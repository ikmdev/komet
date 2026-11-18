package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.ConceptFilterResult;
import dev.ikm.komet.kview.controls.FilteredNavigator;
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
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.LongSets;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Under a Show filter the concept navigator draws the concepts that match as usual and the
 * ancestors it keeps only to lead to them as context rows (the {@code filter-context} state of
 * their tile).
 */
@ExtendWith(ApplicationExtension.class)
class ConceptFilterContextRowsUTestFX {

    private static final PseudoClass FILTER_CONTEXT = PseudoClass.getPseudoClass("filter-context");
    private static final PseudoClass ANCESTOR_OF_SELECTED = PseudoClass.getPseudoClass("cn-border-selected");
    private static final Map<Long, String> NAMES = new HashMap<>();
    private static final Map<Long, Long> PARENTS = new HashMap<>();

    private final FxRobot robot = new FxRobot();
    private KLConceptNavigatorControl control;

    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("concept-filter-context-test").toFile());
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
            ConceptFacade root = concept("Integrated Knowledge Management (SOLOR)", null);
            ConceptFacade observable = concept("Observable entity", root);
            ConceptFacade diameter = concept("Diameter.hilum-cortex.mean", observable);
            ConceptFacade fetus = concept("Diameter.hilum-cortex.mean | Fetus | OB ultrasound", diameter);
            ConceptFacade orbital = concept("Diameter.inner orbital", observable);

            ConceptFilterResult result = ConceptFilterResult.resolve(LongSets.immutable.of(diameter.nid(), fetus.nid(), orbital.nid()),
                    nid -> PARENTS.containsKey(nid) ? new long[]{PARENTS.get(nid)} : new long[0],
                    new long[]{root.nid()});
            Navigator filtered = new FilteredNavigator(navigator(), result);

            control = new KLConceptNavigatorControl();
            control.setHeader("Concept Header");
            control.setNavigator(filtered);
            ConceptNavigatorTreeItem rootItem = new ConceptNavigatorTreeItem(filtered, root, -1);
            ConceptNavigatorTreeItem observableItem = new ConceptNavigatorTreeItem(filtered, observable, -1);
            ConceptNavigatorTreeItem diameterItem = new ConceptNavigatorTreeItem(filtered, diameter, -1);
            diameterItem.getChildren().add(new ConceptNavigatorTreeItem(filtered, fetus, -1));
            observableItem.getChildren().add(diameterItem);
            observableItem.getChildren().add(new ConceptNavigatorTreeItem(filtered, orbital, -1));
            rootItem.getChildren().add(observableItem);
            rootItem.setExpanded(true);
            observableItem.setExpanded(true);
            diameterItem.setExpanded(true);
            control.setShowRoot(true);
            control.setRoot(rootItem);

            Scene scene = new Scene(new StackPane(control), 480, 260);
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
    void ancestorsKeptForContextAreMarkedAndMatchesAreNot() {
        Map<String, Boolean> contextByName = new HashMap<>();
        for (Node tile : robot.lookup(".concept-tile").queryAll()) {
            String name = ((Label) tile.lookup(".concept-label")).getText();
            if (name != null) {
                contextByName.put(name, tile.getPseudoClassStates().contains(FILTER_CONTEXT));
            }
        }
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(control).getImage(),
                Path.of("target", "concept-filter-context-rows.png"));

        assertEquals(Map.of(
                "Integrated Knowledge Management (SOLOR)", true,
                "Observable entity", true,
                "Diameter.hilum-cortex.mean", false,
                "Diameter.hilum-cortex.mean | Fetus | OB ultrasound", false,
                "Diameter.inner orbital", false), contextByName);
    }

    @Test
    void contextAncestorsOfTheSelectedConceptStayWithoutAPlate() {
        robot.interact(() -> control.getSelectionModel().select(3)); // the Fetus concept, under Diameter
        WaitForAsyncUtils.waitForFxEvents();
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(control).getImage(),
                Path.of("target", "concept-filter-context-rows-selected.png"));

        Map<String, Boolean> plateByName = new HashMap<>();
        for (Node tile : robot.lookup(".concept-tile").queryAll()) {
            String name = ((Label) tile.lookup(".concept-label")).getText();
            if (name != null && tile.getParent().getPseudoClassStates().contains(ANCESTOR_OF_SELECTED)) {
                plateByName.put(name, ((Region) tile).getBackground().getFills().stream()
                        .anyMatch(fill -> !Color.TRANSPARENT.equals(fill.getFill())));
            }
        }

        assertEquals(Map.of(
                "Integrated Knowledge Management (SOLOR)", false,
                "Observable entity", false,
                "Diameter.hilum-cortex.mean", true), plateByName);
    }

    private static ConceptFacade concept(String name, ConceptFacade parent) {
        ConceptFacade concept = EntityProxy.Concept.make(name, UUID.randomUUID());
        NAMES.put(concept.nid(), name);
        if (parent != null) {
            PARENTS.put(concept.nid(), parent.nid());
        }
        return concept;
    }

    /** A navigator that only names the concepts: the test builds the tree itself. */
    private static Navigator navigator() {
        ViewCalculator viewCalculator = (ViewCalculator) Proxy.newProxyInstance(
                ConceptFilterContextRowsUTestFX.class.getClassLoader(), new Class<?>[]{ViewCalculator.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getDescriptionTextOrNid" -> NAMES.get(Nid.nidOf(args[0]));
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "ViewCalculator for the context rows test";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        return new Navigator() {
            @Override
            public long[] getParentNids(long childNid) {
                return new long[0];
            }

            @Override
            public long[] getChildNids(long parentNid) {
                return PARENTS.entrySet().stream()
                        .filter(entry -> entry.getValue() == parentNid)
                        .mapToLong(Map.Entry::getKey)
                        .toArray();
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
                return getChildNids(conceptNid).length == 0;
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
