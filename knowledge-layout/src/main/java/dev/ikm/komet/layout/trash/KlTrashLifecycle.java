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

import dev.ikm.tinkar.common.service.LifecyclePhase;
import dev.ikm.tinkar.common.service.ServiceLifecycle;
import dev.ikm.tinkar.common.service.ServiceLifecyclePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Empties Komet's Trash as Komet quits, when the person keeps that preference on. An application
 * service shuts down before the data store does, so what a provider deletes from the knowledge
 * base, such as a conversation's journal, goes while the store is still open.
 */
@LifecyclePhase(value = ServiceLifecyclePhase.APPLICATION_SERVICES, subPriority = 50)
public class KlTrashLifecycle implements ServiceLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(KlTrashLifecycle.class);

    /** Creates the service; called by the service loader. */
    public KlTrashLifecycle() {
    }

    @Override
    public void startup() {
        // Nothing to start: the Trash is read when the journal or the quit asks for it.
    }

    @Override
    public void shutdown() {
        try {
            if (!KlTrash.emptyOnQuit()) {
                return;
            }
            KlTrash.Deleted emptied = KlTrash.empty();
            if (emptied.deleted() > 0 || !emptied.problems().isEmpty()) {
                LOG.info("Emptied the Trash as Komet quit: {} item(s) deleted", emptied.deleted());
            }
            for (String problem : emptied.problems()) {
                LOG.warn("Left in the Trash: {}", problem);
            }
        } catch (RuntimeException e) {
            LOG.error("Could not empty the Trash as Komet quit; it is emptied the next time", e);
        }
    }
}
