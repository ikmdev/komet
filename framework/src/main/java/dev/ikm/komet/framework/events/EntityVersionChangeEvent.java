package dev.ikm.komet.framework.events;

import dev.ikm.tinkar.entity.EntityVersion;
import org.eclipse.collections.api.list.ImmutableList;

public class EntityVersionChangeEvent extends Evt {

    public static final EvtType<EntityVersionChangeEvent> VERSION_UPDATED = new EvtType<>(Evt.ANY, "VERSION_UPDATED");

    private int nid;
    private ImmutableList<Object> observableSemanticVersion;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public <V extends EntityVersion> EntityVersionChangeEvent(Object source, EvtType eventType, int nid, ImmutableList<Object> observableSemanticVersion) {
        super(source, eventType);
        this.nid = nid;
        this.observableSemanticVersion = observableSemanticVersion;
    }

    public int getNid() {
        return nid;
    }

    public  ImmutableList<Object> getObservableSemanticVersion() {
        return observableSemanticVersion;
    }
}
