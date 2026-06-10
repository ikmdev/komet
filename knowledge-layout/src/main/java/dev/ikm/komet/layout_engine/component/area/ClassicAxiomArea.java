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
import dev.ikm.komet.framework.panel.axiom.AxiomView;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.area.AreaGridSettings;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The <em>classic</em> concept axiom view as a Knowledge-Layout {@link SupplementalAreaBlueprint
 * supplemental area} — the default provider behind the swappable axiom view (ike-issues#644). It
 * wraps the existing {@link AxiomView} (rather than replacing it), so the historic logical-definition
 * rendering remains available and selectable alongside the refreshed {@code KonceptAxiomTreeArea}
 * (ike-issues#639).
 *
 * <p>Following the {@code AbstractCheckArea} idiom, the host window injects the area's context: the
 * focused concept ({@link #setFocusConcept(EntityFacade)}), the {@link ViewProperties}
 * ({@link #setAxiomViewProperties(ViewProperties)}), and the {@link PremiseType}
 * ({@link #setPremiseType(PremiseType)}). When a concept and a view are available the area resolves
 * the concept's axiom semantic for the current premise (the same {@code ObservableSemantic.getAxiomSnapshot}
 * path the inline axiom popover uses) and mounts {@code AxiomView.getEditor()} in its centre.
 */
public class ClassicAxiomArea extends SupplementalAreaBlueprint implements KlAxiomArea {

    private static final Logger LOG = LoggerFactory.getLogger(ClassicAxiomArea.class);

    private final ObjectProperty<EntityFacade> focus = new SimpleObjectProperty<>(this, "focus");
    private ViewProperties injectedViewProperties;
    private PremiseType premiseType = PremiseType.INFERRED;

    {
        focus.addListener((obs, oldFocus, newFocus) -> rebuildLater());
    }

    /**
     * Restore constructor.
     *
     * @param preferences the preferences node backing this area
     */
    public ClassicAxiomArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Create constructor.
     *
     * @param preferencesFactory factory for this area's preferences node
     * @param areaFactory        the factory creating this area
     */
    public ClassicAxiomArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /**
     * Sets the concept whose axioms this area renders. The host window supplies this, typically the
     * journal's focused concept.
     *
     * @param concept the concept to render, or {@code null} to clear
     */
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
     * The concept this area renders. The host may bind this to the surrounding layout's focus.
     *
     * @return the focus property
     */
    public final ObjectProperty<EntityFacade> focusProperty() {
        return focus;
    }

    /**
     * Injects the view this area queries, overriding the context-derived default. A host window
     * normally calls this before the area is shown.
     *
     * @param viewProperties the view to use, or {@code null} to fall back to the context view
     */
    public final void setAxiomViewProperties(ViewProperties viewProperties) {
        this.injectedViewProperties = viewProperties;
        rebuildLater();
    }

    /**
     * Sets the premise (stated or inferred) whose axioms are shown.
     *
     * @param premiseType the premise type; {@code null} is treated as {@link PremiseType#INFERRED}
     */
    public final void setPremiseType(PremiseType premiseType) {
        this.premiseType = (premiseType == null) ? PremiseType.INFERRED : premiseType;
        rebuildLater();
    }

    /**
     * Resolves the {@link ViewProperties} to render against: the injected view when one was supplied,
     * otherwise an overridable view derived from this area's layout context.
     *
     * @return the view properties, or {@code null} if none could be resolved
     */
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
            AxiomView axiomView = AxiomView.create(latestAxiom.get(), premiseType, viewProperties);
            ScrollPane scroll = new ScrollPane(axiomView.getEditor());
            scroll.setFitToWidth(true);
            GridPane.setHgrow(scroll, Priority.ALWAYS);
            GridPane.setVgrow(scroll, Priority.ALWAYS);
            host.add(scroll, 0, 0);
        } catch (RuntimeException e) {
            LOG.error("Failed to render classic axiom view for {}", concept, e);
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
        // Nothing to unbind; the AxiomView is rebuilt from the injected context on bind.
    }

    /**
     * Base for the discoverable axiom-view factories. Each concrete factory bakes a fixed
     * {@link PremiseType} so the layout editor offers a distinct supplemental-area item for stated
     * and inferred, persisted by factory class name (ike-issues#644).
     */
    private abstract static class AxiomFactory implements SupplementalAreaBlueprint.Factory<ClassicAxiomArea> {

        /**
         * The premise this factory's areas render.
         *
         * @return the premise type
         */
        abstract PremiseType premise();

        @Override
        public ClassicAxiomArea restore(KometPreferences preferences) {
            ClassicAxiomArea area = new ClassicAxiomArea(preferences);
            area.setPremiseType(premise());
            return area;
        }

        @Override
        public ClassicAxiomArea create(KlPreferencesFactory preferencesFactory, AreaGridSettings areaGridSettings) {
            ClassicAxiomArea area = new ClassicAxiomArea(preferencesFactory, this);
            area.setAreaLayout(areaGridSettings.with(this.getClass()));
            area.setPremiseType(premise());
            return area;
        }
    }

    /**
     * Discoverable factory for the <b>stated</b> classic axiom view — the author-asserted logical
     * definition. Offered by the layout editor as a supplemental area (ike-issues#644).
     */
    public static final class StatedFactory extends AxiomFactory {
        @Override
        PremiseType premise() {
            return PremiseType.STATED;
        }

        /** @return the layout-editor label for this factory */
        @Override
        public String factoryName() {
            return "Axiom view — stated";
        }

        /** @return the product name shown in tooltips */
        @Override
        public String productName() {
            return "Stated axioms";
        }

        /** @return the product description shown in tooltips */
        @Override
        public String productDescription() {
            return "The concept's stated (author-asserted) logical definition, rendered by the classic axiom view.";
        }
    }

    /**
     * Discoverable factory for the <b>inferred</b> classic axiom view — the classifier-computed
     * logical definition. Offered by the layout editor as a supplemental area (ike-issues#644).
     */
    public static final class InferredFactory extends AxiomFactory {
        @Override
        PremiseType premise() {
            return PremiseType.INFERRED;
        }

        /** @return the layout-editor label for this factory */
        @Override
        public String factoryName() {
            return "Axiom view — inferred";
        }

        /** @return the product name shown in tooltips */
        @Override
        public String productName() {
            return "Inferred axioms";
        }

        /** @return the product description shown in tooltips */
        @Override
        public String productDescription() {
            return "The concept's inferred (classifier-computed) logical definition, rendered by the classic axiom view.";
        }
    }
}
