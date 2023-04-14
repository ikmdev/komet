package dev.ikm.komet.framework.panel.concept;

import javafx.scene.Node;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import dev.ikm.komet.framework.panel.ComponentVersionIsFinalPanel;
import dev.ikm.komet.framework.view.ViewProperties;

public class ConceptVersionPanel extends ComponentVersionIsFinalPanel<ObservableConceptVersion> {
    public ConceptVersionPanel(ObservableConceptVersion version, ViewProperties viewProperties) {
        super(version, viewProperties);
        collapsiblePane.setExpanded(false);
        collapsiblePane.setContent(null);
    }

    @Override
    protected Node makeCenterNode(ObservableConceptVersion version, ViewProperties viewProperties) {
        versionDetailsPane.pseudoClassStateChanged(PseudoClasses.UNCOMMITTED_PSEUDO_CLASS, version.uncommitted());
        return null;
    }
}