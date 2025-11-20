package dev.ikm.komet.framework.observable.collection;

import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObservableLongList Observable Behavior Tests")
class ObservableLongListObservableTest {

    @Nested
    @DisplayName("Change Listener Tests")
    class ChangeListenerTests {

        private ObservableLongList list;
        private List<String> changeLog;

        @BeforeEach
        void setUp() {
            list = new ObservableLongList(10L, 20L, 30L);
            changeLog = new ArrayList<>();

            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        changeLog.add("ADDED:" + change.getAddedSize() + " at " + change.getFrom());
                    }
                    if (change.wasRemoved()) {
                        changeLog.add("REMOVED:" + change.getRemovedSize() + " at " + change.getFrom());
                    }
                    if (change.wasReplaced()) {
                        changeLog.add("REPLACED at " + change.getFrom());
                    }
                    if (change.wasUpdated()) {
                        changeLog.add("UPDATED at " + change.getFrom());
                    }
                }
            });
        }

        @Test
        @DisplayName("addLong triggers add change notification")
        void testAddLongTriggersChange() {
            list.addLong(40L);

            assertEquals(1, changeLog.size());
            assertTrue(changeLog.get(0).startsWith("ADDED:1"));
        }

        @Test
        @DisplayName("add (boxed) triggers add change notification")
        void testAddBoxedTriggersChange() {
            list.add(50L);

            assertEquals(1, changeLog.size());
            assertTrue(changeLog.get(0).startsWith("ADDED:1"));
        }

        @Test
        @DisplayName("remove by index triggers remove change notification")
        void testRemoveTriggersChange() {
            list.remove(1);

            assertEquals(1, changeLog.size());
            assertTrue(changeLog.get(0).startsWith("REMOVED:1"));
        }

        @Test
        @DisplayName("removeLong triggers remove change notification")
        void testRemoveLongTriggersChange() {
            list.removeLong(20L);

            assertEquals(1, changeLog.size());
            assertTrue(changeLog.get(0).startsWith("REMOVED:1"));
        }

        @Test
        @DisplayName("set triggers change notification")
        void testSetTriggersChange() {
            list.set(1, 200L);

            // set() operation typically triggers both REMOVED and ADDED changes
            assertTrue(changeLog.size() >= 1, "Should have at least one change notification");
            boolean hasRemoved = changeLog.stream().anyMatch(log -> log.contains("REMOVED"));
            boolean hasAdded = changeLog.stream().anyMatch(log -> log.contains("ADDED"));
            assertTrue(hasRemoved || hasAdded, "Should have REMOVED or ADDED notification");
        }

        @Test
        @DisplayName("clear triggers remove change notification")
        void testClearTriggersChange() {
            list.clear();

            assertEquals(1, changeLog.size());
            assertTrue(changeLog.get(0).startsWith("REMOVED:3"));
        }

        @Test
        @DisplayName("Multiple operations trigger multiple notifications")
        void testMultipleOperations() {
            list.addLong(40L);
            list.removeLong(10L);
            list.set(0, 200L);

            assertTrue(changeLog.size() >= 3, "Should have at least 3 change notifications");
        }

        @Test
        @DisplayName("No changes when retrieving values")
        void testNoChangesOnGet() {
            list.getLong(0);
            list.get(1);
            list.size();
            list.isEmpty();

            assertEquals(0, changeLog.size(), "Read operations should not trigger changes");
        }
    }

    @Nested
    @DisplayName("Multiple Listeners Tests")
    class MultipleListenersTests {

        private ObservableLongList list;
        private int listener1Count;
        private int listener2Count;

        @BeforeEach
        void setUp() {
            list = new ObservableLongList(1L, 2L, 3L);
            listener1Count = 0;
            listener2Count = 0;

            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    listener1Count++;
                }
            });

            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    listener2Count++;
                }
            });
        }

        @Test
        @DisplayName("Both listeners receive change notifications")
        void testMultipleListenersNotified() {
            list.addLong(4L);

            assertEquals(1, listener1Count, "Listener 1 should receive notification");
            assertEquals(1, listener2Count, "Listener 2 should receive notification");
        }

        @Test
        @DisplayName("Removing listener stops notifications")
        void testRemoveListener() {
            ObservableLongList newList = new ObservableLongList(1L, 2L, 3L);
            int[] listenerCount = {0};

            ListChangeListener<Long> listener = change -> {
                while (change.next()) {
                    listenerCount[0] += 100;
                }
            };

            newList.addListener(listener);
            newList.addLong(10L);
            int countAfterFirst = listenerCount[0];
            assertEquals(100, countAfterFirst, "Listener should have been notified once");

            newList.removeListener(listener);
            newList.addLong(20L);

            assertEquals(countAfterFirst, listenerCount[0], "Removed listener should not receive notifications");
        }
    }

    @Nested
    @DisplayName("Change Details Tests")
    class ChangeDetailsTests {

        private ObservableLongList list;

        @BeforeEach
        void setUp() {
            list = new ObservableLongList(10L, 20L, 30L, 40L, 50L);
        }

        @Test
        @DisplayName("Add change provides correct position")
        void testAddChangePosition() {
            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        assertEquals(5, change.getFrom(), "Element added at end should be at index 5");
                        assertEquals(6, change.getTo(), "To index should be 6");
                    }
                }
            });

            list.addLong(60L);
        }

        @Test
        @DisplayName("Remove change provides correct removed items")
        void testRemoveChangeDetails() {
            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    if (change.wasRemoved()) {
                        List<? extends Long> removed = change.getRemoved();
                        assertEquals(1, removed.size(), "Should have 1 removed item");
                        assertEquals(30L, removed.get(0), "Removed item should be 30");
                    }
                }
            });

            list.remove(2);
        }

        @Test
        @DisplayName("Set change shows replacement")
        void testSetChangeDetails() {
            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    assertEquals(1, change.getFrom(), "Change should be at index 1");
                }
            });

            list.set(1, 200L);
        }
    }

    @Nested
    @DisplayName("Batch Operations Tests")
    class BatchOperationsTests {

        @Test
        @DisplayName("addAll triggers single change notification")
        void testAddAll() {
            ObservableLongList list = new ObservableLongList(1L, 2L, 3L);
            int[] changeCount = {0};

            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    changeCount[0]++;
                }
            });

            List<Long> toAdd = List.of(4L, 5L, 6L);
            list.addAll(toAdd);

            assertEquals(1, changeCount[0], "addAll should trigger single change notification");
            assertEquals(6, list.size());
        }

        @Test
        @DisplayName("removeAll triggers change notification")
        void testRemoveAll() {
            ObservableLongList list = new ObservableLongList(1L, 2L, 3L, 4L, 5L);
            int[] changeCount = {0};

            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    changeCount[0]++;
                }
            });

            List<Long> toRemove = List.of(2L, 4L);
            list.removeAll(toRemove);

            assertTrue(changeCount[0] > 0, "removeAll should trigger change notifications");
            assertEquals(3, list.size());
        }
    }

    @Nested
    @DisplayName("Change Listener with Boundary Values")
    class BoundaryValueListenerTests {

        @Test
        @DisplayName("Listener receives notifications for Long.MAX_VALUE operations")
        void testListenerWithMaxValue() {
            ObservableLongList list = new ObservableLongList();
            int[] changeCount = {0};

            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    changeCount[0]++;
                }
            });

            list.addLong(Long.MAX_VALUE);
            assertEquals(1, changeCount[0]);

            list.removeLong(Long.MAX_VALUE);
            assertEquals(2, changeCount[0]);
        }

        @Test
        @DisplayName("Listener receives notifications for Long.MIN_VALUE operations")
        void testListenerWithMinValue() {
            ObservableLongList list = new ObservableLongList();
            int[] changeCount = {0};

            list.addListener((ListChangeListener<Long>) change -> {
                while (change.next()) {
                    changeCount[0]++;
                }
            });

            list.addLong(Long.MIN_VALUE);
            assertEquals(1, changeCount[0]);

            list.set(0, 0L);
            assertTrue(changeCount[0] >= 2);
        }
    }
}
