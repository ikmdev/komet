/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.layout_engine.component.area;

import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticSnapshot;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.KlAxiomArea;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout_engine.blueprint.SupplementalAreaBlueprint;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.logic.PremiseType;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Shared base for the swappable concept axiom-view supplemental areas (ike-issues#644/#639): it
 * implements the {@link KlAxiomArea} injection seam and the common "resolve the concept's axiom
 * semantic for the current premise, then render it" flow, leaving only the actual node-building to
 * subclasses via {@link #renderAxioms}.
 *
 * <p>The host window injects the focused concept ({@link #setFocusConcept}), the
 * {@link ViewProperties} ({@link #setAxiomViewProperties}) and the {@link PremiseType}
 * ({@link #setPremiseType}); the area then resolves the axiom semantic (the same
 * {@code ObservableSemantic.getAxiomSnapshot} path the inline popover uses) and mounts whatever
 * {@link #renderAxioms} returns. {@code ClassicAxiomArea} (wrapping the classic {@code AxiomView})
 * and {@code KonceptAxiomTreeArea} (the refreshed KonceptBadge tree) are the two implementations.
 */
public abstract class AbstractAxiomArea extends SupplementalAreaBlueprint implements KlAxiomArea {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAxiomArea.class);

    private final ObjectProperty<EntityFacade> focus = new SimpleObjectProperty<>(this, "focus");
    private ViewProperties injectedViewProperties;
    private PremiseType premiseType = PremiseType.INFERRED;
    private Subscription coordinateSubscription;

    {
        focus.addListener((obs, oldFocus, newFocus) -> rebuildLater());
    }

    /**
     * Restore constructor.
     *
     * @param preferences the preferences node backing this area
     */
    protected AbstractAxiomArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Create constructor.
     *
     * @param preferencesFactory factory for this area's preferences node
     * @param areaFactory        the factory creating this area
     */
    protected AbstractAxiomArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /**
     * Renders the resolved axiom semantic into a node to mount in the area. Implemented by each
     * renderer (classic {@code AxiomView} wrapper, refreshed KonceptBadge tree, …).
     *
     * @param axiomVersion   the concept's stated/inferred axiom semantic version
     * @param premiseType    the premise being rendered
     * @param viewProperties the view to render against
     * @return the node to display
     */
    protected abstract Node renderAxioms(ObservableSemanticVersion axiomVersion,
                                         PremiseType premiseType,
                                         ViewProperties viewProperties);

    @Override
    public final void setFocusConcept(EntityFacade concept) {
        focus.set(concept);
    }

    /**
     * Returns the concept currently rendered.
     *
     * @return the focused concept, or {@code null} if none
     */
    public final EntityFacade getFocusConcept() {
        return focus.get();
    }

    /**
     * The concept this area renders; a host may bind this to the surrounding layout's focus.
     *
     * @return the focus property
     */
    public final ObjectProperty<EntityFacade> focusProperty() {
        return focus;
    }

    @Override
    public final void setAxiomViewProperties(ViewProperties viewProperties) {
        if (coordinateSubscription != null) {
            coordinateSubscription.unsubscribe();
            coordinateSubscription = null;
        }
        this.injectedViewProperties = viewProperties;
        if (viewProperties != null) {
            // Subscribe to the view coordinate (an ObservableView) so the rendering stays responsive
            // to coordinate changes — language/dialect/description-type, status, premise, etc. The
            // calculator reflects the new coordinate, so the rebuild re-resolves names accordingly.
            coordinateSubscription = viewProperties.nodeView().subscribe(this::rebuildLater);
        }
        rebuildLater();
    }

    @Override
    public final void setPremiseType(PremiseType premiseType) {
        this.premiseType = (premiseType == null) ? PremiseType.INFERRED : premiseType;
        rebuildLater();
    }

    private ViewProperties resolveViewProperties() {
        if (injectedViewProperties != null) {
            return injectedViewProperties;
        }
        try {
            ViewCoordinateRecord viewRecord = context().viewCoordinate().toViewCoordinateRecord();
            String name = getClass().getSimpleName();
            return new ObservableViewNoOverride(viewRecord, name).makeOverridableViewProperties(name);
        } catch (RuntimeException e) {
            LOG.warn("Could not derive view properties from context", e);
            return null;
        }
    }

    private void rebuildLater() {
        if (Platform.isFxApplicationThread()) {
            rebuild();
        } else {
            Platform.runLater(this::rebuild);
        }
    }

    private void rebuild() {
        GridPane host = gridPaneForChildren();
        host.getChildren().clear();
        EntityFacade concept = focus.get();
        ViewProperties viewProperties = resolveViewProperties();
        if (concept == null || viewProperties == null) {
            host.add(new Label("No concept in focus."), 0, 0);
            return;
        }
        try {
            Optional<ObservableSemanticSnapshot> snapshot =
                    ObservableSemantic.getAxiomSnapshot(concept.nid(), premiseType, viewProperties.calculator());
            Latest<ObservableSemanticVersion> latestAxiom =
                    snapshot.map(ObservableSemanticSnapshot::getLatestVersion).orElse(null);
            if (latestAxiom == null || !latestAxiom.isPresent()) {
                host.add(new Label("No " + premiseType.toString().toLowerCase() + " axioms for this concept."), 0, 0);
                return;
            }
            Node rendered = renderAxioms(latestAxiom.get(), premiseType, viewProperties);
            GridPane.setHgrow(rendered, Priority.ALWAYS);
            GridPane.setVgrow(rendered, Priority.ALWAYS);
            host.add(rendered, 0, 0);
        } catch (RuntimeException e) {
            LOG.error("Failed to render axioms for {}", concept, e);
            host.add(new Label("Could not render axioms."), 0, 0);
        }
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        // Focus, view, and premise are supplied by the host; nothing persisted by this area in v1.
    }

    @Override
    protected void subAreaRevert() {
        // No persisted area-specific state to revert.
    }

    @Override
    protected void subAreaSave() {
        // No persisted area-specific state to save.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> {
            this.lifecycleState.set(LifecycleState.BOUND);
            rebuild();
        });
    }

    @Override
    public void knowledgeLayoutUnbind() {
        if (coordinateSubscription != null) {
            coordinateSubscription.unsubscribe();
            coordinateSubscription = null;
        }
    }
}
