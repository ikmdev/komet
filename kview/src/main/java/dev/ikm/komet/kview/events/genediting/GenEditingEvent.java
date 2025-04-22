package dev.ikm.komet.kview.events.genediting;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.observable.ObservableField;

import java.util.List;

public class GenEditingEvent extends Evt {

    public static final EvtType<GenEditingEvent> PUBLISH = new EvtType<>(Evt.ANY, "GEN_EDIT_PUBLISH_SEMANTIC");

    public static final EvtType<GenEditingEvent> VERSION_UPDATED = new EvtType<>(Evt.ANY, "VERSION_UPDATED");

    private List<ObservableField<?>> list;

    private int nid;

    public GenEditingEvent(Object source,EvtType eventType, List<ObservableField<?>> list, int nid){
        super(source,eventType);
        this.list = list;
        this.nid = nid;
    }

    public List<ObservableField<?>> getList(){
        return list;
    }

    public int getNid() {
        return nid;
    }
}
