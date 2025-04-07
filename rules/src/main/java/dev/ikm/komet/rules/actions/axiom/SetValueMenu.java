package dev.ikm.komet.rules.actions.axiom;

import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SetValueMenu class is a menu component that provides options for setting different
 * types of values (Boolean, Integer, Decimal, String) on a specified axiom subject record.
 * It dynamically creates menu items to handle these value-setting operations and associates
 * the necessary event handlers to process the respective value types.
 *
 * This menu is tied to a specific AxiomSubjectRecord and utilizes a ViewCalculator
 * along with an EditCoordinate to perform the operations.
 */
public class SetValueMenu extends Menu {
    private static final Logger LOG = LoggerFactory.getLogger(SetValueMenu.class);
    final ViewCalculator viewCalculator;
    final AxiomSubjectRecord axiomSubjectRecord;

    /**
     * Constructs a new SetValueMenu, which provides options for setting different types of
     * values (Boolean, Integer, Decimal, String) on a specified {@code AxiomSubjectRecord}.
     * Each option in the menu dynamically associates the corresponding action to handle the
     * specific value type.
     *
     * @param s The name of the menu.
     * @param viewCalculator The {@code ViewCalculator} responsible for calculating and providing
     *                        necessary data views for value-setting operations.
     * @param editCoordinate The {@code EditCoordinate} defining the editing context, paths,
     *                        modules, and authoring details for the operations.
     * @param axiomSubjectRecord The {@code AxiomSubjectRecord} representing the subject and context
     *                            upon which the value-setting operations will be performed.
     */
    public SetValueMenu(String s, final ViewCalculator viewCalculator, final EditCoordinate editCoordinate,
                        AxiomSubjectRecord axiomSubjectRecord) {
        super(s);
        this.viewCalculator = viewCalculator;
        this.axiomSubjectRecord = axiomSubjectRecord;
        Platform.runLater(() -> {
            MenuItem setBooleanMenuItem = new MenuItem("Set Boolean");
            setBooleanMenuItem.setOnAction(new SetValueBoolean(s, axiomSubjectRecord, viewCalculator, editCoordinate));
            MenuItem setIntegerMenuItem = new MenuItem("Set Integer");
            setIntegerMenuItem.setOnAction(new SetValueInteger(s, axiomSubjectRecord, viewCalculator, editCoordinate));
            MenuItem setDecimalMenuItem = new MenuItem("Set Decimal");
            setDecimalMenuItem.setOnAction(new SetValueDecimal(s, axiomSubjectRecord, viewCalculator, editCoordinate));
            MenuItem setStringMenuItem = new MenuItem("Set String");
            setStringMenuItem.setOnAction(new SetValueString(s, axiomSubjectRecord, viewCalculator, editCoordinate));
            getItems().addAll(setBooleanMenuItem, setIntegerMenuItem, setDecimalMenuItem, setStringMenuItem);
        });
    }
}
