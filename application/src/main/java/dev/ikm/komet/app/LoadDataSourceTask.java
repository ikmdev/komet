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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadDataSourceTask extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(LoadDataSourceTask.class);
    final SimpleObjectProperty<AppState> state;

    public LoadDataSourceTask(SimpleObjectProperty<AppState> state) {
        super(false, true);
        this.state = state;
        updateTitle("Loading Data Source");
        updateMessage("Executing data source...");
        updateProgress(-1, -1);
    }

    @Override
    protected Void compute() throws Exception {
        try {
            LOG.info("LoadDataSourceTask starting...");
            PrimitiveData.start();
            LOG.info("PrimitiveData.start() completed successfully");
            LOG.info("Scheduling state transition to SELECT_USER");
            Platform.runLater(() -> {
                LOG.info("Platform.runLater executing - setting state to SELECT_USER");
                state.set(AppState.SELECT_USER);
                LOG.info("State set to SELECT_USER");
            });
            LOG.info("LoadDataSourceTask completed successfully");
            return null;
        } catch (Throwable ex) {
            LOG.error("LoadDataSourceTask failed with exception", ex);
            ex.printStackTrace();
            return null;
        } finally {
            updateTitle("Data source loaded in " + durationString());
            updateMessage("Completed");
        }
    }
}
