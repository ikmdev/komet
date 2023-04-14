package dev.ikm.komet.details.concept;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.StampEntityVersion;
import dev.ikm.tinkar.entity.transaction.Transaction;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static dev.ikm.komet.framework.PseudoClasses.UNCOMMITTED_PSEUDO_CLASS;
import static dev.ikm.komet.framework.StyleClasses.STAMP_INDICATOR;
import static dev.ikm.tinkar.common.util.time.DateTimeUtil.FORMATTER;

/**
 * @author kec
 */
public class StampControl extends Label {
    // TODO FIRST_COLUMN_WIDTH & BADGE_WIDTH came from BadgedVersionPaneModel, maybe relocate to there.
    public static final int FIRST_COLUMN_WIDTH = 32;
    protected static final int BADGE_WIDTH = 25;

    public StampControl() {
        this.getStyleClass().setAll(STAMP_INDICATOR.toString());
    }

    public StampControl(String text) {
        super(text);
        this.getStyleClass().setAll(STAMP_INDICATOR.toString());
    }

    public void setStampedVersion(int stampNid, ViewProperties viewProperties, int stampOrder) {

        Latest<StampEntityVersion> stampVersion = viewProperties.calculator().latest(stampNid);
        stampVersion.ifPresent(stampEntityVersion -> {
            if (stampEntityVersion.time() == Long.MAX_VALUE) {
                pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, true);
            } else {
                pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, false);
            }
        });
        this.setMinSize(FIRST_COLUMN_WIDTH, FIRST_COLUMN_WIDTH);
        this.setPrefSize(FIRST_COLUMN_WIDTH, FIRST_COLUMN_WIDTH);
        this.setMaxSize(FIRST_COLUMN_WIDTH, FIRST_COLUMN_WIDTH);
        this.setText(Integer.toString(stampOrder));
        String toolTipText = describeStampSequenceForTooltip(stampNid, viewProperties.calculator());
        Tooltip stampTip = new Tooltip(toolTipText);
        this.setTooltip(stampTip);
    }

    public String describeStampSequenceForTooltip(int stampNid, ViewCalculator viewCalculator) {
        if (stampNid == Integer.MAX_VALUE) {
            // TODO is this still how we handle this in the Tinkar era?
            return "Uncommitted from observable with no stamped version";
        }

        return viewCalculator.latest(stampNid).ifAbsentOrFunction(
                () -> "CANCELED",
                stampVersion -> {

                    final StringBuilder sb = new StringBuilder();

                    sb.append("S: ").append(stampVersion.state());
                    sb.append("\nT: ");

                    Transaction.forVersion(stampVersion).ifPresentOrElse(transaction -> {
                        sb.append("UNCOMMITTED-");
                        if (stampVersion.time() != Long.MAX_VALUE) {
                            ZonedDateTime stampTime = stampVersion.instant().atZone(ZoneOffset.systemDefault());
                            sb.append(stampTime.format(FORMATTER));
                        }
                        sb.append(transaction.transactionUuid().toString());
                    }, () -> {
                        if (stampVersion.time() == Long.MAX_VALUE) {
                            sb.append("Uncommitted");
                        } else if (stampVersion.time() == Long.MIN_VALUE) {
                            sb.append("CANCELED");
                        } else {
                            ZonedDateTime stampTime = stampVersion.instant().atZone(ZoneOffset.systemDefault());
                            sb.append(stampTime.format(FORMATTER));
                        }
                    });
                    sb.append("\nA: ").append(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampVersion.authorNid()));
                    sb.append("\nM: ").append(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampVersion.moduleNid()));
                    sb.append("\nP: ").append(viewCalculator.getPreferredDescriptionTextWithFallbackOrNid(stampVersion.pathNid()));

                    // TODO: need pattern for commit comments...
                    /*
                    Optional<String> optionalComment = Get.commitService()
                            .getComment(stampSequence);

                    if (optionalComment.isPresent()) {
                        sb.append("\n\ncomment: ");
                        sb.append(optionalComment.get());
                    }
                     */

                    return sb.toString();
                });

    }

}
