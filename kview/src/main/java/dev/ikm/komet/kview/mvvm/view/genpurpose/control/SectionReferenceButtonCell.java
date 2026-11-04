package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.function.Function;

/**
 * The closed reference-component combobox's cell: the semantic's identicon, the value that tells
 * it apart from the section's other options, and the pattern it is a semantic of. The options'
 * usual label ("Module in Test Performed Pattern") is the same for every one of them, so it says
 * nothing about which is chosen.
 *
 * <p>The value gives way first when the combobox is narrow, and the cell reports when it is cut
 * off ({@link #valueTruncatedProperty()}) so the section can show its reference strip, which has
 * the room, instead.
 */
public class SectionReferenceButtonCell extends ListCell<EntityFacade> {

    private static final int IDENTICON_SIZE = 12;

    private final Function<EntityFacade, String> valueText;
    private final Function<EntityFacade, String> patternText;

    private final ImageView identicon = new ImageView();
    private final Label valueLabel = new Label();
    private final Label patternLabel = new Label();
    private final HBox line;

    private final ReadOnlyBooleanWrapper valueTruncated = new ReadOnlyBooleanWrapper(this, "valueTruncated", false);

    /**
     * @param valueText   the value telling the option apart, empty for none
     * @param patternText the display name of the option's pattern
     */
    public SectionReferenceButtonCell(Function<EntityFacade, String> valueText, Function<EntityFacade, String> patternText) {
        this.valueText = valueText;
        this.patternText = patternText;

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        identicon.setFitWidth(IDENTICON_SIZE);
        identicon.setFitHeight(IDENTICON_SIZE);

        valueLabel.getStyleClass().add("reference-button-value");
        patternLabel.getStyleClass().add("reference-button-pattern");
        // The pattern name keeps its width; the value is what gives way.
        patternLabel.setMinWidth(Region.USE_PREF_SIZE);

        line = new HBox(identicon, valueLabel, patternLabel) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                valueTruncated.set(valueLabel.prefWidth(-1) > valueLabel.getWidth() + 0.5);
            }
        };
        line.getStyleClass().add("reference-button");

        getStyleClass().add("semantic-list-cell");
    }

    /** Whether the value is cut off with an ellipsis in the cell's current width. */
    public ReadOnlyBooleanProperty valueTruncatedProperty() { return valueTruncated.getReadOnlyProperty(); }
    public boolean isValueTruncated() { return valueTruncated.get(); }

    @Override
    protected void updateItem(EntityFacade item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            identicon.setImage(Identicon.generateIdenticonImage(item.publicId()));
            valueLabel.setText(valueText.apply(item));
            patternLabel.setText("· " + patternText.apply(item));
            setGraphic(line);
        } else {
            setGraphic(null);
            valueTruncated.set(false);
        }
    }
}
