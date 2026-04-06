package dev.ikm.komet.kview.events.genpurpose;

import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

import java.util.List;

public class GenPurposeEvent extends Evt {

    public static final EvtType<GenPurposeEvent> PUBLISH = new EvtType<>(Evt.ANY, "GEN_PURPOSE_PUBLISH_SEMANTIC");

    public static final EvtType<GenPurposeEvent> VERSION_UPDATED = new EvtType<>(Evt.ANY, "VERSION_UPDATED");

    private List list;

    private SemanticEntity<SemanticEntityVersion> semantic;

    public GenPurposeEvent(Object source, EvtType eventType, List<Object> list, SemanticEntity<SemanticEntityVersion> semantic){
        super(source,eventType);
        this.list = list;
        this.semantic = semantic;
    }

    public GenPurposeEvent(Object source, EvtType<GenPurposeEvent> eventType) {
        super(source,eventType);
    }

    public List<?> getList(){
        return list;
    }

    public SemanticEntity<SemanticEntityVersion> getSemantic() {
        return semantic;
    }
}
