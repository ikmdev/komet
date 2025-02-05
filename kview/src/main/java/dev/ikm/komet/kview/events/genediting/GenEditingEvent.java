package dev.ikm.komet.kview.events.genediting;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.observable.ObservableField;

import java.util.List;

public class GenEditingEvent extends Evt {

    public static final EvtType<GenEditingEvent> PUBLISH = new EvtType<>(Evt.ANY, "GEN_EDIT_PUBLISH_SEMANTIC");

    private List<ObservableField<?>> list;

    public GenEditingEvent(Object source,EvtType eventType, List<ObservableField<?>> list){
        super(source,eventType);
        this.list = list;
    }

    public List<ObservableField<?>> getList(){
        return list;
    }
}
