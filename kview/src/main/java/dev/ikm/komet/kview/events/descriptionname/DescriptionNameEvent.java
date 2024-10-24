package dev.ikm.komet.kview.events.descriptionname;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;

public class DescriptionNameEvent extends Evt {

    public static final EvtType<DescriptionNameEvent> DESCRIPTION_NAME = new EvtType<>(Evt.ANY, "DESCRIPTION_NAME");
    public static final EvtType<DescriptionNameEvent> SHOW_ADD_FQN = new EvtType<>(DESCRIPTION_NAME, "SHOW_ADD_FQN");
    public static final EvtType<DescriptionNameEvent> SHOW_ADD_OTHER_NAME = new EvtType<>(DESCRIPTION_NAME, "SHOW_ADD_OTHER_NAME_DESCRIPTION");

    public static final EvtType<DescriptionNameEvent> SHOW_EDIT_FQN = new EvtType<>(DESCRIPTION_NAME, "SHOW_EDIT_FQN_DESCRIPTION");
    public static final EvtType<DescriptionNameEvent> SHOW_EDIT_OTHER_NAME = new EvtType<>(DESCRIPTION_NAME, "SHOW_EDIT_OTHER_NAME_DESCRIPTION");

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public DescriptionNameEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}
