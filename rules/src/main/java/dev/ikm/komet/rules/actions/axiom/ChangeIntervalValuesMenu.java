package dev.ikm.komet.rules.actions.axiom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ikm.elk.snomed.interval.Interval;
import dev.ikm.komet.framework.panel.axiom.AxiomSubjectRecord;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.ext.lang.owl.IntervalUtil;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * The SetValueMenu class is a menu component that provides options for setting
 * different types of values (Boolean, Integer, Decimal, String) on a specified
 * axiom subject record. It dynamically creates menu items to handle these
 * value-setting operations and associates the necessary event handlers to
 * process the respective value types.
 *
 * This menu is tied to a specific AxiomSubjectRecord and utilizes a
 * ViewCalculator along with an EditCoordinate to perform the operations.
 */
public class ChangeIntervalValuesMenu extends Menu {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ChangeIntervalValuesMenu.class);

	/**
	 * Constructs a new ChangeIntervalValuesMenu, which provides options for setting
	 * the values open, closed, and value bounds for an interval
	 * {@code AxiomSubjectRecord}. Each option in the menu dynamically associates
	 * the corresponding action to handle the specific value type.
	 *
	 * @param s                  The name of the menu.
	 * @param viewCalculator     The {@code ViewCalculator} responsible for
	 *                           calculating and providing necessary data views for
	 *                           value-setting operations.
	 * @param editCoordinate     The {@code EditCoordinate} defining the editing
	 *                           context, paths, modules, and authoring details for
	 *                           the operations.
	 * @param axiomSubjectRecord The {@code AxiomSubjectRecord} representing the
	 *                           subject and context upon which the value-setting
	 *                           operations will be performed.
	 */
	public ChangeIntervalValuesMenu(String s, final ViewCalculator viewCalculator, final EditCoordinate editCoordinate,
			AxiomSubjectRecord axiomSubjectRecord) {
		super(s);
		Interval interval = IntervalUtil.makeInterval(axiomSubjectRecord.getAxiomVertex());
		MenuItem setLowerBoundOpenMenuItem = new MenuItem(
				"Set Lower Bound " + (interval.isLowerOpen() ? "Closed" : "Open"));
		setLowerBoundOpenMenuItem.setOnAction(
				new ChangeIntervalBoundOpen(interval, true, s, axiomSubjectRecord, viewCalculator, editCoordinate));
		MenuItem setLowerBoundMenuItem = new MenuItem("Set Lower Bound");
		setLowerBoundMenuItem.setOnAction(
				new ChangeIntervalBound(interval, true, s, axiomSubjectRecord, viewCalculator, editCoordinate));
		MenuItem setUpperBoundMenuItem = new MenuItem("Set Upper Bound");
		setUpperBoundMenuItem.setOnAction(
				new ChangeIntervalBound(interval, false, s, axiomSubjectRecord, viewCalculator, editCoordinate));
		MenuItem setUpperBoundOpenMenuItem = new MenuItem(
				"Set Upper Bound " + (interval.isUpperOpen() ? "Closed" : "Open"));
		setUpperBoundOpenMenuItem.setOnAction(
				new ChangeIntervalBoundOpen(interval, false, s, axiomSubjectRecord, viewCalculator, editCoordinate));
		getItems().addAll(setLowerBoundOpenMenuItem, setLowerBoundMenuItem, setUpperBoundMenuItem,
				setUpperBoundOpenMenuItem);
	}
}
