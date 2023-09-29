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
package dev.ikm.komet.amplify.timeline;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;

public class TimelineController {

    @FXML
    private BorderPane contentBorderPane;

    @FXML
    private Label journaltitleLabel;

    @FXML
    private ToggleButton rangeToggleButton;

    @FXML
    private BorderPane timelineOuterPane;

    @FXML
    void openTimelinePanel(ActionEvent event) {

    }

    public void updateView(final ViewProperties viewProperties, EntityFacade entityFacade) {
        ViewCalculator viewCalculator = viewProperties.calculator();
        System.out.println("updateView called for properties " + viewCalculator);

    }


}
