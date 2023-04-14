package dev.ikm.komet.framework.panel;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.framework.panel.concept.ConceptVersionPanel;
import dev.ikm.komet.framework.panel.pattern.PatternVersionPanel;
import dev.ikm.komet.framework.panel.semantic.SemanticVersionPanel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.Optional;

import static dev.ikm.komet.framework.StyleClasses.COMPONENT_COLLAPSIBLE_PANEL;

/**
 * @param <ES>
 * @param <OE>
 * @param <OV>
 * @param <EV>
 */
public class ComponentIsFinalPanel<ES extends ObservableEntitySnapshot<OE, OV, EV>,
        OE extends ObservableEntity<OV, EV>,
        OV extends ObservableVersion<EV>,
        EV extends EntityVersion> extends ComponentPanelAbstract {
    protected final TitledPane collapsiblePane = new TitledPane("Component", componentDetailPane);
    private final ES component;

    {
        collapsiblePane.getStyleClass().add(COMPONENT_COLLAPSIBLE_PANEL.toString());
    }

    public ComponentIsFinalPanel(ES component, ViewProperties viewProperties,
                                 SimpleObjectProperty<EntityFacade> topEnclosingComponentProperty,
                                 ObservableSet<Integer> referencedNids) {
        super(viewProperties, referencedNids);
        if (component == null) {
            throw new NullPointerException();
        }
        this.component = component;
        this.collapsiblePane.setContentDisplay(ContentDisplay.LEFT);
        Platform.runLater(() -> referencedNids.add(component.nid()));
        // TODO finish good identicon graphic.
        // this.collapsiblePane.setGraphic(Identicon.generateIdenticon(component.publicId(), 24, 24));
        TinkExecutor.threadPool().execute(() -> {
            Latest<OV> latestComponent = component.getLatestVersion();
            latestComponent.ifPresent(latestVersion -> {
                addVersionPanel(latestVersion, true);
            });
            for (OV contradictedVersion : latestComponent.contradictions()) {
                addVersionPanel(contradictedVersion, true);
            }
            for (OV historicVersion : component.getHistoricVersions()) {
                addVersionPanel(historicVersion, false);
            }
            addSemanticReferences(component, topEnclosingComponentProperty);
        });
    }

    private void addVersionPanel(OV version, boolean expanded) {
        ComponentVersionIsFinalPanel<OV> versionPanel = makeVersionPanel(version);
        BorderPane.setAlignment(versionPanel.versionDetailsPane, Pos.TOP_LEFT);
        versionPanel.collapsiblePane.setExpanded(expanded);
        Platform.runLater(() -> ComponentIsFinalPanel.this.componentPanelBox.getChildren().add(versionPanel.getVersionDetailsPane()));
    }

    private ComponentVersionIsFinalPanel makeVersionPanel(OV version) {
        if (version instanceof ObservableSemanticVersion semanticVersion) {
            return new SemanticVersionPanel(semanticVersion, viewProperties);
        } else if (version instanceof ObservableConceptVersion conceptVersion) {
            return new ConceptVersionPanel(conceptVersion, viewProperties);
        } else if (version instanceof ObservablePatternVersion patternVersion) {
            return new PatternVersionPanel(patternVersion, viewProperties);
        }
        throw new UnsupportedOperationException("Can't handle version type: " + version.toString());
    }

    @Override
    public final Optional<ES> getComponent() {
        return Optional.of(component);
    }

    public Node getComponentDetailPane() {
        return collapsiblePane;
    }

}
