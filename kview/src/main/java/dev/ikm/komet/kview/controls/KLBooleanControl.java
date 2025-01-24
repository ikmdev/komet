package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLBooleanControlSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Skin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * <p>KLBooleanControl is a {@link RadioButton} control with a customized skin,
 * intended for displaying and editing Boolean values. The {@link #selectedProperty()} of
 * the control is a boolean property that holds the boolean value of the control:
 * when it is selected, the value is true, and when is not selected, the value is false.
 * When a KLBooleanControl is pressed and released an {@link javafx.event.ActionEvent}
 * is sent and the application can perform some action based
 * on this event by implementing an {@link javafx.event.EventHandler}.
 *
 *  <pre><code>KLBooleanControl item = new KLBooleanControl();
 * item.setText("item");
 * item.setOnAction(e -> System.out.println("item selected: " + item.isSelected()));</code></pre>
 *
 * Alternatively, a listener can be added to the control's {@link #selectedProperty()}.</p>
 *
 * <pre><code>KLBooleanControl item = new KLBooleanControl();
 * item.setText("item");
 * item.selectedProperty().subscribe(selected ->
 *                 System.out.println("item selected: " + selected));</code></pre>
 *
 * <p>A KLBooleanControl that is not in a {@link javafx.scene.control.ToggleGroup} can be selected or deselected freely,
 * however, when several KLBooleanControls are added to the same {@link javafx.scene.control.ToggleGroup},
 * only one of those KLBooleanControl can be selected at a time. Calling {@code ToggleGroup.getSelectedToggle()}
 * returns the KLBooleanControl that has been selected, if any.
 * </p>
 *
 * <pre><code> ToggleGroup group = new ToggleGroup();
 * group.selectedToggleProperty().subscribe(toggle ->
 *                 System.out.println("item selected: " + toggle));
 * KLBooleanControl item1 = new KLBooleanControl();
 * item1.setText("item 1");
 * item1.setToggleGroup(group);
 * item1.setSelected(true);
 * KLBooleanControl button2 = new KLBooleanControl();
 * item2.setText("item 2");
 * item2.setToggleGroup(group);</code></pre>
 */
public class KLBooleanControl extends RadioButton {

    /**
      * A string property that sets the title of the control, if any
      */
    private final StringProperty titleProperty = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() {
         return titleProperty;
    }
    public final String getTitle() {
         return titleProperty.get();
    }
    public final void setTitle(String value) {
       titleProperty.set(value);
    }

    private final BooleanProperty valueProperty = new SimpleBooleanProperty(this, "false");

    public final BooleanProperty getValueProperty() {
        return valueProperty;
    }


    /**
     * Creates a KLBooleanControl with an empty string for its label.
     */
    public KLBooleanControl() {
        getStyleClass().add("boolean-control");
    }

    /**
     * Creates a KLBooleanControl with the specified text as its label.
     *
     * @param text A text string for its label.
     */
    public KLBooleanControl(String text) {
        super(text);
        getStyleClass().add("boolean-control");
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLBooleanControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLBooleanControl.class.getResource("boolean-control.css").toExternalForm();
    }
}
