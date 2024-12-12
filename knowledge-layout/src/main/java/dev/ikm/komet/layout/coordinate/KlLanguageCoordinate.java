package dev.ikm.komet.layout.coordinate;

import dev.ikm.komet.framework.view.ObservableLanguageCoordinate;
import dev.ikm.tinkar.coordinate.language.LanguageCoordinateRecord;

/**
 * Marker interface for language coordinate plugin panes. This interface designates that a plugin pane
 * is specifically related to language coordinates within the system and serves as a more specific
 * type of coordinate-related plugin pane.
 *
 * Extends the {@link KlCoordinate} interface to provide type safety and specific handling
 * for {@link ObservableLanguageCoordinate} and {@link LanguageCoordinateRecord}.
 */
public interface KlLanguageCoordinate extends KlCoordinate<ObservableLanguageCoordinate, LanguageCoordinateRecord> {
}
