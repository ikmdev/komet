package dev.ikm.komet.kview.mvvm.viewmodel.test;

import dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel;
import javafx.beans.property.BooleanProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel.ConfirmationPropertyName.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfirmationPaneViewModelTest {

    private ConfirmationPaneViewModel viewModel;

    @BeforeEach
    public void setUp() {
        viewModel = new ConfirmationPaneViewModel();
    }

    /**
     * Verifies that the title and message properties are set to known values.
     */
    @Test
    public void titleMessageTest() {
        final String TITLE = "title";
        final String MESSAGE = "message";

        viewModel.setPropertyValue(CONFIRMATION_TITLE, TITLE);
        viewModel.setPropertyValue(CONFIRMATION_MESSAGE, MESSAGE);

        assertEquals(TITLE, viewModel.getPropertyValue(CONFIRMATION_TITLE));
        assertEquals(MESSAGE, viewModel.getPropertyValue(CONFIRMATION_MESSAGE));
    }

    /**
     * Verifies that when the CLOSE_CONFIRMATION_PANEL property is set to true that the
     * subscribe() method is called for the property, which is needed to handle the
     * CLOSE PROPERTIES PANEL button being pressed.
     */
    @Test
    public void closePropertyTest() {
        final AtomicBoolean subscribeCalled = new AtomicBoolean(false);

        BooleanProperty closeConfPaneProp = viewModel.getBooleanProperty(CLOSE_CONFIRMATION_PANEL);

        closeConfPaneProp.subscribe(closeIt -> {
            if (closeIt) {
                subscribeCalled.set(true);
            }
        });

        closeConfPaneProp.setValue(true);

        assertTrue(subscribeCalled.get());
    }

    /**
     * Verifies that the reset() method on the view model changes the values to the default values.
     */
    @Test
    public void resetTest() {
        final String TITLE = "title";
        final String MESSAGE = "message";

        viewModel.setPropertyValue(CONFIRMATION_TITLE, TITLE);
        viewModel.setPropertyValue(CONFIRMATION_MESSAGE, MESSAGE);

        viewModel.setPropertyValue(CLOSE_CONFIRMATION_PANEL, true);

        viewModel.reset();

        assertEquals("", viewModel.getPropertyValue(CONFIRMATION_TITLE));
        assertEquals("", viewModel.getPropertyValue(CONFIRMATION_MESSAGE));
        assertEquals(false, viewModel.getPropertyValue(CLOSE_CONFIRMATION_PANEL));
    }

}
