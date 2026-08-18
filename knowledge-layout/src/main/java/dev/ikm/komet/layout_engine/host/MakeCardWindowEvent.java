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

/**
 * A journal-topic gesture asking the hosting journal to <b>focus or create</b> a
 * provider-contributed card window ({@code IKE-Network/ike-issues#1044}): if a card window of
 * the named {@link KlCardProvider} is already open in the journal's workspace it comes to the
 * front; otherwise the journal creates one — one editing surface, never a parallel window. A
 * card publishes this on its journal topic (the {@code Make*WindowEvent} idiom: gestures ride
 * the topic bus; observable state rides properties), so cross-module dependencies stay
 * inverted — the publisher never sees the journal controller.
 */
public class MakeCardWindowEvent extends Evt {

    /** Focus the provider's open card window, or create one when none is open. */
    public static final EvtType<MakeCardWindowEvent> FOCUS_OR_CREATE =
            new EvtType<>(Evt.ANY, "FOCUS_OR_CREATE_CARD");

    private final String providerClassName;

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
        super(source, eventType);
        this.providerClassName = providerClassName;
    }

    /**
     * The provider class name identifying the card kind.
     *
     * @return the {@link KlCardProvider} implementation class name
     */
    public String getProviderClassName() {
        return providerClassName;
    }
}
