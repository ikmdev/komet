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

import dev.ikm.komet.framework.view.ViewMenuModel;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import dev.ikm.komet.kview.controls.StampViewControl;
import dev.ikm.komet.kview.mvvm.view.journal.VerticallyFilledPane;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import dev.ikm.tinkar.entity.PatternVersionRecord;
import dev.ikm.tinkar.terms.PatternFacade;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.fxutils.window.DraggableSupport.addDraggableNodes;
import static dev.ikm.komet.preferences.KLEditorPreferences.KL_MAIN_SECTION;
import static dev.ikm.komet.preferences.KLEditorPreferences.PatternKey.PATTERN_LIST;

public class GenPurposeDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(GenPurposeDetailsController.class);

    @FXML
    private VBox mainSectionContent;

    @FXML
    private BorderPane detailsOuterBorderPane;

    @FXML
    private ToggleButton propertiesToggleButton;

    @FXML
    private MenuButton coordinatesMenuButton;

    /**
     * model required for the filter coordinates menu, used with coordinatesMenuButton
     */
    private ViewMenuModel viewMenuModel;

    /**
     * Used slide out the properties view
     */
    @FXML
    private VerticallyFilledPane propertiesSlideoutTrayPane;

    @FXML
    private ImageView identiconImageView;

    @FXML
    private Label patternTitleText;

    @FXML
    private PublicIDListControl identifierControl;

    @FXML
    StampViewControl stampViewControl;

    @FXML
    private TitledPane mainSectionTitledPane;

    @FXML
    private Button savePatternButton;

    @FXML
    private StackPane publishStackPane;

    @FXML
    private HBox tabHeader;

    @FXML
    private HBox conceptHeaderControlToolBarHbox;

    @FXML
    private Text windowTitle;

    private boolean isUpdatingStampSelection = false;

    private final Tooltip publishTooltip = new Tooltip();
    private ViewProperties viewProperties;

    public GenPurposeDetailsController() {}

    @FXML
    private void initialize() {
        stampViewControl.selectedProperty().subscribe(this::onStampSelectionChanged);

        // Bind the Publish button's disable property to the ViewModel
        publishTooltip.textProperty().bind(Bindings.when(savePatternButton.disableProperty())
                .then("Publish: Disabled")
                .otherwise("Submit"));

        // Assign the tooltip to the StackPane (container of Publish button)
        setupTooltipForDisabledButton(savePatternButton);

        updateDisplayIdentifier();

        // Setup window support with explicit draggable nodes
        addDraggableNodes(detailsOuterBorderPane, tabHeader, conceptHeaderControlToolBarHbox);
    }

    private void onStampSelectionChanged() {
        if (isUpdatingStampSelection) {
            return;
        }

        if (stampViewControl.isSelected()) {
            if (!propertiesToggleButton.isSelected()) {
                propertiesToggleButton.fire();
            }
//            if (CREATE.equals(patternViewModel.getPropertyValue(MODE))) {
//                EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new StampEvent(stampViewControl, StampEvent.CREATE_STAMP));
//            } else {
//                EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new StampEvent(stampViewControl, StampEvent.ADD_STAMP));
//            }
        } else {
//            EvtBusFactory.getDefaultEvtBus().publish(patternViewModel.getPropertyValue(PATTERN_TOPIC), new ClosePropertiesPanelEvent(stampViewControl, CLOSE_PROPERTIES));
        }
    }

    /// Show the public ID
    private void updateDisplayIdentifier() {
//        ViewCalculator viewCalculator = getViewProperties().calculator();
//        PatternFacade patternFacade = (PatternFacade) patternViewModel.getProperty(PATTERN).getValue();
//
//        if (patternFacade != null) {
//            identifierControl.updatePublicIdList(viewCalculator, patternFacade);
//        }
    }

    /**
     * Creates the filter coordinates menu using the view calculator.
     * TODO Note that this is not a working menu, this is the first step to have propagating, inherited, filter coordinates
     * in the window/node hierarchy.
     */
    public void setupFilterCoordinatesMenu() {
//        this.viewMenuModel = new ViewMenuModel(patternViewModel.getViewProperties(), coordinatesMenuButton, "PatternDetailsController");
    }

    private void setupProperties() {
        // Stamp
//        patternViewModel.getProperty(STAMP_VIEW_MODEL).bind(propertiesController.stampFormViewModelProperty());

//        propertiesController.updateModel(patternViewModel.getPropertyValue(PATTERN));

//        patternViewModel.getProperty(MODE).subscribe(newMode -> {
//            if (newMode.equals(EDIT)) {
//                updateStampControlFromViewModel();
//
//                // now in EDIT mode, update the identifier
//                updateDisplayIdentifier();
//            }
//        });

//        propertiesController.getStampFormViewModel().getBooleanProperty(IS_CONFIRMED_OR_SUBMITTED).subscribe(this::onStampConfirmedOrSubmitted);
    }

    private void onStampConfirmedOrSubmitted(boolean isSubmittedOrConfirmed) {
        if (!isSubmittedOrConfirmed) {
            return;
        }

//        updateStampControlFromViewModel();

//        if (patternViewModel.getPropertyValue(MODE).equals(EDIT)) {
//            patternViewModel.setPropertyValue(PUBLISH_PENDING, true);
//        }

        stampViewControl.setDisable(true);
    }

//    private void updateStampControlFromViewModel() {
//        StampFormViewModelBase stampFormViewModel = propertiesController.getStampFormViewModel();
//
//        if (stampFormViewModel == null) {
//            return;
//        }
//
//        // -- status
//        State newStatus = stampFormViewModel.getPropertyValue(STATUS);
//        String statusMsg = newStatus == null ? "Active" : getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid(((State) newStatus).nid());
//        stampViewControl.setStatus(statusMsg);
//
//        // -- time
//        String newTime = stampFormViewModel.getPropertyValue(FORM_TIME_TEXT);
//        stampViewControl.setLastUpdated(newTime);
//
//        // -- author
//        EntityFacade newAuthor = stampFormViewModel.getPropertyValue(AUTHOR);
//        String authorDescription = ViewCalculatorUtils.getDescriptionTextWithFallbackOrNid(newAuthor, getViewProperties());
//        stampViewControl.setAuthor(authorDescription);
//
//        // -- module
//        ConceptFacade newModule = stampFormViewModel.getPropertyValue(MODULE);
//        String newModuleDescription;
//        if (newModule == null) {
//            newModuleDescription = "";
//        } else {
//            newModuleDescription = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid((newModule).nid());
//        }
//        stampViewControl.setModule(newModuleDescription);
//
//        // -- path
//        ConceptFacade newPath = stampFormViewModel.getPropertyValue(PATH);
//        String pathDescr;
//        if (newPath == null) {
//            pathDescr = "";
//        } else {
//            pathDescr = getViewProperties().calculator().getPreferredDescriptionTextWithFallbackOrNid((newPath).nid());
//        }
//        stampViewControl.setPath(pathDescr);
//    }

    public ViewProperties getViewProperties() {
//        return getPatternViewModel().getPropertyValue(VIEW_PROPERTIES);
        return viewProperties;
    }

    private Consumer<GenPurposeDetailsController> onCloseConceptWindow;

    public void setOnCloseConceptWindow(Consumer<GenPurposeDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with pattern: " + patternTitleText.getText());

        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
    }

    @FXML
    private void saveConceptKL(ActionEvent actionEvent) { }

    private void setupTooltipForDisabledButton(Button button) {

        button.disabledProperty().subscribe(isNowDisabled -> {
            if (isNowDisabled) {
                Tooltip.uninstall(button, publishTooltip);

                // Create unique handlers for each button-tooltip pair
                EventHandler<MouseEvent> showHandler = showTooltipOnDisabledButton(button, publishTooltip);
                EventHandler<MouseEvent> hideHandler = hideTooltipHandler(publishTooltip);

                // Store handlers on the button's properties for later removal
                button.getProperties().put("showHandler", showHandler);
                button.getProperties().put("hideHandler", hideHandler);

                publishStackPane.addEventFilter(MouseEvent.MOUSE_MOVED, showHandler);
                publishStackPane.addEventFilter(MouseEvent.MOUSE_EXITED, hideHandler);
            } else {
                Tooltip.install(button, publishTooltip);
                publishTooltip.hide();

                // Remove handlers if present
                EventHandler<MouseEvent> showHandler = (EventHandler<MouseEvent>) button.getProperties().get("showHandler");
                EventHandler<MouseEvent> hideHandler = (EventHandler<MouseEvent>) button.getProperties().get("hideHandler");
                if (showHandler != null) publishStackPane.removeEventFilter(MouseEvent.MOUSE_MOVED, showHandler);
                if (hideHandler != null) publishStackPane.removeEventFilter(MouseEvent.MOUSE_EXITED, hideHandler);
            }
        });
    }

    private EventHandler<MouseEvent> showTooltipOnDisabledButton(Button button, Tooltip tooltip) {
        return event -> {
            if (button.isDisabled()) {
                Bounds bounds = button.localToScreen(button.getBoundsInLocal());
                double mouseX = event.getScreenX();
                double mouseY = event.getScreenY();
                if (bounds.contains(mouseX, mouseY)) {
                    if (!tooltip.isShowing()) {
                        tooltip.show(button, mouseX, mouseY + 10);
                    }
                } else {
                    tooltip.hide();
                }
            } else {
                tooltip.hide();
            }
        };
    }

    private EventHandler<MouseEvent> hideTooltipHandler(Tooltip tooltip) {
        return event -> tooltip.hide();
    }

    public void init(KometPreferences editorWindowPreferences, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;

        final ViewCalculator viewCalculator = viewProperties.calculator();

        String absolutePath = editorWindowPreferences.absolutePath();
        Path path = Paths.get(absolutePath);
        String lastDirName = path.getFileName().toString();

        windowTitle.setText(lastDirName.substring(0, 1).toUpperCase() + lastDirName.substring(1));

        Optional<String> mainSectionName = editorWindowPreferences.get(KL_MAIN_SECTION);
        mainSectionName.ifPresentOrElse(sectionName -> {

            mainSectionTitledPane.setText(sectionName);
            final KometPreferences sectionPreferences = editorWindowPreferences.node(sectionName);

            List<PatternFacade> patternFacades = sectionPreferences.getPatternList(PATTERN_LIST);

            for (PatternFacade patternFacade : patternFacades) {

                Label patternTitle = new Label(patternFacade.description());
                mainSectionContent.getChildren().add(patternTitle);

                // -- add fields if they exist
                Entity<EntityVersion> entity = EntityService.get().getEntityFast(patternFacade);
                Latest<EntityVersion> optionalLatest = viewCalculator.latest(entity);

                AtomicInteger i = new AtomicInteger(1);
                optionalLatest.ifPresent(latest -> {
                    PatternVersionRecord patternVersionRecord = (PatternVersionRecord) latest;
                    ImmutableList<FieldDefinitionRecord> fieldDefinitionRecords = patternVersionRecord.fieldDefinitions();

                    fieldDefinitionRecords.stream().forEachOrdered(fieldDefinitionForEntity -> {
                        String fieldMeaning = fieldDefinitionForEntity.meaning().description();
                        Label fieldLabel = new Label("     Field " + i + ": " + fieldMeaning);
                        mainSectionContent.getChildren().add(fieldLabel);
                        i.incrementAndGet();
                    });
                });
            }
        }, () -> {
            throw new RuntimeException("Can't load section (main section) from preferences");
        });
    }
}