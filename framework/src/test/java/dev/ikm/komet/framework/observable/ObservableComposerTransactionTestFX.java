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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ObservableComposer transaction state management.
 * Tests transaction lifecycle, state transitions, and property notifications.
 */
@ExtendWith({JavaFXThreadExtension.class, NewEphemeralKeyValueProvider.class})
@RunOnJavaFXThread
class ObservableComposerTransactionTestFX {

    @Test
    @RunOnJavaFXThread
    void testTransactionStateEnum() {
            // Verify all transaction states exist
            ObservableComposer.TransactionState[] states = ObservableComposer.TransactionState.values();

            assertEquals(4, states.length);

            // Verify state names
            ObservableComposer.TransactionState none = ObservableComposer.TransactionState.valueOf("NONE");
            ObservableComposer.TransactionState uncommitted = ObservableComposer.TransactionState.valueOf("UNCOMMITTED");
            ObservableComposer.TransactionState committed = ObservableComposer.TransactionState.valueOf("COMMITTED");
            ObservableComposer.TransactionState rolledBack = ObservableComposer.TransactionState.valueOf("ROLLED_BACK");

            assertNotNull(none);
            assertNotNull(uncommitted);
            assertNotNull(committed);
            assertNotNull(rolledBack);
    }

    @Test
    @RunOnJavaFXThread
    void testInitialStateIsNone() {
            ObservableComposer composer = createComposer();

            assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());
    }

    @Test
    @RunOnJavaFXThread
    void testTransactionCreationChangesStateToUncommitted() {
            ObservableComposer composer = createComposer();

            composer.getOrCreateTransaction();

            assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer.getTransactionState());
    }

    @Test
    @RunOnJavaFXThread
    void testCancelChangesStateToNone() {
            ObservableComposer composer = createComposer();

            composer.getOrCreateTransaction();
            composer.cancel();

            assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());
    }

    @Test
    @RunOnJavaFXThread
    void testStateTransitionNotification() {
            ObservableComposer composer = createComposer();

            AtomicInteger notificationCount = new AtomicInteger(0);
            AtomicReference<ObservableComposer.TransactionState> lastState = new AtomicReference<>();

            composer.transactionStateProperty().addListener((obs, oldVal, newVal) -> {
                notificationCount.incrementAndGet();
                lastState.set(newVal);
            });

            // Initial state: NONE
            assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());

            // Create transaction: NONE → UNCOMMITTED
            composer.getOrCreateTransaction();
            assertEquals(1, notificationCount.get());
            assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, lastState.get());

            // Cancel transaction: UNCOMMITTED → NONE
            composer.cancel();
            assertEquals(2, notificationCount.get());
            assertEquals(ObservableComposer.TransactionState.NONE, lastState.get());
    }

    @Test
    @RunOnJavaFXThread
    void testMultipleStateTransitions() {
            ObservableComposer composer = createComposer();

            AtomicInteger notificationCount = new AtomicInteger(0);
            composer.transactionStateProperty().addListener((obs, oldVal, newVal) -> {
                notificationCount.incrementAndGet();
            });

            // NONE → UNCOMMITTED
            composer.getOrCreateTransaction();
            assertEquals(1, notificationCount.get());

            // UNCOMMITTED → NONE
            composer.cancel();
            assertEquals(2, notificationCount.get());

            // NONE → UNCOMMITTED (new transaction)
            composer.getOrCreateTransaction();
            assertEquals(3, notificationCount.get());

            // UNCOMMITTED → NONE
            composer.cancel();
            assertEquals(4, notificationCount.get());
    }

    @Test
    @RunOnJavaFXThread
    void testHasUncommittedChangesInitiallyFalse() {
            ObservableComposer composer = createComposer();

            assertFalse(composer.hasUncommittedChanges());
    }

    @Test
    @RunOnJavaFXThread
    void testHasUncommittedChangesAfterCancel() {
            ObservableComposer composer = createComposer();

            composer.getOrCreateTransaction();
            composer.cancel();

            assertFalse(composer.hasUncommittedChanges());
    }

    @Test
    @RunOnJavaFXThread
    void testTransactionCommentWithStaticFactory() {
            String comment = "Test transaction comment";
            ObservableComposer composer = ObservableComposer.create(
                    Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                    State.ACTIVE,
                    TinkarTerm.USER,
                    TinkarTerm.PRIMORDIAL_MODULE,
                    TinkarTerm.DEVELOPMENT_PATH,
                    comment
            );

            assertNotNull(composer);
            // Comment is stored internally, create transaction to use it
            composer.getOrCreateTransaction();
            assertNotNull(composer.getTransaction());
    }

    @Test
    @RunOnJavaFXThread
    void testTransactionCommentWithBuilder() {
            String comment = "Builder transaction comment";
            ObservableComposer composer = ObservableComposer.builder()
                    .viewCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
                    .author(TinkarTerm.USER)
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .transactionComment(comment)
                    .build();

            assertNotNull(composer);
            composer.getOrCreateTransaction();
            assertNotNull(composer.getTransaction());
    }

    @Test
    @RunOnJavaFXThread
    void testEmptyTransactionComment() {
            ObservableComposer composer = ObservableComposer.builder()
                    .viewCalculator(Coordinates.Stamp.DevelopmentLatest().stampCalculator())
                    .author(TinkarTerm.USER)
                    .module(TinkarTerm.PRIMORDIAL_MODULE)
                    .path(TinkarTerm.DEVELOPMENT_PATH)
                    .transactionComment("")
                    .build();

            assertNotNull(composer);
            composer.getOrCreateTransaction();
            assertNotNull(composer.getTransaction());
    }

    @Test
    @RunOnJavaFXThread
    void testTransactionLifecycle() {
            ObservableComposer composer = createComposer();

            // Initial: no transaction
            assertNull(composer.getTransaction());
            assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());

            // Create transaction
            var transaction = composer.getOrCreateTransaction();
            assertNotNull(transaction);
            assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, composer.getTransactionState());

            // Same transaction on repeated calls
            var sameTransaction = composer.getOrCreateTransaction();
            assertSame(transaction, sameTransaction);

            // Cancel clears transaction
            composer.cancel();
            assertNull(composer.getTransaction());
            assertEquals(ObservableComposer.TransactionState.NONE, composer.getTransactionState());

            // New transaction after cancel
            var newTransaction = composer.getOrCreateTransaction();
            assertNotNull(newTransaction);
            assertNotSame(transaction, newTransaction);
    }

    @Test
    @RunOnJavaFXThread
    void testPropertyBindingToTransactionState() {
            ObservableComposer composer = createComposer();

            javafx.beans.property.SimpleObjectProperty<ObservableComposer.TransactionState> boundProperty =
                    new javafx.beans.property.SimpleObjectProperty<>();
            boundProperty.bind(composer.transactionStateProperty());

            assertEquals(ObservableComposer.TransactionState.NONE, boundProperty.get());

            composer.getOrCreateTransaction();
            assertEquals(ObservableComposer.TransactionState.UNCOMMITTED, boundProperty.get());

            composer.cancel();
            assertEquals(ObservableComposer.TransactionState.NONE, boundProperty.get());
    }

    @Test
    @RunOnJavaFXThread
    void testPropertyBindingToHasUncommittedChanges() {
            ObservableComposer composer = createComposer();

            javafx.beans.property.SimpleBooleanProperty boundProperty =
                    new javafx.beans.property.SimpleBooleanProperty();
            boundProperty.bind(composer.hasUncommittedChangesProperty());

            assertFalse(boundProperty.get());

            composer.getOrCreateTransaction();
            // Still false because no editables tracked
            assertFalse(boundProperty.get());

            composer.cancel();
            assertFalse(boundProperty.get());
    }

    @Test
    @RunOnJavaFXThread
    void testStatePropertyNotifications() {
            ObservableComposer composer = createComposer();

            AtomicInteger changeCount = new AtomicInteger(0);
            composer.transactionStateProperty().addListener((observable, oldValue, newValue) -> {
                changeCount.incrementAndGet();
                assertNotNull(oldValue);
                assertNotNull(newValue);
                assertNotEquals(oldValue, newValue);
            });

            composer.getOrCreateTransaction();
            assertTrue(changeCount.get() > 0);

            int countBeforeCancel = changeCount.get();
            composer.cancel();
            assertTrue(changeCount.get() > countBeforeCancel);
    }

    /**
     * Helper method to create a composer with standard test configuration.
     */
    private ObservableComposer createComposer() {
        return ObservableComposer.create(
                Coordinates.Stamp.DevelopmentLatest().stampCalculator(),
                State.ACTIVE,
                TinkarTerm.USER,
                TinkarTerm.PRIMORDIAL_MODULE,
                TinkarTerm.DEVELOPMENT_PATH
        );
    }
}
