package dev.ikm.komet.layout;


import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;

public interface KlViewFactory extends  KlFactory<KlView> {

    /**
     * Creates a new instance of {@code KlView} using the provided preferences factory
     * and view coordinate record. This method is used to construct and configure
     * a contextual view layout based on the specified parameters.
     *
     * @param preferencesFactory the {@code KlPreferencesFactory} used to create and manage
     *                           preferences for the {@code KlView}
     * @param viewCoordinateRecord the {@code ViewCoordinateRecord} that defines the
     *                             contextual view coordinates for this {@code KlView}
     * @return a new instance of {@code KlView} configured with the provided preferences
     *         factory and view coordinate record
     */
    KlView createWithView(KlPreferencesFactory preferencesFactory, ViewCoordinateRecord viewCoordinateRecord);
}
