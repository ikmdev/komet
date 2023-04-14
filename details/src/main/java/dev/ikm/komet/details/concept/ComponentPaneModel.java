package dev.ikm.komet.details.concept;

import javafx.scene.Node;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ComponentPaneModel extends BadgedVersionPaneModel {
    public ComponentPaneModel(ViewProperties viewProperties,
                              ObservableEntitySnapshot observableEntitySnapshot,
                              List<ConceptFacade> semanticOrderForChronology,
                              MutableIntIntMap stampOrderHashMap,
                              HashMap<String, AtomicBoolean> disclosureStateMap) {
        super();
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getBadgedPane() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void doExpandAllAction(ExpandAction newValue) {
        throw new UnsupportedOperationException();
    }
}
