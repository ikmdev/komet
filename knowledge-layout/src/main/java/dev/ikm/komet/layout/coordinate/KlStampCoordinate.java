package dev.ikm.komet.layout.coordinate;

import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;

/**
 * Marker interface for stamp coordinate plugin panes. This interface designates that a plugin pane
 * is specifically related to stamp coordinates within the system, providing a more specialized type
 * of coordinate-related plugin pane.
 *
 * Extends the KlCoordinate interface to provide type safety and specific handling for
 * ObservableStampCoordinate and StampCoordinateRecord.
 */
public interface KlStampCoordinate extends KlCoordinate<ObservableStampCoordinate, StampCoordinateRecord> {
}
