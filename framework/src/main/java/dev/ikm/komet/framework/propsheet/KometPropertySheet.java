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
package dev.ikm.komet.framework.propsheet;

import dev.ikm.komet.framework.view.ViewProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.controlsfx.control.PropertySheet;

public class KometPropertySheet extends PropertySheet {
    private BooleanProperty hideLabels = new SimpleBooleanProperty(false);

    final ViewProperties viewProperties;

    public KometPropertySheet(ViewProperties viewProperties, boolean hideLabels) {
        this.viewProperties = viewProperties;
        this.hideLabels.set(hideLabels);

        setMode(PropertySheet.Mode.NAME);
        setSearchBoxVisible(false);
        setModeSwitcherVisible(false);
        setSkin(new KometPropertySheetSkin(this));
        setPropertyEditorFactory(new KometPropertyEditorFactory(viewProperties));
    }
    public KometPropertySheet(ViewProperties viewProperties) {
        this(viewProperties, false);
    }

    public boolean isHideLabels() {
        return hideLabels.get();
    }

    public BooleanProperty hideLabelsProperty() {
        return hideLabels;
    }

    public void setHideLabels(boolean hideLabels) {
        this.hideLabels.set(hideLabels);
    }
}
