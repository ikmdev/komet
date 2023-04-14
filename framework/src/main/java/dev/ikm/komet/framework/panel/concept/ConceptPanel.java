package dev.ikm.komet.framework.panel.concept;


import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.observable.ObservableConcept;
import dev.ikm.komet.framework.observable.ObservableConceptSnapshot;
import dev.ikm.komet.framework.observable.ObservableConceptVersion;
import dev.ikm.komet.framework.panel.ComponentIsFinalPanel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.ConceptVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;

public class ConceptPanel extends ComponentIsFinalPanel<
        ObservableConceptSnapshot,
        ObservableConcept,
        ObservableConceptVersion,
        ConceptVersionRecord> {

    public ConceptPanel(ObservableConceptSnapshot conceptEntity,
                        ViewProperties viewProperties,
                        SimpleObjectProperty<EntityFacade> topEnclosingComponentProperty,
                        ObservableSet<Integer> referencedNids) {
        super(conceptEntity, viewProperties, topEnclosingComponentProperty, referencedNids);
        this.collapsiblePane.setText("Concept panel");
        this.getComponentPanelBox().pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, true);
        this.getComponentDetailPane().pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, true);
    }
}
