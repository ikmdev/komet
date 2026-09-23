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

import dev.ikm.komet.kview.controls.FilterComboBox;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Type to search in {@link FilterComboBox}: typing narrows the popup to the options whose text
 * contains the typed text and Enter picks the highlighted match, so the user can choose an option
 * without the mouse. Narrowing the options must never change the value by itself.
 */
@ExtendWith(ApplicationExtension.class)
class FilterComboBoxUTestFX {

    private static final String CASE_SENSITIVE = "Case sensitive";
    private static final String CASE_INSENSITIVE = "Case insensitive";
    private static final String INITIAL_CHARACTER_CASE_SENSITIVE = "Initial character case sensitive";
    private static final List<String> OPTIONS =
            List.of(CASE_SENSITIVE, CASE_INSENSITIVE, INITIAL_CHARACTER_CASE_SENSITIVE);

    private Stage stage;
    private FilterComboBox<String> filterComboBox;
    private final List<String> valueChanges = new ArrayList<>();

    @BeforeEach
    void showControl() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            this.stage = stage;
            filterComboBox = new FilterComboBox<>();
            filterComboBox.getItems().setAll(OPTIONS);
            filterComboBox.setValue(CASE_SENSITIVE);
            filterComboBox.valueProperty().subscribe((_, newValue) -> valueChanges.add(newValue));
            stage.setScene(new Scene(new StackPane(filterComboBox), 400, 300));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
        // The control hands the focus on to its combo box
        FxToolkit.setupFixture(() -> filterComboBox.requestFocus());
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeStage() throws Exception {
        FxToolkit.setupFixture(() -> stage.close());
    }

    private ComboBox<String> comboBox() {
        return new FxRobot().lookup(".filter-combo-box .combo-box").queryAs(ComboBox.class);
    }

    private List<String> shownOptions() {
        return new FxRobot().lookup(".combo-box-popup .list-view").queryAs(ListView.class).getItems();
    }

    private String searchText() {
        Label searchLabel = new FxRobot().lookup(".search-text").queryAs(Label.class);
        return searchLabel.isVisible() ? searchLabel.getText() : "";
    }

    @Test
    void theComboBoxTakesTheFocus() {
        assertTrue(comboBox().isFocused());
    }

    @Test
    void typingNarrowsTheOptionsAndEnterPicksTheMatch() {
        FxRobot robot = new FxRobot();
        robot.write("insen");

        assertTrue(comboBox().isShowing());
        assertEquals("insen", searchText());
        assertEquals(List.of(CASE_INSENSITIVE), shownOptions());
        assertEquals(List.of(), valueChanges, "narrowing the options must not change the value");
        ListView<String> popupList = new FxRobot().lookup(".combo-box-popup .list-view").queryAs(ListView.class);
        assertEquals(-1, popupList.getSelectionModel().getSelectedIndex(), "only the value shows as selected");
        assertEquals(0, popupList.getFocusModel().getFocusedIndex(), "the first match is highlighted");

        robot.type(KeyCode.ENTER);

        assertEquals(CASE_INSENSITIVE, filterComboBox.getValue());
        assertEquals(List.of(CASE_INSENSITIVE), valueChanges);
        assertFalse(comboBox().isShowing());
        assertEquals("", searchText());
        assertEquals(OPTIONS, comboBox().getItems());
    }

    @Test
    void matchingIgnoresCaseAndFindsTextAnywhereInTheName() {
        new FxRobot().write("CASE SENS");

        assertEquals(List.of(CASE_SENSITIVE, INITIAL_CHARACTER_CASE_SENSITIVE), shownOptions());
    }

    @Test
    void enterPicksTheCurrentValueWhenItStillMatches() {
        FxRobot robot = new FxRobot();
        robot.write("case sens");
        robot.type(KeyCode.ENTER);

        assertEquals(CASE_SENSITIVE, filterComboBox.getValue());
        assertEquals(List.of(), valueChanges);
    }

    @Test
    void theArrowKeysMoveTheHighlightAmongTheMatches() {
        FxRobot robot = new FxRobot();
        robot.write("case sens");
        robot.type(KeyCode.DOWN);
        robot.type(KeyCode.ENTER);

        assertEquals(INITIAL_CHARACTER_CASE_SENSITIVE, filterComboBox.getValue());
    }

    @Test
    void backspaceWidensTheSearch() {
        FxRobot robot = new FxRobot();
        robot.write("insen");
        robot.type(KeyCode.BACK_SPACE, 3);

        assertEquals("in", searchText());
        assertEquals(List.of(CASE_INSENSITIVE, INITIAL_CHARACTER_CASE_SENSITIVE), shownOptions());
    }

    @Test
    void escapeAbandonsTheSearchAndKeepsTheValue() {
        FxRobot robot = new FxRobot();
        robot.write("insen");
        robot.type(KeyCode.ESCAPE);

        assertFalse(comboBox().isShowing());
        assertEquals("", searchText());
        assertEquals(CASE_SENSITIVE, filterComboBox.getValue());
        assertEquals(List.of(), valueChanges);
        assertEquals(OPTIONS, comboBox().getItems());
        assertEquals(0, comboBox().getSelectionModel().getSelectedIndex(), "the value is selected again");
    }

    @Test
    void noMatchLeavesTheValueAlone() {
        FxRobot robot = new FxRobot();
        robot.write("xyz");
        assertEquals(List.of(), shownOptions());

        robot.type(KeyCode.ENTER);

        assertEquals(CASE_SENSITIVE, filterComboBox.getValue());
        assertEquals(List.of(), valueChanges);
    }

    @Test
    void theValueFollowsTheControl() throws Exception {
        FxToolkit.setupFixture(() -> filterComboBox.setValue(INITIAL_CHARACTER_CASE_SENSITIVE));

        assertEquals(INITIAL_CHARACTER_CASE_SENSITIVE, comboBox().getValue());
    }
}
