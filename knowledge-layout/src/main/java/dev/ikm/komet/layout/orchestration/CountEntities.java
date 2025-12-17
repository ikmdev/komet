package dev.ikm.komet.layout.orchestration;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;

import java.text.NumberFormat;
import java.util.concurrent.atomic.LongAdder;

/**
 * The CountEntities class is a subclass of TrackingCallable that counts the number of entities in the datastore.
 */
public class CountEntities extends TrackingCallable<Void> {
    public CountEntities() {
        super(false, true);
        updateTitle("Counting entities in datastore");
        updateMessage("Executing " + this.getClass().getSimpleName());
        updateProgress(-1, -1);
    }

    /**
     * Computes the total count of entities in the datastore and reports the status to the user.
     *
     * @return null
     * @throws Exception if an error occurs during computation
     */
    @Override
    protected Void compute() throws Exception {
        LongAdder count = new LongAdder();
        try {
            PrimitiveData.get().forEachParallel((bytes, value) -> count.increment());
            PluggableService.first(StatusReportService.class).reportStatus("Total entity count: " + NumberFormat.getInstance().format(count.sum()));
            return null;
        } finally {
            updateTitle("Counted " + NumberFormat.getInstance().format(count.sum()) + " entities");
            updateMessage("In " + durationString());
        }
    }
}
