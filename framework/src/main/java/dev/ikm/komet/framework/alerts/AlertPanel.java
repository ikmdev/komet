package dev.ikm.komet.framework.alerts;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import dev.ikm.komet.framework.PseudoClasses;
import dev.ikm.komet.framework.StyleClasses;
import dev.ikm.komet.framework.controls.TextAreaReadOnly;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertResolver;


//~--- classes ----------------------------------------------------------------

/**
 * @author kec
 */
public class AlertPanel
        extends GridPane {
    protected final Text alertTitle = new Text();
    protected final TextAreaReadOnly alertDescription = new TextAreaReadOnly();
    protected final Button moreDetailsButton = new Button("    More details");
    protected final Node alertIcon;
    private final ToolBar resolverBar = new ToolBar();
    private final AlertObject alert;
    private boolean showDetails = false;

    //~--- constructors --------------------------------------------------------

    public AlertPanel(AlertObject alert) {
        this.alert = alert;
        alertDescription.setEditable(false);

        switch (alert.getAlertType()) {
            case CONFIRMATION:
                alertIcon = Icon.ALERT_CONFIRM2.makeIcon();
                pseudoClassStateChanged(PseudoClasses.ALERT_CONFIRM_PSEUDO_CLASS, true);
                break;

            case ERROR:
                alertIcon = Icon.ALERT_ERROR2.makeIcon();
                pseudoClassStateChanged(PseudoClasses.ALERT_ERROR_PSEUDO_CLASS, true);
                break;

            case INFORMATION:
                alertIcon = Icon.ALERT_INFORM2.makeIcon();
                pseudoClassStateChanged(PseudoClasses.ALERT_INFO_PSEUDO_CLASS, true);
                break;

            case WARNING:
                alertIcon = Icon.ALERT_WARN2.makeIcon();
                pseudoClassStateChanged(PseudoClasses.ALERT_WARN_PSEUDO_CLASS, true);
                break;

            case SUCCESS:
                alertIcon = Icon.CHECK.makeIcon();
                pseudoClassStateChanged(PseudoClasses.ALERT_SUCCESS_PSEUDO_CLASS, true);
                break;


            default:
                throw new UnsupportedOperationException("Can't handle: " + alert.getAlertType());
        }

        alertIcon.getStyleClass()
                .add(StyleClasses.ALERT_ICON.toString());

        if ((alert.getAlertDescription() != null) && !alert.getAlertDescription().isEmpty()) {
            this.alertTitle.setText(this.alert.getAlertTitle() + "...");
        } else {
            this.alertTitle.setText(this.alert.getAlertTitle());
        }

        this.alertDescription.setText(this.alert.getAlertDescription());
        this.alertTitle.getStyleClass()
                .setAll(StyleClasses.ALERT_TITLE.toString());
        this.alertDescription.getStyleClass()
                .setAll(StyleClasses.ALERT_DESCRIPTION.toString());
        //this.alertDescription.setStyle("-fx-text-fill: red ;");
        this.moreDetailsButton.getStyleClass()
                .setAll(StyleClasses.MORE_ALERT_DETAILS.toString());
        this.moreDetailsButton.setOnAction(
                (event) -> {
                    showDetails = !showDetails;
                    layoutAlert();
                });
        this.getStyleClass()
                .setAll(StyleClasses.ALERT_PANE.toString());
        layoutAlert();
    }

    //~--- methods -------------------------------------------------------------

    public final void layoutAlert() {
        getChildren()
                .clear();

        int row = 0;
        int column = 0;

        GridPane.setConstraints(
                alertIcon,
                column,
                row,
                1,
                1,
                HPos.LEFT,
                VPos.CENTER,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2, 2, 2, 10));
        getChildren()
                .add(alertIcon);
        column++;
        GridPane.setConstraints(
                alertTitle,
                column,
                row,
                1,
                1,
                HPos.LEFT,
                VPos.CENTER,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2, 2, 2, 10));
        getChildren()
                .add(alertTitle);
        column++;

        Label fillerPane = new Label();

        GridPane.setConstraints(
                fillerPane,
                column,
                row,
                1,
                1,
                HPos.LEFT,
                VPos.CENTER,
                Priority.ALWAYS,
                Priority.NEVER,
                new Insets(2, 2, 2, 10));
        getChildren()
                .add(fillerPane);
        column++;
        GridPane.setConstraints(
                moreDetailsButton,
                column,
                row,
                1,
                1,
                HPos.RIGHT,
                VPos.BOTTOM,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2, 2, 2, 10));
        getChildren()
                .add(moreDetailsButton);
        column = 0;
        row++;

        if (showDetails) {
            GridPane.setConstraints(
                    alertDescription,
                    column,
                    row,
                    3,
                    1,
                    HPos.LEFT,
                    VPos.TOP,
                    Priority.ALWAYS,
                    Priority.NEVER,
                    new Insets(2, 2, 2, 15));
            getChildren()
                    .add(alertDescription);
            row++;

            for (AlertResolver resolver : alert.getResolvers()) {

                Node graphic = null;
                switch (resolver.getPersistence()) {
                    case TEMPORARY:
                        graphic = Icon.TEMPORARY_FIX.makeIcon();
                        break;
                    case PERMANENT:
                        break;
                    default:
                        throw new UnsupportedOperationException("Can't handle: " + resolver.getPersistence());
                }


                Button resolverButton = new Button(resolver.getTitle(), graphic);

                resolverButton.setTooltip(new Tooltip(resolver.getDescription()));
                resolverButton.setOnAction(
                        (event) -> {
                            // not blocking since not doing a get() on the task.
                            resolver.resolve();
                        });
                GridPane.setConstraints(
                        resolverButton,
                        column,
                        row,
                        1,
                        2,
                        HPos.LEFT,
                        VPos.TOP,
                        Priority.NEVER,
                        Priority.NEVER,
                        new Insets(2, 2, 10, 15));
                getChildren()
                        .add(resolverButton);
                row++;
            }
        }
    }
}

