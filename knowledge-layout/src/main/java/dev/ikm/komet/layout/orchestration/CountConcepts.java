package dev.ikm.komet.layout.orchestration;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;

import java.text.NumberFormat;
import java.util.concurrent.atomic.LongAdder;

/**
 * The CountConcepts class is a subclass of TrackingCallable that counts the number of concepts in the datastore.
 */
public class CountConcepts extends TrackingCallable<Void> {
    public CountConcepts() {
        super(false, true);
        updateTitle("Counting concepts in datastore");
        updateMessage("Executing " + this.getClass().getSimpleName());
        updateProgress(-1, -1);
    }

    /**
     * Computes the number of concepts in the datastore.
     *
     * @return Void.
     * @throws Exception If an error occurs during computation.
     */
    @Override
    protected Void compute() throws Exception {
        LongAdder count = new LongAdder();
        try {
            PrimitiveData.get().forEachConceptNid((nid) -> count.increment());
            PluggableService.first(StatusReportService.class).reportStatus("Total concept count: " + NumberFormat.getInstance().format(count.sum()));
            return null;
        } finally {
            updateTitle("Counted " + NumberFormat.getInstance().format(count.sum()) + " concepts");
            updateMessage("In " + durationString());
        }
    }
}
