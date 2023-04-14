package dev.ikm.komet.framework.panel.pattern;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservablePatternSnapshot;
import dev.ikm.komet.framework.observable.ObservablePatternVersion;
import dev.ikm.komet.framework.panel.ComponentIsFinalPanel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.terms.EntityFacade;

public class PatternPanel extends ComponentIsFinalPanel<
        ObservablePatternSnapshot,
        ObservablePattern,
        ObservablePatternVersion,
        PatternVersionRecord> {

    public PatternPanel(ObservablePatternSnapshot patternSnapshot, ViewProperties viewProperties,
                        SimpleObjectProperty<EntityFacade> topEnclosingComponentProperty,
                        ObservableSet<Integer> referencedNids) {
        super(patternSnapshot, viewProperties, topEnclosingComponentProperty, referencedNids);
        this.collapsiblePane.setText("Pattern");
        this.getComponentDetailPane().pseudoClassStateChanged(PseudoClasses.PATTERN_PSEUDO_CLASS, true);
        this.getComponentPanelBox().pseudoClassStateChanged(PseudoClasses.PATTERN_PSEUDO_CLASS, true);
    }
}
