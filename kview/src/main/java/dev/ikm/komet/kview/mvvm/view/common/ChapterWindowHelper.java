package dev.ikm.komet.kview.mvvm.view.common;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.controls.FilterOptionsPopup;
import dev.ikm.komet.layout.controls.ViewOptionsPopupHelper;
import dev.ikm.komet.layout.KlView;
import dev.ikm.komet.layout.context.KlContext;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.Pane;

/**
 * Helper class for chapter window related UI components. Currently, will help set up
 * the filter options popup for view coordinates in chapter windows.
 */
public class ChapterWindowHelper {

    public static final PseudoClass FILTER_SET = PseudoClass.getPseudoClass("filter-set");
    public static final PseudoClass FILTER_SHOWING = PseudoClass.getPseudoClass("filter-showing");

    /**
     * Sets up the filter options popup for view coordinates in chapter windows. Delegates to the
     * shared {@link ViewOptionsPopupHelper} in {@code knowledge-layout} so kview chapter windows and
     * the KL-engine windows use one implementation (ike-issues#661/#684).
     *
     * @param viewProperties               The ViewProperties containing view coordinates and view calculators.
     * @param filterType                   The type of filter for the popup.
     * @param owningChapWindowBorderPane   The BorderPane that owns the chapter window.
     * @param coordinatesMenuButton        The MenuButton to trigger the popup.
     * @param updateViewBlock              A Runnable to update the view when filter options change.
     * @return The configured FilterOptionsPopup.
     */
    public static FilterOptionsPopup setupViewCoordinateOptionsPopup(ViewProperties viewProperties,
                                                                     FilterOptionsPopup.FILTER_TYPE filterType,
                                                                     Pane owningChapWindowBorderPane,
                                                                     MenuButton coordinatesMenuButton,
                                                                     Runnable updateViewBlock) {
        return ViewOptionsPopupHelper.setupViewCoordinateOptionsPopup(viewProperties, filterType,
                owningChapWindowBorderPane, coordinatesMenuButton, updateViewBlock);
    }

    /**
     * Wires a chapter window's coordinates menu button to the window's KL {@link KlContext} instead of
     * the {@code FilterOptionsPopup} (ike-issues#660/#661). On show, the button is populated from
     * the context resolved at {@code contextAnchor}'s position in the scene graph; editing the menu
     * drives the context's source view, so KL areas re-render via {@code contextChanged()}. The header
     * (and other non-area FXML content) follows the same coordinate: the derived {@code ViewProperties}'
     * node view tracks the context's source view, so {@code updateViewBlock} re-runs when it changes.
     *
     * <p>Retained as the interim fallback while the popup wiring (ike-issues#661) is verified.
     *
     * @param coordinatesMenuButton the menu button to populate
     * @param contextAnchor         a node within the window, used to resolve the window's context
     * @param viewProperties        the window's (derived) view properties, watched to refresh the header
     * @param updateViewBlock       the host's view-refresh routine, run on each coordinate change
     */
    public static void setupViewContextMenu(MenuButton coordinatesMenuButton, Node contextAnchor,
                                            ViewProperties viewProperties, Runnable updateViewBlock) {
        coordinatesMenuButton.setOnShowing(event -> {
            KlContext context = KlView.context(contextAnchor);
            if (context != null) {
                coordinatesMenuButton.getItems().setAll(context.viewMenu());
            }
        });
        coordinatesMenuButton.setOnHidden(event -> coordinatesMenuButton.getItems().clear());
        if (viewProperties != null) {
            viewProperties.nodeView().subscribe((oldView, newView) -> updateViewBlock.run());
        }
    }
}
