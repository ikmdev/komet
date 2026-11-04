package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KometLabel;
import dev.ikm.komet.kview.mvvm.view.navigation.PatternSemanticListCell;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.css.PseudoClass;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * A Combobox Cell that shows a Semantic. This is used in a combobox in a Section to choose the Reference Component
 * used in that Section.
 *
 * <p>Given a summary function, the cell leads with the summary's text — the field values that tell
 * the semantic apart from the other options, whose labels can all read the same — above the
 * semantic's usual label. An option the function has no text for shows the label alone.
 */
public class SectionSemanticsComboBoxCell extends ListCell<EntityFacade> {

    private static final Logger LOG = LoggerFactory.getLogger(PatternSemanticListCell.class);

    /** Active on the cell's graphic while the summary line shows above the semantic's label. */
    private static final PseudoClass WITH_SUMMARY = PseudoClass.getPseudoClass("with-summary");

    private EntityHandle currentEntityHandle;
    private final KometLabel label;
    private final Label summaryLabel;
    private final VBox lines;
    private final Function<EntityFacade, String> summaryText;

    public SectionSemanticsComboBoxCell(ViewProperties viewProperties) {
        this(viewProperties, null);
    }

    /**
     * @param summaryText the text to lead an option with, empty for none; null makes every option
     *                    a single line
     */
    public SectionSemanticsComboBoxCell(ViewProperties viewProperties, Function<EntityFacade, String> summaryText) {
        this.summaryText = summaryText;

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        label = new KometLabel(viewProperties);
        label.setShowTooltip(true);

        summaryLabel = new Label();
        summaryLabel.getStyleClass().add("reference-option-summary");
        summaryLabel.setMaxWidth(Double.MAX_VALUE);

        lines = new VBox(summaryLabel, label);
        lines.getStyleClass().add("reference-option");

        getStyleClass().add("semantic-list-cell");
    }

    private void updateSummary(String summary) {
        boolean withSummary = !summary.isEmpty();
        summaryLabel.setText(summary);
        summaryLabel.setVisible(withSummary);
        summaryLabel.setManaged(withSummary);
        lines.pseudoClassStateChanged(WITH_SUMMARY, withSummary);
    }

    @Override
    protected void updateItem(EntityFacade item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            long nid = item.nid();
            currentEntityHandle = EntityHandle.get(nid);
            currentEntityHandle.ifPresent(entity -> {
                label.setEntity(entity);
                updateSummary(summaryText == null ? "" : summaryText.apply(item));
                setGraphic(lines);
            });
        } else {
            setGraphic(null);
        }
    }
}
