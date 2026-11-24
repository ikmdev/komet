package dev.ikm.komet.kview.controls.test;

import dev.ikm.komet.kview.controls.ConceptFilterBar;
import javafx.scene.Node;
import javafx.scene.Scene;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Follow window switch at the right end of the concept navigator's Show row.
 */
@ExtendWith(ApplicationExtension.class)
class ConceptFilterBarFollowUTestFX {

    private final FxRobot robot = new FxRobot();
    private ConceptFilterBar bar;

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupStage(stage -> {
            bar = new ConceptFilterBar();
            Scene scene = new Scene(bar, 340, 60);
            scene.getStylesheets().add(ConceptFilterBar.class
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
    void aClickOnTheSwitchTurnsFollowingOnAndASecondOneTurnsItOff() {
        Node followSwitch = robot.lookup(".follow-switch").query();
        assertEquals(false, bar.isFollowWindow(), "off by default");
        assertEquals(true, bar.getLayoutBounds().contains(followSwitch.getBoundsInParent()),
                "the whole switch fits on the row beside the All concepts pill");

        robot.clickOn(followSwitch);
        WaitForAsyncUtils.waitForFxEvents();
        FxService.serviceContext().getCaptureSupport().saveImage(robot.capture(bar).getImage(),
                Path.of("target", "concept-filter-bar-follow.png"));
        assertEquals(true, bar.isFollowWindow());

        robot.clickOn(followSwitch);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(false, bar.isFollowWindow());
    }

    @Test
    void theSwitchLeavesTheRowWhileTheListStandsInForTheTree() {
        Node followSwitch = robot.lookup(".follow-switch").query();

        robot.interact(() -> {
            bar.setFilterText("Has: Dosage");
            bar.setListShown(true);
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(false, followSwitch.isVisible());

        robot.interact(() -> bar.setListShown(false));
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(true, followSwitch.isVisible());
    }
}
