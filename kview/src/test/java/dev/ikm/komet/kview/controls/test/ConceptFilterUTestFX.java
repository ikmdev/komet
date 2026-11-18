package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.kview.controls.ConceptFilterBar;
import dev.ikm.komet.kview.controls.ConceptFilterPicker;
import dev.ikm.komet.kview.controls.ConceptFilterPicker.Choice;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The concept navigator's "Show" row and its picker as controls, filled by hand as their owner
 * would fill them: what they show for what they are given, and what they report back.
 */
@ExtendWith(ApplicationExtension.class)
class ConceptFilterUTestFX {

    private static final PseudoClass SET = PseudoClass.getPseudoClass("set");
    private static final PseudoClass CHOSEN = PseudoClass.getPseudoClass("chosen");

    private final FxRobot robot = new FxRobot();
    private ConceptFilterBar bar;
    private ConceptFilterPicker picker;

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            bar = new ConceptFilterBar();
            picker = new ConceptFilterPicker();
            VBox barHost = new VBox(bar);
            barHost.setStyle("-fx-background-color: #6E7989;");
            VBox root = new VBox(24, barHost, picker);
            root.setPadding(new Insets(16, 24, 24, 24));
            root.setStyle("-fx-background-color: #6E7989;");
            Scene scene = new Scene(root, 485, 640);
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
    void theRowNamesTheFilterAndOffersToClearIt() {
        Label pillLabel = robot.lookup(".filter-pill-label").query();
        Node pill = robot.lookup(".filter-pill").query();
        Node clear = robot.lookup(".filter-clear").query();
        assertEquals("All concepts", pillLabel.getText());
        assertFalse(pill.getPseudoClassStates().contains(SET));
        assertFalse(clear.isVisible(), "there is nothing to clear while no filter is set");
        Node viewSwitch = robot.lookup(".filter-view-switch").query();
        assertFalse(viewSwitch.isVisible(), "and no matching concepts to list");
        capture("bar-idle", bar);

        AtomicInteger opened = new AtomicInteger();
        AtomicInteger cleared = new AtomicInteger();
        robot.interact(() -> {
            bar.setOnPillAction(opened::incrementAndGet);
            bar.setOnClearAction(cleared::incrementAndGet);
            bar.setFilterText("Identifier source: LOINC Code");
            bar.setCountText("213,982\nconcepts");
        });
        assertEquals("Identifier source: LOINC Code", pillLabel.getText());
        assertTrue(pill.getPseudoClassStates().contains(SET));
        assertTrue(clear.isVisible());
        capture("bar-set", bar);

        robot.clickOn(pillLabel);
        assertEquals(1, opened.get(), "a click on the pill opens the picker");
        robot.clickOn(clear);
        assertEquals(1, cleared.get(), "a click on the cross clears the filter");
        assertEquals(1, opened.get(), "and is not also a click on the pill");

        // A set filter brings the Tree | List switch, which starts on Tree.
        assertTrue(viewSwitch.isVisible());
        List<Button> options = robot.lookup(".filter-view-option").queryAllAs(Button.class).stream().toList();
        assertEquals(List.of("Tree", "List"), options.stream().map(Button::getText).toList());
        assertFalse(bar.isListShown());
        assertTrue(options.get(0).getPseudoClassStates().contains(CHOSEN));
        robot.clickOn(options.get(1));
        assertTrue(bar.isListShown());
        assertTrue(options.get(1).getPseudoClassStates().contains(CHOSEN));
        assertFalse(options.get(0).getPseudoClassStates().contains(CHOSEN));
        capture("bar-list", bar);
        robot.clickOn(options.get(0));
        assertFalse(bar.isListShown());
    }

    @Test
    void thePickerCollectsAPatternAFieldAndAValue() {
        robot.interact(() -> picker.getPatterns().setAll(
                new Choice(1L, "Comment pattern", -1),
                new Choice(2L, "Description pattern", -1),
                new Choice(3L, "Identifier pattern", -1)));
        assertNull(picker.getSelectedPattern());

        // Typing narrows the patterns; a click on one chooses it and puts the list away.
        TextField patternField = (TextField) robot.lookup(".picker-input > .text-field").nth(0).query();
        robot.clickOn(patternField).write("ident");
        List<ListCell<?>> patternRows = filledRows(0);
        assertEquals(1, patternRows.size());
        robot.clickOn(patternRows.getFirst());
        assertEquals(3L, picker.getSelectedPattern().key());
        assertEquals("Identifier pattern", patternField.getText());
        assertFalse(robot.lookup(".picker-list").nth(0).query().isVisible());

        // The pattern alone is a filter: the field and value part stays shut behind its link.
        robot.interact(() -> {
            picker.getFields().setAll("Identifier source", "Identifier value");
            picker.setSelectedFieldIndex(0);
            picker.setSummary("1,078,640 concepts match");
            picker.setApplyDisabled(false);
        });
        Node narrowLink = robot.lookup(".picker-disclosure").query();
        Node narrowPane = robot.lookup(".picker-narrow-pane").query();
        assertTrue(narrowLink.isVisible());
        assertFalse(narrowPane.isVisible());
        assertFalse(picker.isNarrowed());
        capture("picker-pattern-only", picker);

        // A click on the link opens that part and says so.
        robot.clickOn(narrowLink);
        assertTrue(picker.isNarrowed());
        assertTrue(narrowPane.isVisible());
        robot.interact(() -> {
            picker.setSummary(null);
            picker.setApplyDisabled(true);
        });

        // The pattern's fields: one joined control, the chosen one reported by its index.
        List<ToggleButton> fields = robot.lookup(".picker-field").queryAllAs(ToggleButton.class).stream().toList();
        assertEquals(2, fields.size());
        assertTrue(fields.get(0).isSelected());
        robot.clickOn(fields.get(1));
        assertEquals(1, picker.getSelectedFieldIndex());
        robot.clickOn(fields.get(0));
        assertEquals(0, picker.getSelectedFieldIndex());

        // The values in use, with their counts; typing narrows them, a click chooses one.
        robot.interact(() -> {
            picker.getValues().setAll(
                    new Choice("gs1", "GS1", 472_827),
                    new Choice("sctid", "SCTID", 368_349),
                    new Choice("loinc", "LOINC Code", 213_982),
                    new Choice("rxcui", "RxCUI", 17_230),
                    new Choice("snomed", "SNOMED CT Identifier Source", 6_252));
            picker.setValuesMessage(null);
        });
        assertEquals(5, filledRows(1).size());
        Label countTitle = robot.lookup(".picker-values-header > .picker-values-count-title").query();
        assertTrue(countTitle.getTooltip().getText().contains("not in this tree"),
                "the Components header says why its counts can exceed the concepts that match");
        TextField valueField =(TextField) robot.lookup(".picker-input > .text-field").nth(1).query();
        robot.clickOn(valueField).write("loi");
        List<ListCell<?>> valueRows = filledRows(1);
        assertEquals(1, valueRows.size());
        assertNull(picker.getSelectedValue(), "typing narrows the list; it does not choose");
        robot.clickOn(valueRows.getFirst());
        assertEquals("loinc", picker.getSelectedValue().key());
        assertEquals("LOINC Code", valueField.getText());
        assertEquals(5, filledRows(1).size(), "the chosen value is shown among all the values again");

        // Apply is the owner's to enable, and reports the press.
        Button apply = robot.lookup(".picker-apply").queryButton();
        assertTrue(apply.isDisabled());
        AtomicInteger applied = new AtomicInteger();
        robot.interact(() -> {
            picker.setSummary("213 982 *concepts* match");
            picker.setApplyDisabled(false);
            picker.setOnApply(applied::incrementAndGet);
        });
        TextFlow summary = robot.lookup(".picker-summary").queryAs(TextFlow.class);
        assertEquals(List.of("213 982 ", "concepts", " match"),
                summary.getChildren().stream().map(node -> ((Text) node).getText()).toList());
        assertEquals("concepts", robot.lookup(".picker-summary > .picker-summary-strong").queryAs(Text.class).getText(),
                "the word that tells the count apart from the Components column is the bold one");
        capture("picker", picker);
        robot.clickOn(apply);
        assertEquals(1, applied.get());

        // Clearing the pattern brings its list back and leaves no pattern chosen, so nothing to narrow.
        robot.clickOn(".picker-row-action");
        assertNull(picker.getSelectedPattern());
        assertTrue(robot.lookup(".picker-list").nth(0).query().isVisible());
        assertEquals(3, filledRows(0).size());
        assertFalse(narrowLink.isVisible());
        assertFalse(narrowPane.isVisible());
    }

    @Test
    void aPatternWithoutFieldsOffersNothingToNarrowBy() {
        robot.interact(() -> {
            picker.getPatterns().setAll(new Choice(4L, "Membership pattern", -1));
            picker.setSelectedPattern(picker.getPatterns().getFirst());
            picker.setNarrowed(true);
        });
        assertFalse(robot.lookup(".picker-disclosure").query().isVisible());
        assertFalse(robot.lookup(".picker-narrow-pane").query().isVisible());

        robot.interact(() -> picker.getFields().setAll("Member of"));
        assertTrue(robot.lookup(".picker-disclosure").query().isVisible());
        assertTrue(robot.lookup(".picker-narrow-pane").query().isVisible());
    }

    @Test
    void aMessageStandsInForValuesThatCannotBeListed() {
        robot.interact(() -> picker.setValuesMessage("Too many different values to list."));
        Label message = robot.lookup(".picker-narrow-pane > .picker-values-message").query();
        assertTrue(message.isVisible());
        assertEquals("Too many different values to list.", message.getText());
        assertFalse(robot.lookup(".picker-list").nth(1).query().isVisible());

        robot.interact(() -> picker.setValuesMessage(null));
        assertFalse(message.isVisible());
        assertTrue(robot.lookup(".picker-list").nth(1).query().isVisible());
    }

    /** The rows that hold a choice in the picker's first (patterns) or second (values) list. */
    private List<ListCell<?>> filledRows(int listIndex) {
        WaitForAsyncUtils.waitForFxEvents();
        Node list = robot.lookup(".picker-list").nth(listIndex).query();
        return robot.from(list).lookup(".list-cell").queryAll().stream()
                .<ListCell<?>>map(node -> (ListCell<?>) node)
                .filter(cell -> !cell.isEmpty())
                .toList();
    }

    /** A picture of the node under kview/target, to compare against the mockup by eye. */
    private void capture(String name, Node node) {
        WaitForAsyncUtils.waitForFxEvents();
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(node).getImage(),
                Path.of("target", "concept-filter-" + name + ".png"));
    }
}
