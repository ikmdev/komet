package dev.ikm.komet.layout.orchestration;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;

import java.text.NumberFormat;
import java.util.concurrent.atomic.LongAdder;

/**
 * The CountSemantics class is a subclass of TrackingCallable that counts the number of semantics in the datastore.
 */
public class CountSemantics extends TrackingCallable<Void> {
    public CountSemantics() {
        super(false, true);
        updateTitle("Counting semantics in datastore");
        updateMessage("Executing " + this.getClass().getSimpleName());
        updateProgress(-1, -1);
    }

    /**
     * This method counts the number of semantics in the datastore and reports the status to the user.
     * It uses a LongAdder to keep track of the count and updates the title and message with the count and duration.
     *
     * @return {@code null}
     * @throws Exception if an error occurs during computation
     */
    @Override
    protected Void compute() throws Exception {
        LongAdder count = new LongAdder();
        try {
            PrimitiveData.get().forEachSemanticNid((nid) -> count.increment());
            PluggableService.first(StatusReportService.class).reportStatus("Total semantic count: " + NumberFormat.getInstance().format(count.sum()));
            return null;
        } finally {
            updateTitle("Counted " + NumberFormat.getInstance().format(count.sum()) + " semantics");
            updateMessage("In " + durationString());
        }
    }
}