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
package dev.ikm.komet.kview.mvvm.view.search;

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.kview.events.MakeConceptWindowEvent;
import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.carlfx.cognitive.viewmodel.ViewModel;

import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;


public class SortResultConceptEntryController extends AbstractBasicController {

    @FXML
    private HBox searchEntryHBox;

    @FXML
    private ImageView identicon;

    @FXML
    private Text componentText;

    @FXML
    private HBox retiredHBox;

    @FXML
    private Label retiredLabel;

    @FXML
    private VBox descriptionsVBox;

    @FXML
    private Button showContextButton;

    @FXML
    private ContextMenu contextMenu;

    private boolean retired;

    public <T extends ViewModel> T getViewModel() {
        return null;
    }

    private EvtBus eventBus;

    private ConceptEntity conceptEntity;

    private ObservableViewNoOverride windowView;

    @FXML
    @Override
    public void initialize() {
        eventBus = EvtBusFactory.getDefaultEvtBus();
        showContextButton.setVisible(false);
        contextMenu.setHideOnEscape(true);
        searchEntryHBox.setOnMouseEntered(mouseEvent -> showContextButton.setVisible(true));
        searchEntryHBox.setOnMouseExited(mouseEvent -> {
            if (!contextMenu.isShowing()) {
                showContextButton.setVisible(false);
            }
        });
        showContextButton.setOnAction(event -> contextMenu.show(showContextButton, Side.BOTTOM, 0, 0));

        searchEntryHBox.setOnMouseClicked(mouseEvent -> {
            // double left click creates the concept window
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    eventBus.publish(JOURNAL_TOPIC, new MakeConceptWindowEvent(this, MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity, windowView));
                }
            }
            // right click shows the context menu
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                contextMenu.show(showContextButton, Side.BOTTOM, 0, 0);
            }
        });
    }

    @FXML
    private void populateConcept(ActionEvent actionEvent) {
        actionEvent.consume();
        eventBus.publish(JOURNAL_TOPIC, new MakeConceptWindowEvent(this, MakeConceptWindowEvent.OPEN_CONCEPT_FROM_CONCEPT, conceptEntity, windowView));
    }

    @FXML
    private  void openInConceptNavigator(ActionEvent actionEvent){
        actionEvent.consume();
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public HBox getRetiredHBox() {
        return this.retiredHBox;
    }

    public Label getRetiredLabel() {
        return this.retiredLabel;
    }

    public void setIdenticon(Image identiconImage) {
        this.identicon.setImage(identiconImage);
    }

    public void setComponentText(String topText) {
        this.componentText.setText(topText);
    }

    public VBox getDescriptionsVBox() {
        return this.descriptionsVBox;
    }

    @Override
    public void updateView() {

    }

    @Override
    public void clearView() {

    }

    @Override
    public void cleanup() {

    }

    public void setData(ConceptEntity conceptEntity) {
        this.conceptEntity = conceptEntity;
    }

    public void setWindowView(ObservableViewNoOverride windowView) {
        this.windowView = windowView;
    }

}
