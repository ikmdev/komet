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
package dev.ikm.komet.layout.trash;

import java.util.List;

/**
 * A source of things in Komet's Trash, discovered as a service (a {@code provides KlTrashProvider
 * with …} clause): a plugin whose cards move what a person deletes to the Trash, rather than
 * deleting it at once, answers here for those items, so the journal's one Trash lists, restores
 * and deletes them all.
 */
public interface KlTrashProvider {

    /** @return the provider's name, as the Trash labels its items */
    String sourceName();

    /** @return the provider's items in the Trash */
    List<KlTrashItem> items();

    /**
     * Takes items out of the Trash and returns each to where it was.
     *
     * @param items items this provider gave
     */
    void putBack(List<KlTrashItem> items);

    /**
     * Deletes items for good. An item that cannot be deleted stays in the Trash and is named in
     * the result.
     *
     * @param items items this provider gave
     * @return what was done
     */
    KlTrash.Deleted delete(List<KlTrashItem> items);

    /**
     * Listens for any change to the provider's items.
     *
     * @param listener called after each change, on any thread
     */
    void onChange(Runnable listener);

    /**
     * Stops listening.
     *
     * @param listener a listener given to {@link #onChange}
     */
    void stopListening(Runnable listener);
}
