package dev.ikm.komet.kview.klfields;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlStringField;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Abstract base class for creating UI components that bind to an {@link ObservableField} representing a string value.
 * This class provides a common structure for string fields, including UI components such as a label indicating the
 * meaning, a tooltip for describing the purpose, and a control for displaying or editing the string value. It also
 * includes a container for these components.
 *
 * This class implements the {@link KlStringField} interface, providing the ability to retrieve the associated
 * observable field and the widget representation of the string field UI component.
 *
 * Subclasses are expected to define specific behaviors or customizations for the string control.
 */
public abstract class StringFieldAbstract implements KlStringField {
    protected final ObservableField<String> observableStringField;
    protected final ObservableView observableView;
    protected final Label meaningLabel = new Label("A string:");
    protected final Tooltip purposeTooltip = new Tooltip("The purpose of this field");
    protected final Control stringControl;
    //FIXME we need to be able to choose our control
    protected final HBox widgetContainer;

    /**
     * Constructs a StringFieldAbstract object with specified ObservableField, string Control, and ObservableView.
     * Initializes UI components, including a label for meaning, a tooltip for purpose,
     * and an HBox container with the specified Control. Sets text values based on the ObservableView's description
     * for the meaning and purpose nids of the provided ObservableField.
     *
     * @param observableStringField The observable field representing a string value, whose meaning and purpose nids
     *                              are used for setting the text of the meaning label and purpose tooltip.
     * @param stringControl The control component (e.g., TextField) used for displaying or editing the string value.
     * @param observableView The observable view used to fetch descriptions for setting up meaning and purpose texts.
     */
    public StringFieldAbstract(ObservableField<String> observableStringField, Control stringControl, ObservableView observableView) {
        this.observableStringField = observableStringField;
        this.observableView = observableView;
        //FIXME this is a temporary formatting
        this.meaningLabel.setText(observableView.getDescriptionTextOrNid(observableStringField.meaningNid()) + ": ");
        this.purposeTooltip.setText(observableView.getDescriptionTextOrNid(observableStringField.purposeNid()));
        this.stringControl = stringControl;
        //FIXME we don't want to hard code a Label or HBox here
        this.widgetContainer = new HBox(meaningLabel, stringControl);
        Tooltip.install(widgetContainer, purposeTooltip);
        // TODO: put listener on the ObservableView, and update presentation if the view changes.
    }

    @Override
    public <SGN extends Node> SGN klWidget() {
        return (SGN) widgetContainer;
    }

    @Override
    public ObservableField<String> field() {
        return observableStringField;
    }
}

