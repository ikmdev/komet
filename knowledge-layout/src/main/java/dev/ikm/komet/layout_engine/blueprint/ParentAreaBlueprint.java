package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.layout.KlParent;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public non-sealed abstract class ParentAreaBlueprint extends AreaBlueprint<BorderPane>
    implements KlParent<BorderPane> {

    private final GridPane gridPaneForChildren = new GridPane();
    {
        fxObject().setCenter(gridPaneForChildren);
    }

    public ParentAreaBlueprint(KometPreferences preferences) {
        super(preferences, new BorderPane());
    }

    public ParentAreaBlueprint(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory, new BorderPane());
    }

    @Override
    public final GridPane gridPaneForChildren() {
        return gridPaneForChildren;
    }
}
