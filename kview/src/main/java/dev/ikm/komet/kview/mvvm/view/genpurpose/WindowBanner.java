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
package dev.ikm.komet.kview.mvvm.view.genpurpose;

import static dev.ikm.komet.kview.klfields.KlFieldHelper.retrieveCommittedLatestVersion;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableEntitySnapshot;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.common.ViewCalculatorUtils;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.State;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The banner of a general-purpose KL window: the component's title with its identicon, its public
 * identifiers and the STAMP of its latest committed version. Shown for a component on demand —
 * on open, after a coordinate change, after a publish — and, in create mode, with the STAMP a
 * component would be committed with instead.
 */
final class WindowBanner {

    private static final Logger LOG = LoggerFactory.getLogger(WindowBanner.class);

    private final ViewProperties viewProperties;
    private final ComponentItemNode windowConceptTitle;
    private final Tooltip windowConceptTitleTooltip;
    private final PublicIDListControl identifierControl;
    private final StampViewControl stampViewControl;

    /**
     * @param view           the window view holding the banner's controls
     * @param viewProperties the view the descriptions in the banner follow
     */
    WindowBanner(GenPurposeWindowView view, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.windowConceptTitle = view.getWindowConceptTitle();
        this.windowConceptTitleTooltip = view.getWindowConceptTitleTooltip();
        this.identifierControl = view.getIdentifierControl();
        this.stampViewControl = view.getStampViewControl();

        // The header STAMP is view-only in this window — clicking it must not select it or open
        // the STAMP form.
        stampViewControl.setSelectable(false);
    }

    /**
     * Shows the component: its identifiers, its title and the STAMP of its latest committed
     * version.
     */
    void updateComponent(EntityFacade component) {
        updateDisplayIdentifier(component);
        updateWindowTitle(component);
        updateStampControl(component);
    }

    /// Show the public ID
    private void updateDisplayIdentifier(EntityFacade refComponent) {
        ViewCalculator viewCalculator = viewProperties.calculator();
        identifierControl.updatePublicIdList(viewCalculator, refComponent);
    }

    private void updateStampControl(EntityFacade refConcept) {
        ObservableEntity observableEntity = ObservableEntityHandle.get(refConcept.nid()).expectEntity();
        ObservableEntitySnapshot observableEntitySnapshot;
        try {
            observableEntitySnapshot = observableEntity.getSnapshot(viewProperties.calculator());
        } catch (IllegalStateException e) {
            // No version of the concept passes the current view coordinate (e.g. a status/path filter that
            // excludes every version); leave the stamp control as-is rather than throwing out of the
            // coordinate-change listener chain — which would also abort the supplemental areas' re-render
            // (ike-issues#666).
            LOG.debug("No latest version for nid {} under the current view coordinate; skipping stamp update",
                    refConcept.nid());
            return;
        }
        Latest<EntityVersion> latestEntityVersion = retrieveCommittedLatestVersion(observableEntitySnapshot);
        latestEntityVersion.ifPresent(latestVersion -> {
            StampEntity stampEntity = latestEntityVersion.get().stamp();

            // -- status
            State newStatus = stampEntity.state();
            String statusMsg = newStatus == null ? "Active" : viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(((State) newStatus).nid());
            stampViewControl.setStatus(statusMsg);

            // -- time
            long newTime = stampEntity.time();
            stampViewControl.setLastUpdated(DateTimeUtil.format(newTime, DateTimeUtil.DAY_FORMATTER));

            // -- author
            ConceptFacade authorConcept = stampEntity.author();
            String authorDescription = ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(authorConcept, viewProperties);
            stampViewControl.setAuthor(authorDescription);

            // -- module
            ConceptFacade newModule = stampEntity.module();
            String newModuleDescription;
            if (newModule == null) {
                newModuleDescription = "";
            } else {
                newModuleDescription = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((newModule).nid());
            }
            stampViewControl.setModule(newModuleDescription);

            // -- path
            ConceptFacade newPath = stampEntity.path();
            String pathDescr;
            if (newPath == null) {
                pathDescr = "";
            } else {
                pathDescr = viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid((newPath).nid());
            }
            stampViewControl.setPath(pathDescr);
        });
    }

    /**
     * Populates the header STAMP control from the edit coordinate — the author, module and path a
     * newly created component will be committed with, and the Active status it will be committed
     * as. Used in create mode, where no committed STAMP exists yet to read those values from.
     */
    void populateStampFromEditCoordinate() {
        var editCoordinate = viewProperties.nodeView().editCoordinate();

        stampViewControl.setStatus(viewProperties.calculator()
                .getPreferredDescriptionTextWithFallbackOrNid(State.ACTIVE.nid()));

        ConceptFacade author = editCoordinate.getAuthorForChanges();
        stampViewControl.setAuthor(ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(author, viewProperties));

        ConceptFacade module = editCoordinate.defaultModuleProperty().get();
        stampViewControl.setModule(ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(module, viewProperties));

        ConceptFacade path = editCoordinate.defaultPathProperty().get();
        stampViewControl.setPath(ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(path, viewProperties));
    }

    private void updateWindowTitle(EntityFacade refConcept) {
        // Follow the view coordinate's description-type preference (FQN vs preferred), like the axiom
        // badges, so the header tracks the coordinate too (ike-issues#660).
        String conceptNameStr = viewProperties.calculator().getDescriptionTextOrNid(refConcept.nid());
        Image identicon = Identicon.generateIdenticonImage(refConcept.publicId());

        boolean isConcept = EntityHandle.get(refConcept).isConcept();
        ComponentItem componentItem = new ComponentItem(conceptNameStr, identicon, refConcept.publicId(), isConcept);

        windowConceptTitle.setComponentItem(componentItem);

        windowConceptTitleTooltip.setText(conceptNameStr);
    }
}
