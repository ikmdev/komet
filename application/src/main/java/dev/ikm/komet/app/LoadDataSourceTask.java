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
