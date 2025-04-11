package dev.ikm.komet.framework.events;

import dev.ikm.tinkar.entity.EntityVersion;

public class EntityVersionChangeEvent extends Evt {

    public static final EvtType<EntityVersionChangeEvent> VERSION_UPDATED = new EvtType<>(Evt.ANY, "VERSION_UPDATED");

    private final EntityVersion entityVersion;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public <V extends EntityVersion> EntityVersionChangeEvent(Object source, EvtType eventType, EntityVersion entityVersion) {
        super(source, eventType);

        this.entityVersion = entityVersion;
    }

    public EntityVersion getEntityVersion() {
        return entityVersion;
    }
}
