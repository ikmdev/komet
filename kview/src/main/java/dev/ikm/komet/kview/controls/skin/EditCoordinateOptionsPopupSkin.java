package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.komet.kview.controls.EditCoordinateOptions;
import dev.ikm.komet.kview.controls.EditCoordinateOptionsPopup;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentControlFactory;
import dev.ikm.komet.kview.controls.SavedFiltersPopup;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EditCoordinateOptionsPopupSkin implements Skin<EditCoordinateOptionsPopup> {

    private static final Logger LOG = LoggerFactory.getLogger(EditCoordinateOptionsPopupSkin.class);

    private static final ResourceBundle resources = ResourceBundle.getBundle(
            "dev.ikm.komet.kview.controls.edit-coordinate-options");
    private static final String EDIT_COORDINATE_OPTIONS_KEY = "edit-coordinate-options";
    private static final String SAVED_FILTERS_KEY = "saved-filters";

    private final EditCoordinateOptionsPopup control;
    private final VBox root;
    private final ScrollPane scrollPane;
    private final Button revertButton;
    private final SavedFiltersPopup savedFiltersPopup;
    private final KometPreferences kometPreferences;

    // One KLComponentControl per edit-coordinate field
    private final KLComponentControl authorControl;
    private final KLComponentControl defaultModuleControl;
    private final KLComponentControl destinationModuleControl;
    private final KLComponentControl defaultPathControl;
    private final KLComponentControl promotionPathControl;

    // Original values captured when the popup opens, restored on Revert
    private ConceptFacade originalAuthor;
    private ConceptFacade originalDefaultModule;
    private ConceptFacade originalDestinationModule;
    private ConceptFacade originalDefaultPath;
    private ConceptFacade originalPromotionPath;

    // Guards against feedback loops between controls and coordinate properties
    private boolean syncingToCoordinate = false;
    private boolean syncingToControl    = false;

    private Subscription subscription;

    public EditCoordinateOptionsPopupSkin(EditCoordinateOptionsPopup control) {
        this.control = control;

        // --- Create the five single-concept pickers ----------------------
        var calc = control.getViewProperties().calculator();
        authorControl           = KLComponentControlFactory.createComponentControl(calc);
        defaultModuleControl    = KLComponentControlFactory.createComponentControl(calc);
        destinationModuleControl= KLComponentControlFactory.createComponentControl(calc);
        defaultPathControl      = KLComponentControlFactory.createComponentControl(calc);
        promotionPathControl    = KLComponentControlFactory.createComponentControl(calc);

        // Hierarchy filters — currently inactive (passes all concepts through).
        // To restrict each field to its correct concept hierarchy, replace `_ -> true`
        // with a predicate that tests ancestry via ViewCalculator, e.g.:
        //
        //   ViewCalculator vc = control.getViewProperties().calculator();
        //   authorControl.setComponentAllowedFilter(publicId -> {
        //       int nid = Entity.nid(publicId);
        //       return vc.navigationCalculator().isChildOf(nid, TinkarTerm.USER.nid());
        //   });
        //   defaultModuleControl.setComponentAllowedFilter(publicId -> {
        //       int nid = Entity.nid(publicId);
        //       return vc.navigationCalculator().isChildOf(nid, TinkarTerm.MODULE.nid());
        //   });
        //   // destinationModuleControl — same as defaultModuleControl
        //   defaultPathControl.setComponentAllowedFilter(publicId -> {
        //       int nid = Entity.nid(publicId);
        //       return vc.navigationCalculator().isChildOf(nid, TinkarTerm.PATH.nid());
        //   });
        //   // promotionPathControl — same as defaultPathControl
        //
        authorControl           .setComponentAllowedFilter(_ -> true);
        defaultModuleControl    .setComponentAllowedFilter(_ -> true);
        destinationModuleControl.setComponentAllowedFilter(_ -> true);
        defaultPathControl      .setComponentAllowedFilter(_ -> true);
        promotionPathControl    .setComponentAllowedFilter(_ -> true);

        // Titles from the shared resource bundle via Option.title()
        EditCoordinateOptions.MainEditCoordinates mc =
                control.getFilterOptions().getMainCoordinates();
        authorControl           .setTitle(mc.getAuthorForChange()    .title());
        defaultModuleControl    .setTitle(mc.getDefaultModule()       .title());
        destinationModuleControl.setTitle(mc.getDestinationModule()   .title());
        defaultPathControl      .setTitle(mc.getDefaultPath()         .title());
        promotionPathControl    .setTitle(mc.getPromotionPath()       .title());

        // --- Preferences & saved-filter popup ----------------------------
        savedFiltersPopup = new SavedFiltersPopup(this::applyFilter, this::removeFilter);
        kometPreferences = Preferences.get().getConfigurationPreferences()
                .node(EDIT_COORDINATE_OPTIONS_KEY)
                .node(control.getFilterType().name());

        // --- Header ------------------------------------------------------
        StackPane closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(_ -> getSkinnable().hide());

        Label title = new Label(resources.getString("control.title"));
        title.getStyleClass().add("title");

        ToggleButton filterToggle = new ToggleButton(null, new IconRegion("icon", "filter"));
        filterToggle.getStyleClass().add("filter-button");

        HBox headerBox = new HBox(closePane, title, filterToggle);
        headerBox.getStyleClass().add("header-box");

        // --- Content: five labelled pickers in a scroll pane ------------
        VBox contentBox = new VBox(8,
                authorControl,
                defaultModuleControl,
                destinationModuleControl,
                defaultPathControl,
                promotionPathControl);
        contentBox.getStyleClass().add("coordinate-fields-box");
        contentBox.setPadding(new Insets(8));

        scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);

        // --- Footer ------------------------------------------------------
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        StackPane saveIcon = new StackPane(new IconRegion("icon", "filter"));
        saveIcon.getStyleClass().add("region");
        Button saveButton = new Button(resources.getString("button.save"), saveIcon);
        // TODO: Save to List not yet designed; keep disabled until workflow is defined
        saveButton.setDisable(true);

        revertButton = new Button(resources.getString("button.revert"));
        revertButton.setDisable(true); // enabled once the popup has opened
        revertButton.setOnAction(_ -> revertCoordinates());

        VBox bottomBox = new VBox(saveButton, revertButton);
        bottomBox.getStyleClass().add("bottom-box");

        // --- Assemble ----------------------------------------------------
        root = new VBox(headerBox, scrollPane, spacer, bottomBox);
        root.getStyleClass().add("filter-options-popup");
        root.getStylesheets().add(
                EditCoordinateOptionsPopup.class.getResource("filter-options-popup.css")
                        .toExternalForm());

        // --- Wire controls ↔ edit coordinate -----------------------------
        wireCoordinateBindings();

        // Capture originals and enable Revert each time the popup opens
        control.setOnShown(_ -> {
            captureOriginalValues();
            revertButton.setDisable(false);
            scrollPane.setVvalue(scrollPane.getVmin());
        });

        // Saved-filters panel toggle
        subscription = savedFiltersPopup.showingProperty().subscribe((_, showing) -> {
            if (!showing) filterToggle.setSelected(false);
        });
        subscription = subscription.and(filterToggle.selectedProperty().subscribe((_, selected) -> {
            if (selected) {
                updateSavedFilterList();
                Bounds bounds = control.getStyleableNode()
                        .localToScreen(control.getStyleableNode().getLayoutBounds());
                savedFiltersPopup.show(control.getScene().getWindow(),
                        bounds.getMaxX(), bounds.getMinY());
            } else if (savedFiltersPopup.isShowing()) {
                savedFiltersPopup.hide();
            }
        }));
    }

    // -----------------------------------------------------------------------
    // Binding: KLComponentControl ↔ ObservableEditCoordinate
    // -----------------------------------------------------------------------

    private void wireCoordinateBindings() {
        ObservableEditCoordinate ec =
                control.getViewProperties().nodeView().editCoordinate();

        // Seed controls from the live coordinate on first wire-up
        initControl(authorControl,            ec.authorForChangesProperty().get());
        initControl(defaultModuleControl,     ec.defaultModuleProperty().get());
        initControl(destinationModuleControl, ec.destinationModuleProperty().get());
        initControl(defaultPathControl,       ec.defaultPathProperty().get());
        initControl(promotionPathControl,     ec.promotionPathProperty().get());

        // Control → Coordinate (user picks a concept via search or drag-drop)
        subscription = Subscription.EMPTY;
        subscription = subscription.and(authorControl.entityProperty().subscribe(e -> {
            if (syncingToControl || !(e instanceof ConceptFacade cf)) return;
            syncingToCoordinate = true;
            ec.authorForChangesProperty().setValue(cf);
            syncingToCoordinate = false;
        }));
        subscription = subscription.and(defaultModuleControl.entityProperty().subscribe(e -> {
            if (syncingToControl || !(e instanceof ConceptFacade cf)) return;
            syncingToCoordinate = true;
            ec.defaultModuleProperty().setValue(cf);
            syncingToCoordinate = false;
        }));
        subscription = subscription.and(destinationModuleControl.entityProperty().subscribe(e -> {
            if (syncingToControl || !(e instanceof ConceptFacade cf)) return;
            syncingToCoordinate = true;
            ec.destinationModuleProperty().setValue(cf);
            syncingToCoordinate = false;
        }));
        subscription = subscription.and(defaultPathControl.entityProperty().subscribe(e -> {
            if (syncingToControl || !(e instanceof ConceptFacade cf)) return;
            syncingToCoordinate = true;
            ec.defaultPathProperty().setValue(cf);
            syncingToCoordinate = false;
        }));
        subscription = subscription.and(promotionPathControl.entityProperty().subscribe(e -> {
            if (syncingToControl || !(e instanceof ConceptFacade cf)) return;
            syncingToCoordinate = true;
            ec.promotionPathProperty().setValue(cf);
            syncingToCoordinate = false;
        }));

        // Coordinate → Control (external changes propagate back into the pickers)
        subscription = subscription.and(ec.authorForChangesProperty().subscribe(cf -> {
            if (syncingToCoordinate) return;
            syncingToControl = true;
            syncControl(authorControl, cf);
            syncingToControl = false;
        }));
        subscription = subscription.and(ec.defaultModuleProperty().subscribe(cf -> {
            if (syncingToCoordinate) return;
            syncingToControl = true;
            syncControl(defaultModuleControl, cf);
            syncingToControl = false;
        }));
        subscription = subscription.and(ec.destinationModuleProperty().subscribe(cf -> {
            if (syncingToCoordinate) return;
            syncingToControl = true;
            syncControl(destinationModuleControl, cf);
            syncingToControl = false;
        }));
        subscription = subscription.and(ec.defaultPathProperty().subscribe(cf -> {
            if (syncingToCoordinate) return;
            syncingToControl = true;
            syncControl(defaultPathControl, cf);
            syncingToControl = false;
        }));
        subscription = subscription.and(ec.promotionPathProperty().subscribe(cf -> {
            if (syncingToCoordinate) return;
            syncingToControl = true;
            syncControl(promotionPathControl, cf);
            syncingToControl = false;
        }));
    }

    /** Sets the control's entity from {@code value}; no-ops when value is null. */
    private void initControl(KLComponentControl ctrl, ConceptFacade value) {
        if (value != null) {
            ctrl.setEntity(value.toProxy());
        }
    }

    /** Updates the control only when its current nid differs from the incoming value's nid. */
    private void syncControl(KLComponentControl ctrl, ConceptFacade value) {
        if (value == null) return;
        var current = ctrl.getEntity();
        if (current == null || current.nid() != value.nid()) {
            ctrl.setEntity(value.toProxy());
        }
    }

    // -----------------------------------------------------------------------
    // Revert
    // -----------------------------------------------------------------------

    private void captureOriginalValues() {
        ObservableEditCoordinate ec =
                control.getViewProperties().nodeView().editCoordinate();
        originalAuthor            = ec.authorForChangesProperty().get();
        originalDefaultModule     = ec.defaultModuleProperty().get();
        originalDestinationModule = ec.destinationModuleProperty().get();
        originalDefaultPath       = ec.defaultPathProperty().get();
        originalPromotionPath     = ec.promotionPathProperty().get();
    }

    private void revertCoordinates() {
        ObservableEditCoordinate ec =
                control.getViewProperties().nodeView().editCoordinate();
        ec.authorForChangesProperty()  .setValue(originalAuthor);
        ec.defaultModuleProperty()     .setValue(originalDefaultModule);
        ec.destinationModuleProperty() .setValue(originalDestinationModule);
        ec.defaultPathProperty()       .setValue(originalDefaultPath);
        ec.promotionPathProperty()     .setValue(originalPromotionPath);
        // Coordinate → Control subscriptions above will sync the pickers automatically
    }

    // -----------------------------------------------------------------------
    // Saved filters (infrastructure kept; Save button disabled until designed)
    // -----------------------------------------------------------------------

    private void applyFilter(String key) {
        // Saved filter apply not yet implemented
        LOG.debug("applyFilter: {} (not yet implemented)", key);
    }

    private void removeFilter(String key) {
        kometPreferences.remove(key);
        List<String> list = kometPreferences.getList(SAVED_FILTERS_KEY);
        list.remove(key);
        kometPreferences.putList(SAVED_FILTERS_KEY, list);
        updateSavedFilterList();
    }

    private void updateSavedFilterList() {
        List<String> savedFilters = kometPreferences.getList(SAVED_FILTERS_KEY, new ArrayList<>());
        savedFiltersPopup.getSavedFiltersList().setAll(savedFilters);
    }

    // -----------------------------------------------------------------------
    // Skin contract
    // -----------------------------------------------------------------------

    @Override public EditCoordinateOptionsPopup getSkinnable() { return control; }
    @Override public Node getNode() { return root; }

    @Override
    public void dispose() {
        if (subscription != null) subscription.unsubscribe();
    }
}
