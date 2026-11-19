package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.kview.controls.ComponentSearchTextField;
import dev.ikm.komet.kview.controls.ConceptFilterBar;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.kview.controls.KLSearchControl;
import dev.ikm.komet.kview.mvvm.view.navigation.ConceptPatternNavController;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The top of the navigator panel, put together from the same parts and style classes as
 * navigation-concept-pattern.fxml and the concept side put it together: the Concepts | Patterns
 * switch, the concept search field and the "Show" row, on the panel's light column.
 */
@ExtendWith(ApplicationExtension.class)
class ConceptNavigatorTopUTestFX {

    private static final File STARTER_DATA = new File("target/data", "ike-starter-set-reasoned-pb.zip");

    private final FxRobot robot = new FxRobot();
    private VBox column;
    private ConceptFilterBar bar;
    private VBox conceptSide;
    private VBox patternSide;

    @BeforeAll
    static void startStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("concept-navigator-top-test").toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        // The search control's view coordinate resolves its language and stamp concepts, so the store needs the starter set.
        assertTrue(STARTER_DATA.exists(), "Starter data must be at " + STARTER_DATA.getAbsolutePath());
        assertTrue(new LoadEntitiesFromProtobufFile(STARTER_DATA).compute().getTotalCount() > 0);
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            ToggleGroup group = new ToggleGroup();
            ToggleButton concepts = new ToggleButton("Concepts");
            concepts.getStyleClass().addAll("nav-toggle", "nav-toggle-left");
            concepts.setToggleGroup(group);
            concepts.setSelected(true);
            ToggleButton patterns = new ToggleButton("Patterns");
            patterns.getStyleClass().addAll("nav-toggle", "nav-toggle-right");
            patterns.setToggleGroup(group);
            HBox navSwitch = new HBox(concepts, patterns);
            navSwitch.getStyleClass().add("nav-switch");
            navSwitch.setMaxWidth(HBox.USE_PREF_SIZE);
            HBox header = new HBox(navSwitch);
            header.setAlignment(Pos.CENTER);
            header.getStyleClass().add("concept-pattern-header");

            bar = new ConceptFilterBar();
            KLSearchControl search = new KLSearchControl();
            search.setViewProperties(new ObservableViewNoOverride(Coordinates.View.DefaultView(), "navigator view")
                    .makeOverridableViewProperties("navigator"));
            VBox conceptSide = new VBox(search, bar);
            conceptSide.getStyleClass().add("concept-navigator-container");

            this.conceptSide = conceptSide;
            column = new VBox(header, conceptSide);
            column.getStyleClass().addAll("concept-pattern-container", "grey-vertical-scroll-area");
            column.setMinWidth(448);
            column.setMaxWidth(448);
            // The PATTERNS side next to it, as the controller makes it, to compare the two search fields.
            patternSide = ConceptPatternNavController.createPatternsPanel(new ComponentSearchTextField(), new ScrollPane());
            patternSide.setMinWidth(448);
            patternSide.setMaxWidth(448);
            Scene scene = new Scene(new HBox(column, patternSide), 896, 200);
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
    void theSearchFieldSpansTheColumnWithTheViewOptionsGlyphInsideIt() {
        TextField field = robot.lookup(".search-control > .text-field").query();
        Node glyph = robot.lookup(".search-control > .filter-region").query();
        Bounds fieldBounds = field.localToScene(field.getBoundsInLocal());
        Bounds glyphBounds = glyph.localToScene(glyph.getBoundsInLocal());
        assertTrue(fieldBounds.contains(glyphBounds), "the glyph is inside the field, at its right");
        assertEquals(14, fieldBounds.getMinX(), 0.5, "the inset the pattern navigator's search has");
        assertEquals(448 - 14, fieldBounds.getMaxX(), 0.5);
        capture("concept-navigator-top-idle");

        robot.interact(() -> {
            bar.setFilterText("Identifier source: SCTID");
            bar.setCountText("368 597\nconcepts");
        });
        capture("concept-navigator-top-filtered");
    }

    @Test
    void theSearchFieldIsTheSameOnBothSides() {
        assertEquals(within(patternSide, ".auto-complete-text-field"), within(conceptSide, ".search-control > .text-field"),
                "the field: where it is and how large");
        assertEquals(within(patternSide, ".filter-icon"), within(conceptSide, ".filter-region > .icon.filter"),
                "the glyph at the right of the field");
        assertEquals(within(patternSide, ".search-icon"), within(conceptSide, ".region > .icon.search"),
                "the glyph at the left of the field");
    }

    /** Where a node of a side is, from the side's top left corner, to the half pixel. */
    private String within(Node side, String selector) {
        WaitForAsyncUtils.waitForFxEvents();
        Node node = robot.from(side).lookup(selector).query();
        Bounds bounds = side.sceneToLocal(node.localToScene(node.getBoundsInLocal()));
        return "x %.1f, y %.1f, %.1f wide, %.1f tall".formatted(
                bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }

    private void capture(String name) {
        WaitForAsyncUtils.waitForFxEvents();
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(column).getImage(),
                Path.of("target", name + ".png"));
    }
}
