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

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.kview.controls.KLComponentComboBoxControl;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.KernelConcept;
import javafx.beans.property.Property;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.ikm.komet.terms.KometConcept.BLANK_CONCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A new semantic's component fields start with the value
 * {@link ObservableComposer#generateDefaultFieldValues} seeds, which reaches the component
 * controls through an untyped field-value list and the editable field's binding. Both controls
 * a component field is edited with must take that seed: the {@code KometConcept} enum once
 * arrived there raw and threw {@code ClassCastException} in {@code KLComponentComboBoxControlSkin}
 * when Create semantic was clicked.
 */
@ExtendWith(ApplicationExtension.class)
class NewSemanticComponentFieldUTestFX {

    private static final EntityProxy.Concept ENGLISH =
            EntityProxy.Concept.make("English language", UUID.randomUUID());

    private final List<Throwable> fxThreadFailures = new CopyOnWriteArrayList<>();
    private Stage stage;

    /** The controls resolve nids (e.g. to tell a blank value), so a store must be running */
    @BeforeAll
    static void startEphemeralStore() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("new-semantic-component-field-test").toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @BeforeEach
    void captureFxThreadFailures() throws Exception {
        FxToolkit.registerPrimaryStage();
        // A skin's failure surfaces on the FX thread, not in the test
        FxToolkit.setupFixture(() -> Thread.currentThread()
                .setUncaughtExceptionHandler((thread, failure) -> fxThreadFailures.add(failure)));
    }

    @AfterEach
    void closeStage() throws Exception {
        FxToolkit.setupFixture(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(null);
            if (stage != null) {
                stage.close();
            }
        });
    }

    @Test
    void theComponentControlTakesANewSemanticsSeed() throws Exception {
        KLComponentControl control = new KLComponentControl();
        control.setComponentNameRenderer(entity -> "rendered");
        show(control);

        seed(control.entityProperty());

        assertNoFxThreadFailure();
        assertTrue(control.isEmpty(), "the seed is the blank concept");
    }

    @Test
    void theComponentComboBoxTakesANewSemanticsSeed() throws Exception {
        KLComponentComboBoxControl control = new KLComponentComboBoxControl();
        control.setComponentNameRenderer(entity -> "rendered");
        control.getItems().setAll(ENGLISH);
        show(control);

        seed(control.valueProperty());

        assertNoFxThreadFailure();
        assertEquals(BLANK_CONCEPT.nid(), ((EntityFacade) control.getValue()).nid());
    }

    @Test
    void theComponentControlsTakeABindingEnum() throws Exception {
        // The controls hold any facade, so a binding enum needs no conversion to a proxy
        KLComponentControl componentControl = new KLComponentControl();
        componentControl.setComponentNameRenderer(entity -> "rendered");
        KLComponentComboBoxControl comboBoxControl = new KLComponentComboBoxControl();
        comboBoxControl.setComponentNameRenderer(entity -> "rendered");
        comboBoxControl.getItems().setAll(ENGLISH);
        show(new VBox(componentControl, comboBoxControl));

        FxToolkit.setupFixture(() -> {
            componentControl.setEntity(KernelConcept.ENGLISH_LANGUAGE);
            comboBoxControl.setValue(KernelConcept.ENGLISH_LANGUAGE);
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertNoFxThreadFailure();
        assertFalse(componentControl.isEmpty(), "a concept is not the blank value");
        assertTrue(EntityFacade.same(KernelConcept.ENGLISH_LANGUAGE, comboBoxControl.getValue()));
    }

    private void show(Region control) throws Exception {
        FxToolkit.setupStage(stage -> {
            this.stage = stage;
            stage.setScene(new Scene(new StackPane(control), 400, 300));
            stage.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Sets a control's value the way an editable field's binding does: from the untyped
     * field-value list, unchecked.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void seed(Property<?> valueProperty) throws Exception {
        Object seed = ObservableComposer.defaultComponentFieldValue();
        FxToolkit.setupFixture(() -> ((Property) valueProperty).setValue(seed));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void assertNoFxThreadFailure() {
        assertTrue(fxThreadFailures.isEmpty(), () -> "the control failed on the FX thread: " + fxThreadFailures);
    }
}
