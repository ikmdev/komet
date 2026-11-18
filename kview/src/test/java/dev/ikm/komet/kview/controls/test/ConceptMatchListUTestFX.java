package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.kview.controls.ConceptMatchList;
import dev.ikm.komet.kview.controls.ConceptMatchList.Match;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxService;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The flat list of the concepts that match the concept navigator's "Show" filter, filled by hand
 * as its owner would fill it: what it shows for what it is given, and what it reports back.
 */
@ExtendWith(ApplicationExtension.class)
class ConceptMatchListUTestFX {

    private static final String COMPONENT_PATH = "Phenomenon › … › Concept model object attribute › Component";

    private final FxRobot robot = new FxRobot();
    private ConceptMatchList list;

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            list = new ConceptMatchList();
            list.setPathProvider(match -> match.nid() < 5 ? COMPONENT_PATH : "Phenomenon › Observable Entity");
            list.getMatches().setAll(
                    new Match(1, "(Agaricus bisporus+Pleurotus ostreatus+Lentinula edodes)"),
                    new Match(2, "(Diffusion capacity/Alveolar volume)"),
                    new Match(3, "(Dog dander+Cat epithelium+Horse dander)"),
                    new Match(4, "(View AP"),
                    new Match(5, "Diameter.hilum-cortex.mean | Fetus | OB ultrasound"),
                    new Match(6, "Diameter.inner orbital"));
            VBox root = new VBox(list);
            VBox.setVgrow(list, javafx.scene.layout.Priority.ALWAYS);
            root.setStyle("-fx-background-color: #6E7989;");
            Scene scene = new Scene(root, 448, 380);
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
    void theListIsAsWideAsTheTreeItStandsInFor() {
        assertEquals(448, list.minWidth(-1), "the concept navigator tree has this minimum width; a narrower list would shrink the panel");
    }

    @Test
    void everyMatchIsARowWithItsNameAndItsPath() {
        List<ListCell<?>> rows = filledRows();
        assertEquals(6, rows.size());
        assertEquals("(Agaricus bisporus+Pleurotus ostreatus+Lentinula edodes)", textOf(rows.getFirst(), ".match-name"));
        assertEquals(COMPONENT_PATH, textOf(rows.getFirst(), ".match-path"));
        assertEquals("Phenomenon › Observable Entity", textOf(rows.getLast(), ".match-path"));
        assertEquals("6 · A–Z", summary());
        robot.moveTo(rows.get(1));
        capture("concept-match-list", list);
    }

    @Test
    void textNarrowsTheRowsToTheNamesThatContainIt() {
        robot.interact(() -> list.setFilterText("DIAMETER"));
        assertEquals(2, filledRows().size(), "whatever the case of what is typed");
        assertEquals("2 of 6 · A–Z", summary());

        robot.interact(() -> list.setFilterText("no such name"));
        Label message = robot.lookup(".match-list-message").query();
        assertTrue(message.isVisible());
        assertEquals("No matching concept has that in its name.", message.getText());
        assertFalse(robot.lookup(".match-list-rows").query().isVisible());

        robot.interact(() -> list.setFilterText("  "));
        assertEquals(6, filledRows().size(), "blank text narrows nothing");
        assertFalse(message.isVisible());
    }

    @Test
    void aMessageStandsInForTheRowsWhileTheMatchesAreRead() {
        robot.interact(() -> list.setMessage("Reading the matching concepts…"));
        Label message = robot.lookup(".match-list-message").query();
        assertTrue(message.isVisible());
        assertEquals("Reading the matching concepts…", message.getText());
        assertFalse(robot.lookup(".match-list-rows").query().isVisible());
        assertEquals("", summary());

        robot.interact(() -> list.setMessage(null));
        assertFalse(message.isVisible());
        assertEquals(6, filledRows().size());
    }

    @Test
    void showInTreeReportsTheConceptOfItsRow() {
        List<Long> shown = new ArrayList<>();
        robot.interact(() -> list.setOnShowInTree(match -> shown.add(match.nid())));

        // The link is on a row only while the pointer is.
        ListCell<?> row = filledRows().get(2);
        Node link = robot.from(row).lookup(".match-show-in-tree").query();
        assertFalse(link.isVisible());
        robot.moveTo(row);
        assertTrue(link.isVisible());
        robot.clickOn(link);
        assertEquals(List.of(3L), shown);

        // A double click on a row does the same.
        robot.doubleClickOn(filledRows().get(4));
        assertEquals(List.of(3L, 5L), shown);
    }

    @Test
    void aListLongerThanItsRoomScrollsWithTheSlimScrollbar() {
        robot.interact(() -> {
            List<Match> matches = new ArrayList<>();
            for (int index = 1; index <= 40; index++) {
                matches.add(new Match(index, index + " ML ENX sodium 100 MG/ML Prefilled Syringe"));
            }
            list.getMatches().setAll(matches);
        });
        WaitForAsyncUtils.waitForFxEvents();
        ScrollBar scrollBar = robot.lookup(".match-list-rows .scroll-bar:vertical").queryAs(ScrollBar.class);
        assertTrue(scrollBar.isVisible());
        assertEquals(8, scrollBar.getWidth(), "the width of the scrollbars of the KL windows");
        assertEquals(0, robot.lookup(".match-list-rows .scroll-bar:vertical .increment-button").queryAs(Region.class).getHeight(),
                "no arrow buttons");
        capture("concept-match-list-scrolling", list);
    }

    private List<ListCell<?>> filledRows() {
        WaitForAsyncUtils.waitForFxEvents();
        return robot.lookup(".match-list-rows .list-cell").queryAll().stream()
                .<ListCell<?>>map(node -> (ListCell<?>) node)
                .filter(cell -> !cell.isEmpty())
                .sorted((first, second) -> Integer.compare(first.getIndex(), second.getIndex()))
                .toList();
    }

    private String textOf(Node row, String selector) {
        return ((Label) robot.from(row).lookup(selector).query()).getText();
    }

    private String summary() {
        WaitForAsyncUtils.waitForFxEvents();
        return ((Label) robot.lookup(".match-list-summary").query()).getText();
    }

    /** A picture of the list under kview/target, to compare against the mockup by eye. */
    private void capture(String name, Node node) {
        WaitForAsyncUtils.waitForFxEvents();
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(node).getImage(),
                Path.of("target", name + ".png"));
    }
}
