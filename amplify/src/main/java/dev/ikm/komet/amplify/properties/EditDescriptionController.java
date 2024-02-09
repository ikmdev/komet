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
package dev.ikm.komet.amplify.properties;

import dev.ikm.komet.amplify.commons.BasicController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EditDescriptionController implements BasicController {

    @FXML
    private Label editDescriptionTitleLabel;


    @Override
    @FXML
    public void initialize() {
        clearView();
        setEditDescriptionTitleLabel("Subpulmonic stenosis, ventricular septal defect, " +
                "overriding aorta, AND right ventricular hypertrophy");
    }

    public void setEditDescriptionTitleLabel(String addAxiomTitleLabelText) {
        this.editDescriptionTitleLabel.setText(addAxiomTitleLabelText);
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
}
