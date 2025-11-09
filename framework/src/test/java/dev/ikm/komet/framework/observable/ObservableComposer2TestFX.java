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
package dev.ikm.komet.framework.observable;

import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.integration.NewEphemeralKeyValueProvider;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension;
import dev.ikm.komet.framework.testing.JavaFXThreadExtension.RunOnJavaFXThread;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ObservableComposer.
 * Tests the composer's core functionality without requiring a full database.
 */
@ExtendWith({JavaFXThreadExtension.class, NewEphemeralKeyValueProvider.class})
@RunOnJavaFXThread
class ObservableComposer2TestFX {

    @Test
    void testCreateComposerWithStaticFactory() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertNotNull(composer);
        assertEquals(State.ACTIVE, composer.getDefaultState());
        assertEquals(TinkarTerm.USER.nid(), composer.getAuthorNid());
        assertEquals(TinkarTerm.PRIMORDIAL_MODULE.nid(), composer.getModuleNid());
        assertEquals(TinkarTerm.DEVELOPMENT_PATH.nid(), composer.getPathNid());
    }

    @Test
    void testCreateComposerWithComment() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH,
                "Test transaction"
        );

        assertNotNull(composer);
        assertEquals(State.ACTIVE, composer.getDefaultState());
    }

    @Test
    void testBuilderPattern() {
        ObservableComposer composer = ObservableComposer.builder()
                .viewCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
                .author(TinkarTerm.USER)
                .module(TinkarTerm.PRIMORDIAL_MODULE)
                .path(TinkarTerm.DEVELOPMENT_PATH)
                .defaultState(State.INACTIVE)
                .transactionComment("Builder test")
                .build();

        assertNotNull(composer);
        assertEquals(State.INACTIVE, composer.getDefaultState());
        assertEquals(TinkarTerm.USER.nid(), composer.getAuthorNid());
    }

    @Test
    void testBuilderRequiresAuthor() {
        assertThrows(NullPointerException.class, () -> {
            ObservableComposer.builder()
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .build();
        });
    }

    @Test
    void testBuilderRequiresModule() {
        assertThrows(NullPointerException.class, () -> {
            ObservableComposer.builder()
                    .author(TinkarTerm.USER)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .build();
        });
    }

    @Test
    void testBuilderRequiresPath() {
        assertThrows(NullPointerException.class, () -> {
            ObservableComposer.builder()
                    .author(TinkarTerm.USER)
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .build();
        });
    }

    @Test
    void testBuilderRejectsNullAuthor() {
        assertThrows(NullPointerException.class, () -> {
            ObservableComposer.builder()
                    .author(null)
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .build();
        });
    }

    @Test
    void testBuilderRejectsNullModule() {
        assertThrows(NullPointerException.class, () -> {
            ObservableComposer.builder()
                    .author(TinkarTerm.USER)
                    .module(null)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .build();
        });
    }

    @Test
    void testBuilderRejectsNullPath() {
        assertThrows(NullPointerException.class, () -> {
            ObservableComposer.builder()
                    .author(TinkarTerm.USER)
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .path(null)
                    .build();
        });
    }

    @Test
    void testBuilderRejectsNullDefaultState() {
        assertThrows(NullPointerException.class, () -> {
            ObservableComposer.builder()
                    .author(TinkarTerm.USER)
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .defaultState(null)
                    .build();
        });
    }

    @Test
    void testInitialTransactionState() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());
        assertFalse(composer.hasUncommittedChanges());
    }

    @Test
    void testTransactionCreatedLazily() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertNull(composer.getTransaction());

        // Trigger transaction creation
        composer.getOrCreateTransaction();

        assertNotNull(composer.getTransaction());
        assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer.getTransactionState());
    }

    @Test
    void testCancelTransaction() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        composer.getOrCreateTransaction();
        assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer.getTransactionState());

        composer.cancel();

        assertNull(composer.getTransaction());
        assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());
        assertFalse(composer.hasUncommittedChanges());
    }

    @Test
    void testCancelWithoutTransaction() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        // Should not throw
        composer.cancel();

        assertNull(composer.getTransaction());
        assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());
    }

    @Test
    void testTransactionStateProperty() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        AtomicReference<ObservableComposer.TransactionState> capturedState = new AtomicReference<>();
        composer.transactionStateProperty().addListener((obs, oldVal, newVal) -> {
            capturedState.set(newVal);
        });

        composer.getOrCreateTransaction();

        assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, capturedState.get());
    }

    @Test
    void testHasUncommittedChangesPropertyInitiallyFalse() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertFalse(composer.hasUncommittedChanges());
        assertNotNull(composer.hasUncommittedChangesProperty());
    }

    @Test
    void testTransactionStatePropertyReadOnly() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertNotNull(composer.transactionStateProperty());
        // Verify it's a ReadOnlyObjectProperty by checking the type
        assertTrue(composer.transactionStateProperty() instanceof javafx.beans.property.ReadOnlyObjectProperty);
    }

    @Test
    void testHasUncommittedChangesPropertyReadOnly() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertNotNull(composer.hasUncommittedChangesProperty());
        // Verify it's a ReadOnlyBooleanProperty by checking the type
        assertTrue(composer.hasUncommittedChangesProperty() instanceof javafx.beans.property.ReadOnlyBooleanProperty);
    }

    @Test
    void testRequiresJavaFXThread() throws Exception {
        // This should throw when called from non-JavaFX thread
        // We need to explicitly run this on a different thread since the class-level annotation runs all tests on FX thread
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<RuntimeException> exceptionRef = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                ObservableComposer.create(
                        Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                        State.ACTIVE,
                        TinkarTerm.USER,
                        TinkarTerm.PRIMORDIAL_MODULE,
                        TinkarTerm.DEVELOPMENT_PATH
                );
            } catch (RuntimeException e) {
                exceptionRef.set(e);
            } finally {
                latch.countDown();
            }
        });
        thread.start();

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test timed out");
        RuntimeException exception = exceptionRef.get();
        assertNotNull(exception, "Expected RuntimeException to be thrown");
        assertTrue(exception.getMessage().contains("JavaFX"));
    }

    @Test
    void testBuilderDefaultState() {
        ObservableComposer composer = ObservableComposer.builder()
                .viewCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
                .author(TinkarTerm.USER)
                .module(TinkarTerm.PRIMORDIAL_MODULE)
                .path(TinkarTerm.DEVELOPMENT_PATH)
                .build();

        // Default state should be ACTIVE
        assertEquals(State.ACTIVE, composer.getDefaultState());
    }

    @Test
    void testBuilderNullTransactionComment() {
        ObservableComposer composer = ObservableComposer.builder()
                .viewCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
                .author(TinkarTerm.USER)
                .module(TinkarTerm.PRIMORDIAL_MODULE)
                .path(TinkarTerm.DEVELOPMENT_PATH)
                .transactionComment(null)
                .build();

        assertNotNull(composer);
    }

    @Test
    void testStaticFactoryNullTransactionComment() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH,
                null
        );

        assertNotNull(composer);
    }

    @Test
    void testMultipleTransactionCreations() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        var transaction1 = composer.getOrCreateTransaction();
        var transaction2 = composer.getOrCreateTransaction();

        // Should return the same transaction instance
        assertSame(transaction1, transaction2);
    }

    @Test
    void testStatePropertyBindable() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        javafx.beans.property.SimpleObjectProperty<ObservableComposer.TransactionState> boundProperty =
                new javafx.beans.property.SimpleObjectProperty<>();
        boundProperty.bind(composer.transactionStateProperty());

        assertEquals(ObservableComposer.TransactionState.NONE, boundProperty.get());

        composer.getOrCreateTransaction();

        assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, boundProperty.get());
    }

    @Test
    void testInactiveStateComposer() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.INACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertEquals(State.INACTIVE, composer.getDefaultState());
    }

    @Test
    void testCanceledStateComposer() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.CANCELED,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertEquals(State.CANCELED, composer.getDefaultState());
    }

    @Test
    void testPrimordialStateComposer() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.PRIMORDIAL,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertEquals(State.PRIMORDIAL, composer.getDefaultState());
    }

    @Test
    void testWithdrawnStateComposer() {
        ObservableComposer composer = ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.WITHDRAWN,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );

        assertEquals(State.WITHDRAWN, composer.getDefaultState());
    }
}
