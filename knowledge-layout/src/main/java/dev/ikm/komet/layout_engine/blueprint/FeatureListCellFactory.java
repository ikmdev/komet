package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.framework.observable.*;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.tinkar.entity.EntityVersion;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Factory class for creating custom ListCell components for a ListView
 * that displays {@link ObservableVersion} instances of type {@link EntityVersion}.
 *
 * This factory customizes the appearance and content of each cell
 * in the ListView by setting a preferred text for the stamp associated
 * with the given {@link ObservableVersion}.
 */
public class FeatureListCellFactory<LE extends Feature> implements Callback<ListView<LE>, ListCell<LE>> {
    final KlArea enclosingKlWidget;

    public FeatureListCellFactory(KlArea enclosingArea) {
        this.enclosingKlWidget = enclosingArea;
    }

    /**
     * Creates a custom `ListCell` for a `ListView` of `ObservableVersion<EntityVersion>`.
     * This method customizes how the individual items in the `ListView` are displayed,
     * showing preferred text for the stamp associated with each `ObservableVersion`.
     *
     * @param param the `ListView` to which the custom `ListCell` will be applied
     * @return a new `ListCell` populated with preferred stamp text from the `ObservableVersion`,
     *         or an appropriate placeholder if the cell is empty or contains a null value
     */
    @Override
    public ListCell<LE> call(ListView<LE> param) {
        return new ListCell<>() {
            @Override
            public void updateItem(LE listElement, boolean empty) {
                super.updateItem(listElement, empty);
                setGraphic(null);
                if (empty) {
                    setText(null);
                } else if (listElement != null) {
                    String cellText = switch (listElement) {
                        case FeatureList _ -> throw new IllegalStateException("FeatureList should not be an element in a FeatureList");
                        case ObservableFeatureDefinition definition -> "Definition" + definition;
                        case ObservableVersion ov -> enclosingKlWidget.calculatorForContext().getPreferredTextForStamp(ov.getVersionRecord().stampNid());
                        case FeatureWrapper feature -> feature.value().toString();
                        case ObservableFeature field -> "Field " + field;
                        case ObservableChronology chronology -> "Chronology " + chronology;
                    };
                    setText(cellText);
                } else {
                    setText("Null value in list cell");
                }
            }
        };
    }
}
