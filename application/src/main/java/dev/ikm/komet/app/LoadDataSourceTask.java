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
package dev.ikm.komet.app;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;

public class LoadDataSourceTask extends TrackingCallable<Void> {
    final SimpleObjectProperty<AppState> state;

    public LoadDataSourceTask(SimpleObjectProperty<AppState> state) {
        super(false, true);
        this.state = state;
        updateTitle("Loading Data Source");
        updateMessage("Executing " + PrimitiveData.getController().controllerName());
        updateProgress(-1, -1);
    }

    @Override
    protected Void compute() throws Exception {
        try {
            PrimitiveData.start();
            Platform.runLater(() -> state.set(AppState.RUNNING));
            return null;
        } finally {
            updateTitle(PrimitiveData.getController().controllerName() + " completed");
            updateMessage("In " + durationString());
        }
    }
}
