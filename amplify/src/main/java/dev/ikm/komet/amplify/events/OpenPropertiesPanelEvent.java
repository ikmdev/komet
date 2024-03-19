package dev.ikm.komet.amplify.events;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.tinkar.common.id.PublicId;
import javafx.scene.control.ToggleButton;

public class OpenPropertiesPanelEvent extends Evt {

    public static final EvtType<OpenPropertiesPanelEvent> OPEN_PROPERTIES_PANEL = new EvtType<>(Evt.ANY, "OPEN_PROPERTIES_PANEL");

    private PublicId fqnPublicId;

    private PublicId otherNamePublicId;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     */
    public OpenPropertiesPanelEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }

    public OpenPropertiesPanelEvent(Object source, EvtType eventType, PublicId fqnPublicId, PublicId otherNamePublicId) {
        super(source, eventType);
        this.fqnPublicId = fqnPublicId;
        this.otherNamePublicId = otherNamePublicId;
    }

    public PublicId getFqnPublicId() {
        return fqnPublicId;
    }

    public PublicId getOtherNamePublicId() {
        return otherNamePublicId;
    }
}
