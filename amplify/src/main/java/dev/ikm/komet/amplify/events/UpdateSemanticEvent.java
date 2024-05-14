package dev.ikm.komet.amplify.events;

import dev.ikm.komet.amplify.om.DescrName;
import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;

public class UpdateSemanticEvent extends Evt {

    public static final EvtType<UpdateSemanticEvent> UPDATE_OTHER_NAME = new EvtType<>(Evt.ANY, "UPDATE_OTHER_NAME");
    
    private DescrName model;
    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public UpdateSemanticEvent(Object source, EvtType eventType, DescrName model) {
        super(source, eventType);
        this.model = model;
    }

    public DescrName getModel() {
        return model;
    }
}
