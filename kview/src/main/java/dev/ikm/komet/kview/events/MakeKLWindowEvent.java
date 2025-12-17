package dev.ikm.komet.kview.events;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.terms.EntityFacade;

public class MakeKLWindowEvent extends Evt {

    public static final EvtType<MakeConceptWindowEvent> OPEN_ENTITY_COMPONENT = new EvtType<>(Evt.ANY, "OPEN_ENTITY_COMPONENT");

    public static final EvtType<MakeConceptWindowEvent> OPEN_ENTITY_FROM_ENTITY = new EvtType<>(Evt.ANY, "OPEN_ENTITY_FROM_ENTITY");

    private final EntityFacade entityFacade;

    private final String windowTitle;

    /**
     * Constructs a prototypical Event.
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType
     * @param entityFacade
     * @throws IllegalArgumentException if source is null
     */
    public MakeKLWindowEvent(Object source, EvtType eventType, EntityFacade entityFacade, String windowTitle) {
        super(source, eventType);
        this.entityFacade = entityFacade;
        this.windowTitle = windowTitle;
    }

    public EntityFacade getEntityFacade() { return entityFacade; }

    public String getWindowTitle() { return windowTitle; }
}