package dev.ikm.tinkar.entity;

import dev.ikm.tinkar.common.service.DataActivity;

/**
 * ChangeSetWriterService ensures durability and exchangeability every time a new entity version is written
 * and a later STAMP entity for that version is written.
 */
public interface ChangeSetWriterService {
    /**
     * Writes the given entity to the change set.
     *
     * @param entity       The entity instance for which a new version is written.
     * @param dataActivity The type of data activity being performed, which helps determine
     *                     if the entity should be written to the change set (e.g., initialization,
     *                     loading change set, synchronizable edit, or local edit), based
     *                     on the service's policies.
     */
    void writeToChangeSet(Entity entity, DataActivity dataActivity);

    /**
     * Shuts down the ChangeSetWriterService, ensuring any necessary cleanup
     * or finalization steps are performed.
     */
    void shutdown();

}
