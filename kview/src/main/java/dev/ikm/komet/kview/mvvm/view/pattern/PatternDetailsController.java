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
package dev.ikm.komet.kview.mvvm.view.pattern;

import static dev.ikm.komet.kview.events.PatternPropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.PatternPropertyPanelEvent.OPEN_PANEL;
import static dev.ikm.komet.kview.events.ShowPatternPanelEvent.SHOW_ADD_DESCRIPTION;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isClosed;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.isOpen;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideIn;
import static dev.ikm.komet.kview.fxutils.SlideOutTrayHelper.slideOut;
import static dev.ikm.komet.kview.fxutils.ViewportHelper.clipChildren;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.CONCEPT_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.events.PatternPropertyPanelEvent;


import dev.ikm.komet.kview.events.ShowPatternPanelEvent;
import dev.ikm.komet.kview.lidr.events.LidrPropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.UUID;
import java.util.function.Consumer;

public class PatternDetailsController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternDetailsController.class);

    private static final int PENCIL_BUTTON_SPACING = 34; // pixels to offset the right arrow dropdown

    public static final URL PATTERN_PROPERTIES_VIEW_FXML_URL = PatternDetailsController.class.getResource("pattern-properties.fxml");

    private Consumer<ToggleButton> reasonerResultsControllerConsumer;

    @FXML
    private BorderPane detailsOuterBorderPane;

    @FXML
    private ToggleButton propertiesToggleButton;

    /**
     * Used slide out the properties view
     */
    @FXML
    private Pane propertiesSlideoutTrayPane;

    @FXML
    private Pane timelineSlideoutTrayPane;

    @FXML
    private Label patternTitleText;

    @FXML
    private TitledPane patternDefinitionTitledPane;

    @FXML
    private TitledPane descriptionsTitledPane;

    @FXML
    private TitledPane fieldsTitledPane;

    private PropertiesController propertiesViewController;

    private BorderPane propertiesViewBorderPane;

    @FXML
    private Button addDescriptionButton;

    @InjectViewModel
    private PatternViewModel patternViewModel;

    private UUID conceptTopic;

    private EvtBus eventBus;

    private Subscriber<PatternPropertyPanelEvent> patternPropertiesEventSubscriber;

    public PatternDetailsController() {}

    @FXML
    public void initialize() {
        // event bus will listen on this topic.
        if (conceptTopic == null) {
            // if not set caller used the one set inside the view model.
            conceptTopic = patternViewModel.getPropertyValue(CONCEPT_TOPIC);
        }

        eventBus = EvtBusFactory.getDefaultEvtBus();

        // listen for open and close events
        patternPropertiesEventSubscriber = (evt) -> {
            if (evt.getEventType() == CLOSE_PANEL) {
                LOG.info("propBumpOutListener - Close Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                propertiesToggleButton.setSelected(false);
                if (isOpen(propertiesSlideoutTrayPane)) {
                    slideIn(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                }
            } else if (evt.getEventType() == OPEN_PANEL) {
                LOG.info("propBumpOutListener - Opening Properties bumpout toggle = " + propertiesToggleButton.isSelected());
                propertiesToggleButton.setSelected(true);
                if (isClosed(propertiesSlideoutTrayPane)) {
                    slideOut(propertiesSlideoutTrayPane, detailsOuterBorderPane);
                }
            }
        };
        eventBus.subscribe(conceptTopic, PatternPropertyPanelEvent.class, patternPropertiesEventSubscriber);

        // Setup Properties
        setupProperties();
    }

    private void setupProperties() {
        // Setup Property screen bump out
        // Load Concept Properties View Panel (FXML & Controller)
        Config config = new Config(PATTERN_PROPERTIES_VIEW_FXML_URL)
                .updateViewModel("propertiesViewModel", (propertiesViewModel) ->
                        propertiesViewModel
                                .setPropertyValue(VIEW_PROPERTIES, getViewProperties())
                                .setPropertyValue(CONCEPT_TOPIC, conceptTopic));
        JFXNode<BorderPane, PropertiesController> propsFXMLLoader = FXMLMvvmLoader.make(config);
        this.propertiesViewBorderPane = propsFXMLLoader.node();
        this.propertiesViewController = propsFXMLLoader.controller();

        attachPropertiesViewSlideoutTray(this.propertiesViewBorderPane);
    }

    public ViewProperties getViewProperties() {
        return getPatternViewModel().getPropertyValue(VIEW_PROPERTIES);
    }

    private ValidationViewModel getPatternViewModel() {
        return patternViewModel;
    }

    private Consumer<PatternDetailsController> onCloseConceptWindow;

    public void setOnCloseConceptWindow(Consumer<PatternDetailsController> onClose) {
        this.onCloseConceptWindow = onClose;
    }

    public void onReasonerSlideoutTray(Consumer<ToggleButton> reasonerResultsControllerConsumer) {
        this.reasonerResultsControllerConsumer = reasonerResultsControllerConsumer;
    }

    @FXML
    void closeConceptWindow(ActionEvent event) {
        LOG.info("Cleanup occurring: Closing Window with pattern: " + patternTitleText.getText());
        if (this.onCloseConceptWindow != null) {
            onCloseConceptWindow.accept(this);
        }
        Pane parent = (Pane) detailsOuterBorderPane.getParent();
        parent.getChildren().remove(detailsOuterBorderPane);
    }

    @FXML
    private void showEditView(ActionEvent actionEvent) {
        // put the edit view in the properties pane
    }

    @FXML
    private void showEditDescriptionPanel(ActionEvent actionEvent) {
        // Todo show bump out and display Edit Description panel
        LOG.info("Todo show bump out and display Edit Description panel \n" + actionEvent);
        // publish property open.
        eventBus.publish(conceptTopic, new ShowPatternPanelEvent(actionEvent.getSource(), SHOW_ADD_DESCRIPTION));

        eventBus.publish(conceptTopic, new PatternPropertyPanelEvent(actionEvent.getSource(), OPEN_PANEL));
    }

    @FXML
    private void openReasonerSlideout(ActionEvent event) {
        LOG.info("not implemented yet");
//        ToggleButton reasonerToggle = (ToggleButton) event.getSource();
//        reasonerResultsControllerConsumer.accept(reasonerToggle);
    }

    @FXML
    public void popupStampEdit(ActionEvent event) {
        //TODO implement this method
    }

    @FXML
    private void openTimelinePanel(ActionEvent event) {
        LOG.info("not implemented yet");
//        ToggleButton timelineToggle = (ToggleButton) event.getSource();
//        // if selected open properties
//        if (timelineToggle.isSelected()) {
//            LOG.info("Opening slideout of timeline panel");
//            slideOut(timelineSlideoutTrayPane, detailsOuterBorderPane);
//        } else {
//            LOG.info("Close Properties timeline panel");
//            slideIn(timelineSlideoutTrayPane, detailsOuterBorderPane);
//        }
    }

    @FXML
    private void openPropertiesPanel(ActionEvent event) {
        ToggleButton propertyToggle = (ToggleButton) event.getSource();
        EvtType<PatternPropertyPanelEvent> eventEvtType = propertyToggle.isSelected() ? OPEN_PANEL : CLOSE_PANEL;
        eventBus.publish(conceptTopic, new PatternPropertyPanelEvent(propertyToggle, eventEvtType));
    }

    public void attachPropertiesViewSlideoutTray(Pane propertiesViewBorderPane) {
        addPaneToTray(propertiesViewBorderPane, propertiesSlideoutTrayPane);
    }

    private void addPaneToTray(Pane contentViewPane, Pane slideoutTrayPane) {
        double width = contentViewPane.getWidth();
        contentViewPane.setLayoutX(width);
        contentViewPane.getStyleClass().add("slideout-tray-pane");

        slideoutTrayPane.getChildren().add(contentViewPane);
        clipChildren(slideoutTrayPane, 0);
        contentViewPane.setLayoutX(-width);
        slideoutTrayPane.setMaxWidth(0);

        Region contentRegion = contentViewPane;
        // binding the child's height to the preferred height of hte parent
        // so that when we resize the window the content in the slide out pane
        // aligns with the details view
        contentRegion.prefHeightProperty().bind(slideoutTrayPane.heightProperty());
    }

    public void updateView() {
    }

    public void putTitlePanesArrowOnRight() {
        putArrowOnRight(this.patternDefinitionTitledPane);
        putArrowOnRight(this.descriptionsTitledPane);
        putArrowOnRight(this.fieldsTitledPane);
    }

    /**
     * Code credit
     * https://stackoverflow.com/a/55085777
     * @param pane
     */
    private void putArrowOnRight(TitledPane pane) {
        pane.layout();
        pane.applyCss();
        Region title = (Region) pane.lookup(".title");
        Region arrow = (Region) title.lookup(".arrow-button");
        Text text = (Text) title.lookup(".text");

        arrow.translateXProperty().bind(Bindings.createDoubleBinding(() -> {
            double rightInset = title.getPadding().getRight() + PENCIL_BUTTON_SPACING;
            return title.getWidth() - arrow.getLayoutX() - arrow.getWidth() - rightInset;
        }, title.paddingProperty(), title.widthProperty(), arrow.widthProperty(), arrow.layoutXProperty()));
        arrow.setStyle("-fx-padding: 0.0em 0.0em 0.0em 0.583em;");

        DoubleBinding textGraphicBinding = Bindings.createDoubleBinding(() -> {
            switch (pane.getAlignment()) {
                case TOP_CENTER:
                case CENTER:
                case BOTTOM_CENTER:
                case BASELINE_CENTER:
                    return 0.0;
                default:
                    return -(arrow.getWidth());
            }
        }, arrow.widthProperty(), pane.alignmentProperty());
        text.translateXProperty().bind(textGraphicBinding);

        pane.graphicProperty().addListener((observable, oldGraphic, newGraphic) -> {
            if (oldGraphic != null) {
                oldGraphic.translateXProperty().unbind();
                oldGraphic.setTranslateX(0);
            }
            if (newGraphic != null) {
                newGraphic.translateXProperty().bind(textGraphicBinding);
            }
        });
        if (pane.getGraphic() != null) {
            pane.getGraphic().translateXProperty().bind(textGraphicBinding);
        }
    }
}
