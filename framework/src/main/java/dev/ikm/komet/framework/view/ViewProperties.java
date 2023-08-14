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
package dev.ikm.komet.framework.view;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.binary.DecoderInput;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;

import java.util.Optional;

public class ViewProperties {

    private final ObservableViewNoOverride parentView;
    private final ObservableViewWithOverride overridableView;

    public ViewProperties(ObservableViewWithOverride overridableView, ObservableViewNoOverride parentView) {
        this.overridableView = overridableView;
        this.parentView = parentView;
    }

    public static ViewProperties make(ObservableViewNoOverride parentView,
                                      KometPreferences preferencesNode) {
        return makeOverridableView(preferencesNode, parentView);
    }

    private static ViewProperties makeOverridableView(KometPreferences preferencesNode, ObservableViewNoOverride parentView) {
        Optional<byte[]> optionalCoordinateData = preferencesNode.getByteArray(Keys.VIEW_COORDINATE_BYTES);
        if (optionalCoordinateData.isEmpty()) {
            throw new IllegalStateException(Keys.VIEW_COORDINATE_BYTES + " not initialized: " + preferencesNode);
        }
        ViewCoordinateRecord viewCoordinateRecord = ViewCoordinateRecord.decode(new DecoderInput(optionalCoordinateData.get()));
        ViewProperties viewProperties = parentView.makeOverridableViewProperties();
        viewProperties.overridableView.setOverrides(viewCoordinateRecord);
        return viewProperties;
    }

    public ViewCalculator calculator() {
        return this.overridableView.calculator();
    }

    public ObservableViewNoOverride parentView() {
        return parentView;
    }

    public ObservableViewWithOverride nodeView() {
        return overridableView;
    }

    public enum Keys {
        VIEW_COORDINATE_BYTES,
    }
}
