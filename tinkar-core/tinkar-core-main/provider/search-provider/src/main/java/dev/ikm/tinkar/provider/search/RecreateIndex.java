package dev.ikm.tinkar.provider.search;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TrackingCallable;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/**
 * The {@code RecreateIndex} class is responsible for rebuilding a Lucene index.
 * This process includes iterating over all entities, indexing them using a provided
 * {@link Indexer}, and updating progress tracking mechanisms.
 * <p>
 * The class extends {@code TrackingCallable<Void>}, providing mechanisms for tracking
 * and reporting progress, as well as handling cancellation and lifecycle events
 * of the index recreation task.
 * <p>
 * Constructor:
 * <p> - Initializes the class with a specified {@link Indexer}.
 * <p> - Sets relevant properties for tracking task status and progress.
 * <p> - Logs the start of the index recreation process.
 * <p>
 * Methods:
 * <p> - {@link #compute()}:
 * <p>   Executes the index rebuilding task, including:
 * <p>   - Initializing load phases through {@link EntityService}.
 * <p>   - Counting and indexing entities in a parallelized manner.
 * <p>   - Committing the changes to the index.
 * <p>   - Building the "Type Ahead" search suggester.
 * <p>   - Logging and updating task completion status.
 */
public class RecreateIndex extends TrackingCallable<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(RecreateIndex.class);
    private final Indexer indexer;

    public RecreateIndex(Indexer indexer) {
        super(false, true);
        this.indexer = indexer;
        this.updateTitle("Recreate Lucene Index");
        LOG.info("Recreate Lucene Index started");
    }

    /**
     * Executes the process of rebuilding the Lucene index, including indexing entities
     * and building a "Type Ahead" search suggester.
     * The method manages progress tracking, lifecycle events, and logs the overall process duration.
     *
     * @return Returns {@code null} upon completion of the index rebuilding process.
     * @throws Exception if an error occurs during the index reconstruction or suggestion building phase.
     */
    @Override
    protected Void compute() throws Exception {
        updateTitle("Indexing Semantics");
        updateMessage("Initializing...");
        updateProgress(-1,1);

        EntityService.get().beginLoadPhase();
        try {
            LongAdder totalEntities = new LongAdder();
            LongAdder processedEntities = new LongAdder();
            PrimitiveData.get().forEachParallel((bytes, nid) -> {
                totalEntities.increment();
            });
            updateMessage("Generating Lucene Indexes...");
            updateProgress(0, totalEntities.longValue()+1);

            PrimitiveData.get().forEachParallel((bytes, nid) -> {
                Entity.get(nid).ifPresent(this.indexer::index);
                processedEntities.increment();
                if (updateIntervalElapsed()) {
                    updateProgress(processedEntities.longValue(), totalEntities.longValue());
                }
            });
            this.indexer.commit();
        } finally {
            EntityService.get().endLoadPhase(); // Ending Load Phase triggers type ahead suggester build on background thread
        }
        LOG.info("Recreate Lucene Index completed in {}", this.durationString());
        this.updateTitle("Recreate Lucene Index Completed");
        this.updateMessage("Index time: " + this.durationString());
        updateProgress(1,1);
        return null;
    }
}
