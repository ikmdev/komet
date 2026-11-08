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
package dev.ikm.komet.kview.klwindows.test;

import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.CardKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.CardBlueprint;
import dev.ikm.komet.layout_engine.host.AbstractHostCard;
import dev.ikm.komet.layout_engine.host.KlCardProvider;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import javafx.application.Platform;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * A card closed on a journal must not stay reachable from that journal.
 *
 * <p>Found 2026-11-07 in Komet's heap: every closed card, with its scene graph, its
 * {@code RichTextArea}s and its content, stayed reachable until the journal closed. Two gaps, both
 * exercised here through the journal's own close path ({@link CardKlWindow#closeTile()}):</p>
 * <ol>
 *   <li>Closing a card tile saved it but never unbound the card (ike-issues#1047 made close
 *       "not deletion", and the unbind went with the deletion).</li>
 *   <li>Unbinding left the card's view registered with the journal's view: a card's coordinate is a
 *       child override of the journal's ({@code ViewContext.createChildOf}), which adds three
 *       listeners to the journal's view that nothing removed. Through what subscribes to the card's
 *       view (the toolbar's coordinate indicator, as {@code ToolCard} wires it) the journal's view
 *       then holds the whole card.</li>
 * </ol>
 */
class ClosedCardReleaseTest {

    private static final File STARTER_DATA = new File("target/data", "ike-starter-set-reasoned-pb.zip");

    private static boolean fxReady;

    @BeforeAll
    static void start() throws Exception {
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT,
                Files.createTempDirectory("closed-card-release-test").toFile());
        PrimitiveData.selectControllerByName("Load Memory-Only 64-bit Store");
        PrimitiveData.start();
        // A view coordinate resolves its language and stamp concepts, so the store needs the starter set.
        assertTrue(STARTER_DATA.exists(), "Starter data must be at " + STARTER_DATA.getAbsolutePath());
        assertTrue(new LoadEntitiesFromProtobufFile(STARTER_DATA).compute().getTotalCount() > 0);
        selectHeadlessFx();
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            fxReady = latch.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            fxReady = true;
        } catch (Throwable cannotStart) {
            fxReady = false;
        }
    }

    @AfterAll
    static void stopStore() {
        PrimitiveData.stop();
    }

    @Test
    void aClosedCardTileIsNotHeldByTheJournalView() throws Exception {
        KometPreferences root = KometPreferencesImpl.getConfigurationRootPreferences()
                .node("test-closed-card-release");
        try {
            // The journal's view properties: an override of the journal's base view, as the journal makes them.
            ObservableViewNoOverride journalBase =
                    new ObservableViewNoOverride(Coordinates.View.DefaultView(), "journal view");
            ViewProperties journalViewProperties = journalBase.makeOverridableViewProperties("journal");

            Released released = openAndClose(root.node("card-window"), journalViewProperties);

            awaitCollected(released.card(), released.cardView());
            assertNull(released.cardView().get(),
                    "the closed card's view is still reachable from the journal's view");
            assertNull(released.card().get(), "the closed card is still reachable from the journal's view");
            Reference.reachabilityFence(journalViewProperties);
            Reference.reachabilityFence(journalBase);
        } finally {
            try {
                root.removeNode();
                root.flush();
            } catch (Exception ignored) {
                // best effort
            }
        }
    }

    @Test
    void closingATileUnbindsItAsAClose() throws Exception {
        KometPreferences root = KometPreferencesImpl.getConfigurationRootPreferences()
                .node("test-closed-card-unbind-on-close");
        try {
            ObservableViewNoOverride journalBase =
                    new ObservableViewNoOverride(Coordinates.View.DefaultView(), "journal view");
            ViewProperties journalViewProperties = journalBase.makeOverridableViewProperties("journal");
            KometPreferences windowNode = root.node("card-window");
            windowNode.put(EntityKlWindowState.WINDOW_ID, UUID.randomUUID().toString());
            windowNode.put(EntityKlWindowState.WINDOW_TYPE, CardKlWindow.CARD_WINDOW_TYPE.toString());
            CardKlWindow window = onFx(() -> CardKlWindow.create(UUID.randomUUID(), new TestCardProvider(),
                    journalViewProperties, windowNode));
            onFx(() -> {
                window.onShown();
                return null;
            });
            onFx(() -> {
                window.closeTile();
                return null;
            });
            // A close keeps the tile, so a card may let work in flight finish (the Assistant's reply) before it
            // unbinds; deletion does not wait. The close must take the close path.
            assertTrue(((TestCard) window.card()).unboundOnClose,
                    "closing a tile did not unbind its card as a close");
        } finally {
            try {
                root.removeNode();
                root.flush();
            } catch (Exception ignored) {
                // best effort
            }
        }
    }

    @Test
    void aClosedTileKeepsItsPreferencesWhenItsPaneLeavesTheWorkspace() throws Exception {
        KometPreferences root = KometPreferencesImpl.getConfigurationRootPreferences()
                .node("test-closed-card-keeps-preferences");
        try {
            ObservableViewNoOverride journalBase =
                    new ObservableViewNoOverride(Coordinates.View.DefaultView(), "journal view");
            ViewProperties journalViewProperties = journalBase.makeOverridableViewProperties("journal");
            KometPreferences windowNode = root.node("card-window");
            windowNode.put(EntityKlWindowState.WINDOW_ID, UUID.randomUUID().toString());
            windowNode.put(EntityKlWindowState.WINDOW_TYPE, CardKlWindow.CARD_WINDOW_TYPE.toString());
            CardKlWindow window = onFx(() -> CardKlWindow.create(UUID.randomUUID(), new TestCardProvider(),
                    journalViewProperties, windowNode));
            StackPane workspace = new StackPane();
            onFx(() -> {
                workspace.getChildren().add(window.fxObject());
                window.onShown();
                return null;
            });
            KometPreferences cardNode = onFx(() -> window.card().preferences());
            onFx(() -> {
                // The journal's close handler: close the tile, then take it off the workspace.
                window.closeTile();
                workspace.getChildren().remove(window.fxObject());
                return null;
            });
            // Close is not deletion (ike-issues#1047): the card's node, and what it stores, stays for a reopen.
            assertTrue(cardNode.nodeExists(""), "closing the tile removed its card's preferences node");
            assertTrue(cardNode.keys().length > 0, "closing the tile cleared its card's preferences");
        } finally {
            try {
                root.removeNode();
                root.flush();
            } catch (Exception ignored) {
                // best effort
            }
        }
    }

    /**
     * Opens a card tile on the journal, as the journal's Tiles menu does, and closes it, as its close
     * control does. Returns only weak references, so nothing in the test's own frame holds the card.
     */
    private static Released openAndClose(KometPreferences windowNode, ViewProperties journalViewProperties)
            throws InterruptedException {
        // What the journal's window-preferences helper writes for a new tile.
        windowNode.put(EntityKlWindowState.WINDOW_ID, UUID.randomUUID().toString());
        windowNode.put(EntityKlWindowState.WINDOW_TYPE, CardKlWindow.CARD_WINDOW_TYPE.toString());
        CardKlWindow window = onFx(() -> CardKlWindow.create(UUID.randomUUID(), new TestCardProvider(),
                journalViewProperties, windowNode));
        onFx(() -> {
            window.onShown();   // binds the card; its realize runs on a later FX turn
            return null;
        });
        Released released = onFx(() -> new Released(new WeakReference<>(window.card()),
                new WeakReference<>(window.card().getCardViewProperties().nodeView())));
        onFx(() -> {
            window.closeTile();
            return null;
        });
        return released;
    }

    private record Released(WeakReference<AbstractHostCard> card, WeakReference<ObservableView> cardView) {
    }

    private static void awaitCollected(WeakReference<?>... references) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            boolean live = false;
            for (WeakReference<?> reference : references) {
                live |= reference.get() != null;
            }
            if (!live) {
                return;
            }
            System.gc();
            Thread.sleep(50);
        }
    }

    private static <T> T onFx(Supplier<T> body) throws InterruptedException {
        assumeTrue(fxReady, "JavaFX toolkit unavailable");
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(body.get());
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX task did not complete");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
        return result.get();
    }

    /** Monocle's headless platform when it is on the class path, as the other headless FX tests use. */
    private static void selectHeadlessFx() {
        try {
            Class.forName("com.sun.glass.ui.monocle.MonoclePlatformFactory");
        } catch (ClassNotFoundException noMonocle) {
            return;
        }
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    /** Contributes {@link TestCard}s, as a plugin's {@link KlCardProvider} does. */
    static final class TestCardProvider implements KlCardProvider {

        @Override
        public String cardName() {
            return "Test card";
        }

        @Override
        public AbstractHostCard createCard(KometPreferences windowPreferences, UUID journalTopic) {
            return new TestCard.Factory().create(KlPreferencesFactory.create(windowPreferences, TestCard.class));
        }

        @Override
        public AbstractHostCard restoreCard(KometPreferences windowPreferences) {
            throw new UnsupportedOperationException("not restored in this test");
        }
    }

    /** The smallest host card whose toolbar observes the card's view, as {@code ToolCard}'s does. */
    static final class TestCard extends AbstractHostCard {

        /** Set when the card is unbound as a close rather than a deletion. */
        boolean unboundOnClose;

        private TestCard(KometPreferences preferences) {
            super(preferences);
        }

        private TestCard(KlPreferencesFactory preferencesFactory, Factory factory) {
            super(preferencesFactory, factory);
        }

        @Override
        protected String cardTitle() {
            return "Test card";
        }

        @Override
        public void knowledgeLayoutUnbindOnClose() {
            unboundOnClose = true;
            super.knowledgeLayoutUnbindOnClose();
        }

        @Override
        protected void renderContent() {
            // No content: the toolbar's subscription is enough to tie the card to its view.
        }

        @Override
        protected void buildToolbarControls(HBox toolBar) {
            MenuButton coordinateButton = new MenuButton();
            wireCoordinateOverrideIndicator(coordinateButton);
            toolBar.getChildren().add(coordinateButton);
        }

        static final class Factory implements CardBlueprint.Factory<TestCard> {

            @Override
            public TestCard restore(KometPreferences preferences) {
                return new TestCard(preferences);
            }

            @Override
            public TestCard create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
                TestCard card = new TestCard(preferencesFactory, this);
                card.setAreaLayout(areaGridSettings.with(this.getClass()));
                return card;
            }
        }
    }
}
