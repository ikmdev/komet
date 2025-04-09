package dev.ikm.komet.framework.events;

public class EntityVersionChangeEvent extends Evt {

    public static final EvtType<EntityVersionChangeEvent> VERSION_UPDATED = new EvtType<>(Evt.ANY, "VERSION_UPDATED");

    private int nid;
    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public EntityVersionChangeEvent(Object source, EvtType eventType,  int nid) {
        super(source, eventType);
        this.nid = nid;
    }

    public int getNid() {
        return nid;
    }

}
