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
package dev.ikm.komet.framework.panel;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;

public class StampPanel<V extends EntityVersion> extends HBox {
    final StampEntity stampEntity;

    public StampPanel(V entityVersion, ViewProperties viewProperties) {
        super(4);
        this.stampEntity = entityVersion.stamp();

        Label stateLabel = new Label(stampEntity.state().toString());
        stateLabel.getStyleClass().add(StyleClasses.STAMP_STATUS_PANEL.toString());
        getChildren().add(stateLabel);

        Label timeLabel = new Label(DateTimeUtil.format(this.stampEntity.time()));
        timeLabel.getStyleClass().add(StyleClasses.STAMP_TIME_PANEL.toString());
        getChildren().add(timeLabel);

        Label authorLabel = new Label(viewProperties.calculator().getDescriptionTextOrNid(this.stampEntity.authorNid()));
        authorLabel.getStyleClass().add(StyleClasses.STAMP_AUTHOR_PANEL.toString());
        getChildren().add(authorLabel);

        Label moduleLabel = new Label(viewProperties.calculator().getDescriptionTextOrNid(this.stampEntity.moduleNid()));
        moduleLabel.getStyleClass().add(StyleClasses.STAMP_MODULE_PANEL.toString());
        getChildren().add(moduleLabel);

        Label pathLabel = new Label(viewProperties.calculator().getDescriptionTextOrNid(this.stampEntity.pathNid()));
        pathLabel.getStyleClass().add(StyleClasses.STAMP_PATH_PANEL.toString());
        getChildren().add(pathLabel);

        this.getStyleClass().add(StyleClasses.STAMP_PANEL.toString());
    }
}
