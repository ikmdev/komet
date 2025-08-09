package dev.ikm.komet.kview.events.genediting;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.komet.framework.observable.ObservableField;

import java.util.List;

public class GenEditingEvent extends Evt {

    public static final EvtType<GenEditingEvent> PUBLISH = new EvtType<>(Evt.ANY, "GEN_EDIT_PUBLISH_SEMANTIC");

    public static final EvtType<GenEditingEvent> CONFIRM_REFERENCE_COMPONENT = new EvtType<>(Evt.ANY, "CONFIRM_REFERENCE_COMPONENT");

    public static final EvtType<GenEditingEvent> VERSION_UPDATED = new EvtType<>(Evt.ANY, "VERSION_UPDATED");

    private List<Object> list;

    private int nid;

    public GenEditingEvent(Object source,EvtType eventType, List<Object> list, int nid){
        super(source,eventType);
        this.list = list;
        this.nid = nid;
    }

    public GenEditingEvent(Object source, EvtType<GenEditingEvent> eventType) {
        super(source,eventType);
    }

    public List<?> getList(){
        return list;
    }

    public int getNid() {
        return nid;
    }
}
