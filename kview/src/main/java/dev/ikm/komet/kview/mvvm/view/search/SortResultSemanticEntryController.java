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

import dev.ikm.komet.kview.mvvm.view.AbstractBasicController;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.carlfx.cognitive.viewmodel.ViewModel;

public class SortResultSemanticEntryController  {

    @FXML
    private HBox searchEntryHBox;

    @FXML
    private ImageView identicon;

    @FXML
    private TextFlow textFlow;

    @FXML
    private Text semanticText;

    @FXML
    private HBox retiredHBox;

    @FXML
    private Label retiredLabel;

    @FXML
    private Button showContextButton;

    @FXML
    private ContextMenu contextMenu;

    private boolean retired;

    private static final int WIDTH_WITHOUT_RETIRED_LABEL = 212;

    @FXML
    public void initialize() {
        showContextButton.setVisible(false);
        contextMenu.setHideOnEscape(true);
        searchEntryHBox.setOnMouseEntered(mouseEvent -> showContextButton.setVisible(true));
        searchEntryHBox.setOnMouseExited(mouseEvent -> {
            if (!contextMenu.isShowing()) {
                showContextButton.setVisible(false);
            }
        });
        showContextButton.setOnAction(event -> contextMenu.show(showContextButton, Side.BOTTOM, 0, 0));
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

    public void setSemanticText(String text) {
        this.semanticText.setText(text);
    }

    public void increaseTextFlowWidth() {
        textFlow.setMinWidth(WIDTH_WITHOUT_RETIRED_LABEL);
    }
}
