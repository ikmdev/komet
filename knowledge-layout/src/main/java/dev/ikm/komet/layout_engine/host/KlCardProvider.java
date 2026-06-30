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
package dev.ikm.komet.layout_engine.host;

import dev.ikm.komet.preferences.KometPreferences;

import java.util.UUID;

/**
 * Discovery SPI for contributing a first-class {@link AbstractHostCard} to the Journal workspace.
 *
 * <p>A plugin provides one via {@code ServiceLoader} (a {@code provides KlCardProvider with …} clause); the
 * journal enumerates the providers, surfaces each on the workspace "+" menu, creates the card on selection,
 * and dispatches restoration back to the same provider on reopen. The provider owns its card's preferences-node
 * convention: {@link #createCard} allocates the card's node under the supplied window node and returns a ready
 * card shell; {@link #restoreCard} reconstructs it from that same node. The generic {@code CardKlWindow} adapter
 * hosts whatever {@code AbstractHostCard} the provider returns — so a plugin contributes a real card (its own
 * chrome, context, lifecycle, and sandboxed prefs-node storage) rather than a panel hosted inside a generic shell.
 *
 * <p>This is the card-tier analogue of {@code KlToolArea.Factory}: where a tool area is hosted <em>inside</em> a
 * generic {@code ToolCard}, a {@code KlCardProvider} contributes the card itself.
 */
public interface KlCardProvider {

    /**
     * Human-readable label for the workspace "+" menu entry that creates this card.
     *
     * @return the menu label, for example {@code "Claude Assistant"}
     */
    String cardName();

    /**
     * Creates a fresh card shell to add to the workspace. The provider allocates the card's preferences node
     * under {@code windowPreferences} (so the card's per-instance sandboxed storage lives there) and injects
     * its inputs; realization is deferred to the card's {@code knowledgeLayoutBind()} by the host window.
     *
     * @param windowPreferences the hosting window's preferences node
     * @param journalTopic      the journal topic for event coordination
     * @return the created card shell (realized later, at bind)
     */
    AbstractHostCard createCard(KometPreferences windowPreferences, UUID journalTopic);

    /**
     * Restores a card from its own preferences node under {@code windowPreferences} (the node {@link #createCard}
     * allocated), via the framework restore. The host window then re-binds it.
     *
     * @param windowPreferences the hosting window's saved preferences node
     * @return the restored card
     */
    AbstractHostCard restoreCard(KometPreferences windowPreferences);
}
