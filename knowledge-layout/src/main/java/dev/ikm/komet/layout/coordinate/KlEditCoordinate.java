package dev.ikm.komet.layout.coordinate;


import dev.ikm.komet.framework.view.ObservableEditCoordinate;
import dev.ikm.tinkar.coordinate.edit.EditCoordinateRecord;

/**
 * Marker interface for all edit coordinate plugin panes. This interface designates that a plugin pane
 * is specifically related to editing coordinates within the system, and serves as a common ancestor
 * for more specific edit coordinate-related interfaces.
 */
public interface KlEditCoordinate extends KlCoordinate<ObservableEditCoordinate, EditCoordinateRecord> {
}
