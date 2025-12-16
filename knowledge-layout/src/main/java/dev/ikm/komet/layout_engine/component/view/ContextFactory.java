package dev.ikm.komet.layout_engine.component.view;

import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.Coordinates;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;

import java.util.UUID;

public class ContextFactory {
    private final ViewCoordinateRecord viewCoordinateRecord;

    private ContextFactory(ViewCoordinateRecord viewCoordinateRecord) {
        this.viewCoordinateRecord = viewCoordinateRecord;
    }

    public ViewContext create(KlContextProvider klContextProvider) {
        PublicId publicId = PublicIds.of(UUID.randomUUID());
        PublicIdStringKey publicIdStringKey = new PublicIdStringKey(publicId, "Context for " + klContextProvider.getClass().getSimpleName());
        return new ViewContext(klContextProvider, viewCoordinateRecord, publicIdStringKey);
    }

    public ViewContext restore(KometPreferences preferences, KlContextProvider klContextProvider) {
        return ViewContext.restore(preferences, klContextProvider);
    }

    public static ContextFactory defaultView() {
        return new ContextFactory(Coordinates.View.DefaultView());
    }

    public static ContextFactory withViewCoordinate(ViewCoordinateRecord viewCoordinateRecord) {
        return new ContextFactory(viewCoordinateRecord);
    }
}
