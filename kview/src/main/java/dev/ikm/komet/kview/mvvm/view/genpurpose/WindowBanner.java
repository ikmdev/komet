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
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The banner of a general-purpose KL window: the component's title with its identicon, its public
 * identifiers and the STAMP of its latest committed version. Shown for a component on demand —
 * on open, after a coordinate change, after a publish — and, in create mode, with the STAMP a
 * component would be committed with instead. Owns its layout: the title grid on the left, the
 * STAMP on the right.
 *
 * <p>In the standard Semantic window the banner also says what the semantic is a semantic of: a
 * line under the title naming its pattern, as a link to the pattern, with the pattern's meaning
 * and purpose ({@link #updateSemanticPattern}). Before the semantic exists that line and a muted
 * placeholder are all the title shows ({@link #showPlaceholderTitle}).
 */
final class WindowBanner extends BorderPane {

    private static final Logger LOG = LoggerFactory.getLogger(WindowBanner.class);

    /** Marks the title as standing in for a component that doesn't exist yet (see kview.css). */
    private static final PseudoClass PLACEHOLDER = PseudoClass.getPseudoClass("placeholder");

    /** The line under the title naming a semantic's pattern; shown in the standard Semantic window only. */
    private final TextFlow patternLine = new TextFlow();
    private final Hyperlink patternLink = new Hyperlink();
    private final Text meaningText = new Text();
    private final Text purposeText = new Text();

    private final PublicIDListControl identifierControl = new PublicIDListControl();
    // Built bare, off ComponentItemNodeFactory, so the title shows the identicon and name only: no
    // kind sigil or taxonomic status cluster leading it.
    private final ComponentItemNode windowTitle = new ComponentItemNode();
    private final Tooltip windowTitleTooltip = new Tooltip("Empty Tooltip");
    private final StampViewControl stampViewControl = new StampViewControl();

    WindowBanner() {
        ColumnConstraints iconColumn = new ColumnConstraints();
        iconColumn.setHalignment(HPos.LEFT);
        iconColumn.setMaxWidth(586);
        iconColumn.setMinWidth(Region.USE_PREF_SIZE);
        iconColumn.setPrefWidth(42);
        ColumnConstraints titleColumn = new ColumnConstraints();
        titleColumn.setHalignment(HPos.LEFT);
        titleColumn.setMaxWidth(586);
        titleColumn.setMinWidth(234);
        titleColumn.setPrefWidth(416);
        RowConstraints titleRow = new RowConstraints();
        titleRow.setVgrow(Priority.NEVER);
        RowConstraints identifierRow = new RowConstraints();
        identifierRow.setMaxHeight(77);
        identifierRow.setMinHeight(0);
        identifierRow.setVgrow(Priority.SOMETIMES);

        identifierControl.setId("identifierControl");
        identifierControl.getStyleClass().add("concept-definition-title");
        GridPane.setColumnIndex(identifierControl, 0);
        GridPane.setColumnSpan(identifierControl, 3);
        GridPane.setRowIndex(identifierControl, 1);
        GridPane.setHalignment(identifierControl, HPos.LEFT);
        GridPane.setValignment(identifierControl, VPos.TOP);

        windowTitle.setId("windowTitle");
        windowTitle.setMaxHeight(Double.MAX_VALUE);
        windowTitle.setPrefWidth(416);
        windowTitle.getStyleClass().add("window-main-title");
        windowTitle.setWrapText(true);
        windowTitle.setTooltip(windowTitleTooltip);

        // The pattern line sits right under the title, in the title's own grid cell, so a window
        // without one (every window but the Semantic window) keeps the grid's rows as they were.
        patternLink.getStyleClass().add("banner-pattern-link");
        patternLink.setOnAction(_ -> getOnOpenPattern().run());
        meaningText.getStyleClass().add("banner-pattern-value");
        purposeText.getStyleClass().add("banner-pattern-value");
        patternLine.getStyleClass().add("window-banner-pattern-line");
        patternLine.getChildren().addAll(
                patternLineLabel("Pattern: "), patternLink,
                patternLineSeparator(), patternLineLabel("Meaning: "), meaningText,
                patternLineSeparator(), patternLineLabel("Purpose: "), purposeText);
        patternLine.setVisible(false);
        patternLine.setManaged(false);

        VBox titleBox = new VBox(windowTitle, patternLine);
        titleBox.getStyleClass().add("window-banner-title-box");
        GridPane.setColumnIndex(titleBox, 0);
        GridPane.setColumnSpan(titleBox, 3);

        GridPane bannerGrid = new GridPane();
        bannerGrid.setMaxHeight(Double.MAX_VALUE);
        bannerGrid.setMaxWidth(Double.MAX_VALUE);
        bannerGrid.setPrefWidth(510);
        bannerGrid.getStyleClass().addAll("concept-detail-banner-background", "window-banner-grid");
        bannerGrid.getColumnConstraints().addAll(iconColumn, titleColumn, new ColumnConstraints());
        bannerGrid.getRowConstraints().addAll(titleRow, identifierRow);
        bannerGrid.getChildren().addAll(identifierControl, titleBox);

        // The header STAMP is view-only in this window — clicking it must not select it or open
        // the STAMP form.
        stampViewControl.setId("stampViewControl");
        stampViewControl.setSelectable(false);
        StackPane stampHolder = new StackPane(stampViewControl);
        BorderPane.setAlignment(stampHolder, Pos.CENTER);
        BorderPane.setMargin(stampHolder, new Insets(5, 5, 5, 0));

        setPrefWidth(894);
        getStyleClass().add("concept-detail-banner-background");
        BorderPane.setAlignment(this, Pos.CENTER);
        setCenter(bannerGrid);
        setRight(stampHolder);
    }

    /**
     * Shows the component: its identifiers, its title and the STAMP of its latest committed
     * version, with the descriptions following the view.
     */
    void updateComponent(ViewProperties viewProperties, EntityFacade component) {
        updateDisplayIdentifier(viewProperties, component);
        updateWindowTitle(viewProperties, component);
        updateStampControl(viewProperties, component);
    }

    /**
     * Shows, under the title, the pattern a semantic is a semantic of: its name, which opens the
     * pattern ({@link #onOpenPatternProperty()}), and its meaning and purpose, read through the view.
     */
    void updateSemanticPattern(ViewProperties viewProperties, PatternFacade pattern) {
        var languageCalculator = viewProperties.calculator().languageCalculator();
        patternLink.setText(languageCalculator.getDescriptionText(pattern.nid())
                .orElseGet(() -> languageCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(pattern.nid())));

        Latest<PatternEntityVersion> latestPattern = viewProperties.calculator().latestPatternEntityVersion(pattern);
        latestPattern.ifPresent(patternVersion -> {
            meaningText.setText(languageCalculator.getDescriptionText(patternVersion.semanticMeaningNid())
                    .orElse("No description for meaning"));
            purposeText.setText(languageCalculator.getDescriptionText(patternVersion.semanticPurposeNid())
                    .orElse("No description for purpose"));
        });

        patternLine.setVisible(true);
        patternLine.setManaged(true);
    }

    /**
     * Shows a muted line in place of the title, with no identicon and no identifiers: the window's
     * component doesn't exist yet. The next {@link #updateComponent} replaces it.
     */
    void showPlaceholderTitle(String text) {
        windowTitle.setComponentItem(new ComponentItem(text, null));
        windowTitle.pseudoClassStateChanged(PLACEHOLDER, true);
        windowTitleTooltip.setText(text);
        identifierControl.setPublicIdList(java.util.List.of());
    }

    private static Text patternLineLabel(String text) {
        Text label = new Text(text);
        label.getStyleClass().add("banner-pattern-label");
        return label;
    }

    private static Text patternLineSeparator() {
        Text separator = new Text("   •   ");
        separator.getStyleClass().add("banner-pattern-separator");
        return separator;
    }

    /// Show the public ID
    private void updateDisplayIdentifier(ViewProperties viewProperties, EntityFacade refComponent) {
        ViewCalculator viewCalculator = viewProperties.calculator();
        identifierControl.updatePublicIdList(viewCalculator, refComponent);
    }

    private void updateStampControl(ViewProperties viewProperties, EntityFacade refComponent) {
        ObservableEntity observableEntity = ObservableEntityHandle.get(refComponent.nid()).expectEntity();
        ObservableEntitySnapshot observableEntitySnapshot;
        try {
            observableEntitySnapshot = observableEntity.getSnapshot(viewProperties.calculator());
        } catch (IllegalStateException e) {
            // No version of the concept passes the current view coordinate (e.g. a status/path filter that
            // excludes every version); leave the stamp control as-is rather than throwing out of the
            // coordinate-change listener chain — which would also abort the supplemental areas' re-render
            // (ike-issues#666).
            LOG.debug("No latest version for nid {} under the current view coordinate; skipping stamp update",
                    refComponent.nid());
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
    void populateStampFromEditCoordinate(ViewProperties viewProperties) {
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

    private void updateWindowTitle(ViewProperties viewProperties, EntityFacade refComponent) {
        // Follow the view coordinate's description-type preference (FQN vs preferred), like the axiom
        // badges, so the header tracks the coordinate too (ike-issues#660).
        // A semantic has no description of its own: it is named after what it is about and its
        // pattern, the way the classic Semantic window names it.
        EntityHandle handle = EntityHandle.get(refComponent);
        String componentName = handle.isSemantic()
                ? semanticName(viewProperties, handle.expectSemantic())
                : viewProperties.calculator().getDescriptionTextOrNid(refComponent.nid());
        Image identicon = Identicon.generateIdenticonImage(refComponent.publicId());

        boolean isConcept = handle.isConcept();
        ComponentItem componentItem = new ComponentItem(componentName, identicon, refComponent.publicId(), isConcept);

        windowTitle.setComponentItem(componentItem);
        windowTitle.pseudoClassStateChanged(PLACEHOLDER, false);

        windowTitleTooltip.setText(componentName);
    }

    /**
     * "Reference component in Pattern": the reference component by the view's name for it, the
     * pattern by its fully qualified name.
     */
    private static String semanticName(ViewProperties viewProperties, SemanticEntity<?> semantic) {
        var languageCalculator = viewProperties.calculator().languageCalculator();
        String referenceComponentName = languageCalculator.getDescriptionText(semantic.referencedComponentNid())
                .orElse("No description for referenced component");
        return referenceComponentName + " in "
                + languageCalculator.getFullyQualifiedDescriptionTextWithFallbackOrNid(semantic.patternNid());
    }

    /*=*************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     ************************************************************************=*/

    // -- on open pattern
    /** Called when the pattern named under the title is pressed, to open that pattern. */
    private final ObjectProperty<Runnable> onOpenPattern = new SimpleObjectProperty<>(this, "onOpenPattern");
    public Runnable getOnOpenPattern() { return onOpenPattern.get(); }
    public ObjectProperty<Runnable> onOpenPatternProperty() { return onOpenPattern; }
    public void setOnOpenPattern(Runnable handler) { this.onOpenPattern.set(handler); }
}
