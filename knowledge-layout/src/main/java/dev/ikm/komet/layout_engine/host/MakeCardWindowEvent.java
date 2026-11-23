/*
 * Copyright © 2026 Knowledge Graphlet / IKE Network
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.layout_engine.host;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import javafx.scene.Node;

import java.util.function.Consumer;

/**
 * A journal-topic gesture asking the hosting journal to <b>focus or create</b> a
 * provider-contributed card window ({@code IKE-Network/ike-issues#1044}): if a card window of
 * the named {@link KlCardProvider} is already open in the journal's workspace it comes to the
 * front; otherwise the journal creates one — one editing surface, never a parallel window. A
 * card publishes this on its journal topic (the {@code Make*WindowEvent} idiom: gestures ride
 * the topic bus; observable state rides properties), so cross-module dependencies stay
 * inverted — the publisher never sees the journal controller.
 *
 * <p>{@link #CREATE_NEW} always makes a new card window, as a card does that opens one document
 * or subject among several: it may name the node to open beside and an authored size, and it hands
 * the new card back to the publisher once the card is on the workspace.
 *
 * <p>{@link #FOCUS_OR_REOPEN} names one tile rather than a kind of card: its window comes to the
 * front, or the closed tile reopens with its content, so content kept in a closed tile is never
 * left behind for a new, empty one.
 */
public class MakeCardWindowEvent extends Evt {

    /** Focus the provider's open card window, or create one when none is open. */
    public static final EvtType<MakeCardWindowEvent> FOCUS_OR_CREATE =
            new EvtType<>(Evt.ANY, "FOCUS_OR_CREATE_CARD");

    /** Create a new card window of the provider, whether or not one is open. */
    public static final EvtType<MakeCardWindowEvent> CREATE_NEW =
            new EvtType<>(Evt.ANY, "CREATE_NEW_CARD");

    /**
     * Front the card window of one tile, the journal's {@code journal_card_…} preferences node the
     * event names, or reopen that tile from its preferences when its window is closed, with all its
     * content back: what the journal's Tiles menu does with Show and Reopen. Made with
     * {@link #focusOrReopen}.
     */
    public static final EvtType<MakeCardWindowEvent> FOCUS_OR_REOPEN =
            new EvtType<>(Evt.ANY, "FOCUS_OR_REOPEN_CARD");

    private final String providerClassName;
    private final String tile;
    private final Node near;
    private final double authoredWidth;
    private final double authoredHeight;
    private final Consumer<AbstractHostCard> onCreated;

    /**
     * Constructs the gesture.
     *
     * @param source            the publishing object
     * @param eventType         {@link #FOCUS_OR_CREATE}
     * @param providerClassName the {@link KlCardProvider} implementation class name identifying
     *                          the card kind — stable across renames of the display name
     */
    public MakeCardWindowEvent(Object source, EvtType<MakeCardWindowEvent> eventType,
                               String providerClassName) {
        this(source, eventType, providerClassName, null, -1, -1, null);
    }

    /**
     * Constructs the gesture with a placement.
     *
     * @param source            the publishing object
     * @param eventType         {@link #FOCUS_OR_CREATE} or {@link #CREATE_NEW}
     * @param providerClassName the {@link KlCardProvider} implementation class name
     * @param near              the node the window opens beside, or {@code null} to place it as usual
     * @param authoredWidth     the window's width in points, or a negative number for the workspace's
     * @param authoredHeight    the window's height in points, or a negative number for the workspace's
     * @param onCreated         receives a card this gesture created, once it is on the workspace; may be
     *                          {@code null}, and is not called when an open window is fronted
     */
    public MakeCardWindowEvent(Object source, EvtType<MakeCardWindowEvent> eventType, String providerClassName,
                               Node near, double authoredWidth, double authoredHeight,
                               Consumer<AbstractHostCard> onCreated) {
        this(source, eventType, providerClassName, "", near, authoredWidth, authoredHeight, onCreated);
    }

    private MakeCardWindowEvent(Object source, EvtType<MakeCardWindowEvent> eventType, String providerClassName,
                                String tile, Node near, double authoredWidth, double authoredHeight,
                                Consumer<AbstractHostCard> onCreated) {
        super(source, eventType);
        this.providerClassName = providerClassName;
        this.tile = tile;
        this.near = near;
        this.authoredWidth = authoredWidth;
        this.authoredHeight = authoredHeight;
        this.onCreated = onCreated;
    }

    /**
     * The gesture that fronts one tile's card window, or reopens the tile when its window is closed.
     *
     * @param source    the publishing object
     * @param tile      the name of the tile's preferences node, a child of the journal's node
     * @param onShown   receives the tile's card once its window is on the workspace, fronted or
     *                  reopened; may be {@code null}
     * @return the gesture, to publish on the journal's topic
     */
    public static MakeCardWindowEvent focusOrReopen(Object source, String tile, Consumer<AbstractHostCard> onShown) {
        return new MakeCardWindowEvent(source, FOCUS_OR_REOPEN, "", tile, null, -1, -1, onShown);
    }

    /**
     * The provider class name identifying the card kind.
     *
     * @return the {@link KlCardProvider} implementation class name, empty for {@link #FOCUS_OR_REOPEN}
     */
    public String getProviderClassName() {
        return providerClassName;
    }

    /**
     * The tile a {@link #FOCUS_OR_REOPEN} names.
     *
     * @return the name of the tile's preferences node, empty for the other gestures
     */
    public String getTile() {
        return tile;
    }

    /**
     * The node a created window opens beside.
     *
     * @return the node, or {@code null} to place the window as usual
     */
    public Node getNear() {
        return near;
    }

    /**
     * The created window's authored width.
     *
     * @return the width in points, negative for the workspace's
     */
    public double getAuthoredWidth() {
        return authoredWidth;
    }

    /**
     * The created window's authored height.
     *
     * @return the height in points, negative for the workspace's
     */
    public double getAuthoredHeight() {
        return authoredHeight;
    }

    /**
     * What receives a created card, or for {@link #FOCUS_OR_REOPEN} the tile's card once it is shown.
     *
     * @return the consumer, or {@code null}
     */
    public Consumer<AbstractHostCard> getOnCreated() {
        return onCreated;
    }
}
