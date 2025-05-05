package dev.ikm.komet.kview.mvvm.model;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculatorWithCache;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampRecord;
import dev.ikm.tinkar.entity.StampVersionRecord;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A convenience helper class to change view coordinates when using view calculators (filters).
 */
public class ViewCoordinateHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ViewCoordinateHelper.class);
    /**
     * TODO: Experimental - will change view coordinates viewProperties.nodeView().setValue(latestViewCoordinate));
     * Changes the current view coordinate with the current datetime, thus updating its view calculator.
     * @param viewProperties A given view property
     */
    public static void changeViewCalculatorToLatestByTime(ViewProperties viewProperties, long time) {
        ViewCalculator existingViewCalculator = viewProperties.calculator();
        StampCoordinateRecord latestStampCoordinate = existingViewCalculator.vertexStampCalculator().filter().withStampPositionTime(time);
        ViewCoordinateRecord latestViewCoordinate = existingViewCalculator.viewCoordinateRecord().withStampCoordinate(latestStampCoordinate);
        Platform.runLater(() -> viewProperties.nodeView().setValue(latestViewCoordinate));
    }

    /**
     * Create a new View Calculator based on the current datetime + 24 hours with an updated view coordinate & view calculator.
     * Note: This appears to work however the records returned could be uncommitted records as they have a larger latest date.
     * @param viewProperties A given view property
     */
    public static ViewCalculatorWithCache createViewCalculatorLatestByTime(ViewProperties viewProperties) {
        return createViewCalculatorLatestByTime(viewProperties, System.currentTimeMillis() + (1000 * 60 * 60 * 24)); // 1ms x 60s x 60m x 24hrs
    }

    /**
     * Create a new View Calculator based on a time with an updated view coordinate & view calculator.
     * @param viewProperties A given view property
     * @oaran time milliseconds since Epoch time.
     */
    public static ViewCalculatorWithCache createViewCalculatorLatestByTime(ViewProperties viewProperties, long time) {
        ViewCalculator existingViewCalculator = viewProperties.calculator();
        StampCoordinateRecord latestStampCoordinate = existingViewCalculator.vertexStampCalculator().filter().withStampPositionTime(time);
        ViewCoordinateRecord latestViewCoordinate = existingViewCalculator.viewCoordinateRecord().withStampCoordinate(latestStampCoordinate);
        return new ViewCalculatorWithCache(latestViewCoordinate);
    }

    /**
     * TODO: Experimental - Returns an updated view calculator cache based on the latest committed stamp version.
     * @param viewProperties
     * @return
     */
    public static ViewCalculatorWithCache createViewCalculatorLatestCommittedStamp(ViewProperties viewProperties) {
        ViewCalculator existingViewCalculator = viewProperties.calculator();
        AtomicReference<StampVersionRecord> stampVersionRecordAtomicReference = new AtomicReference<>();
        PrimitiveData.get().forEachStampNid(intProc -> {
            StampRecord stampRecord = Entity.getStamp(intProc);
            if (stampRecord != null) {
                // compare who has the latest date
                Comparator<StampVersionRecord> comparator = Comparator.comparingLong(StampVersionRecord::time);
                // filter out versions containing Long.MAX_VALUE.
                Optional<StampVersionRecord> stampVersionRecordOpt = stampRecord.versions().stream().filter(stampVersionRecord1 -> stampVersionRecord1.committed()).max(comparator);
                if (stampVersionRecordOpt.isPresent()) {
                    long currentMaxLatest = stampVersionRecordAtomicReference.get() != null ? stampVersionRecordAtomicReference.get().time() : Long.MIN_VALUE;
                    if (stampVersionRecordOpt.get().time() > currentMaxLatest) {
                        stampVersionRecordAtomicReference.set(stampVersionRecordOpt.get());
                    }
                }
            }
        });
        StampVersionRecord stampVersionRecord = stampVersionRecordAtomicReference.get();
        if (stampVersionRecord != null) {
            // get latest stamp in database
            StampCoordinateRecord latestStampCoordinate = existingViewCalculator.vertexStampCalculator().filter().withStampPositionTime(stampVersionRecord.time());
            ViewCoordinateRecord latestViewCoordinate = existingViewCalculator.viewCoordinateRecord().withStampCoordinate(latestStampCoordinate);
            LOG.info("Created a new ViewCalculatorWithCache using " + latestViewCoordinate);
            return new ViewCalculatorWithCache(latestViewCoordinate);
        }

        // If it does not find the latest then return the current view coordinate.
        LOG.info("Created a new ViewCalculatorWithCache using current view coordinate " + viewProperties.nodeView().toViewCoordinateRecord());
        return new ViewCalculatorWithCache(viewProperties.nodeView().toViewCoordinateRecord());
    }
}
