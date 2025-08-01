package dev.ikm.komet.kview.events.pattern;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.EntityFacade;

public class MakePatternWindowEvent extends Evt {

    public static final EvtType<MakePatternWindowEvent> OPEN_PATTERN = new EvtType<>(Evt.ANY, "OPEN_PATTERN");

    private EntityFacade patternFacade;


    private ViewProperties viewProperties;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public MakePatternWindowEvent(Object source, EvtType eventType, EntityFacade patternFacade, ViewProperties viewProperties) {
        super(source, eventType);
        this.viewProperties = viewProperties;
        this.patternFacade = patternFacade;
    }

    public EntityFacade getPatternFacade() {
        return patternFacade;
    }

    public ViewProperties getViewProperties() {
        return viewProperties;
    }

}
