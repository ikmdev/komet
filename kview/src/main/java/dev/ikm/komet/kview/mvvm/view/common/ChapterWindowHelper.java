package dev.ikm.komet.kview.mvvm.view.common;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.komet.navigator.graph.ViewNavigator;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for chapter window related UI components. Currently, will help set up
 * the filter options popup for view coordinates in chapter windows.
 */
public class ChapterWindowHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ChapterWindowHelper.class);

    private static final PseudoClass FILTER_SET = PseudoClass.getPseudoClass("filter-set");
    private static final PseudoClass FILTER_SHOWING = PseudoClass.getPseudoClass("filter-showing");

    /**
     * Sets up the filter options popup for view coordinates in chapter windows.
     *
     * @param viewProperties               The ViewProperties containing view coordinates and view calculators.
     * @param filterType                   The type of filter for the popup.
     * @param owningChapWindowBorderPane   The BorderPane that owns the chapter window.
     * @param coordinatesMenuButton       The MenuButton to trigger the popup.
     * @param updateViewBlock              A Runnable to update the view when filter options change.
     * @return The configured FilterOptionsPopup.
     */
    public static FilterOptionsPopup setupViewCoordinateOptionsPopup(ViewProperties viewProperties,
                                                                     FilterOptionsPopup.FILTER_TYPE filterType,
                                                                     Pane owningChapWindowBorderPane,
                                                                     MenuButton coordinatesMenuButton,
                                                                     Runnable updateViewBlock) {
        // Filter Options Popup for the coordinates menu button.
        FilterOptionsPopup filterOptionsPopup = new FilterOptionsPopup(filterType);

        // Bind the popup's filter options to the view model's filter options. Update details if options change.
        viewProperties.nodeView().subscribe((_, nv) -> {
            filterOptionsPopup.setNavigator(new ViewNavigator(nv));
            updateViewBlock.run();
        });

        // Subscribe default F.O. to this nodeView, so changes from its menu are propagated to default F.O.
        // Typically, changes to nodeView can come from parentView, if the coordinate has no overrides
        filterOptionsPopup.getFilterOptionsUtils().subscribeFilterOptionsToView(
                filterOptionsPopup.getInheritedFilterOptions(), viewProperties.nodeView());

        // Subscribe nodeView to F.O., so changes from the F.O. popup are propagated to this nodeView
        filterOptionsPopup.filterOptionsProperty().subscribe((oldFilterOptions, filterOptions) -> {
            if (oldFilterOptions != null) {
                filterOptionsPopup.getFilterOptionsUtils().unsubscribeNodeFilterOptions();
            }
            if (filterOptions != null) {
                filterOptionsPopup.getFilterOptionsUtils().subscribeViewToFilterOptions(filterOptions, viewProperties.nodeView());
            }
        });

        owningChapWindowBorderPane.heightProperty().subscribe(h -> filterOptionsPopup.setStyle("-popup-pref-height: " + h));
        coordinatesMenuButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (filterOptionsPopup.getNavigator() == null) {
                Navigator navigator = new ViewNavigator(viewProperties.nodeView());
                filterOptionsPopup.setNavigator(navigator);
            }
            if (e.getButton() == MouseButton.PRIMARY) {
                if (filterOptionsPopup.isShowing()) {
                    e.consume();
                    filterOptionsPopup.hide();
                } else {

                    // Determine which side to show the popup on.
                    //   1. Detect which screen (monitor) the concept and journal window is being view on.
                    //      a. Get the bounds of the BorderPane in screen coordinates.
                    //      b. Get the bounds of each screen (monitor).
                    //      c. Find the screen (monitor) where the BorderPane's bounds intersect the screen's bounds.
                    //   2. Determine the distance from the left edge of the concept window to the left edge of the screen.
                    //      a. Get the x coordinate of the left edge of the concept window.
                    //      b. Get the x coordinate of the left edge of the screen.
                    //   3. If that distance is less than the width of the popup, show the popup on the right side of the concept window.
                    //      else show it on the left side of the concept window.
                    //      a. Get the width of the popup.
                    //      b. Compare the distance from the left edge of the concept window to the left edge of the screen with the width of the popup.
                    //      c. If the distance is less than the width of the popup, show the popup on the right side of the concept window.

                    // 1. Get the bounds of the BorderPane in screen coordinates.
                    Bounds chapterWindowBounds = owningChapWindowBorderPane.localToScreen(owningChapWindowBorderPane.getLayoutBounds());
                    // 2. Determine which screen (monitor) the concept window is being viewed on.


                    // determine which screen the concept window is on.
                    Screen screenChapWindowIsIn = Screen.getScreens().stream().filter(screen -> {
                        double screenMinX = screen.getBounds().getMinX();
                        double screenMaxX = screen.getBounds().getMaxX();
                        double conceptWindowX = chapterWindowBounds.getMinX();
                        return conceptWindowX >= screenMinX && conceptWindowX <= screenMaxX;
                    }).findFirst().orElse(Screen.getPrimary());
                    // conceptScreen should not be null.
                    if (screenChapWindowIsIn != null) {
                        LOG.debug(" Concept details area is on screen with bounds: " + screenChapWindowIsIn.getBounds());
                        // found the screen the concept window is on. conceptScreen
                        // Now determine if there is room to the left of the concept window to show the popup.

                        // 2. Determine the width of the popup.
                        final double popupWidth = filterOptionsPopup.getWidth() == 0.0d ? 326.0 : filterOptionsPopup.getWidth();
                        LOG.debug(" popupWidth: " + popupWidth);
                        double distanceFromWindowLeftToScreenLeft = chapterWindowBounds.getMinX() - screenChapWindowIsIn.getBounds().getMinX();
                        filterOptionsPopup.setAutoFix(false);
                        // 3. If that distance is less than the width of the popup, show the popup on the right side of the concept window.
                        if (distanceFromWindowLeftToScreenLeft < popupWidth) {
                            LOG.debug(" No room on left to display popup. Show on right.");
                            filterOptionsPopup.show(owningChapWindowBorderPane.getScene().getWindow(), chapterWindowBounds.getMaxX(), chapterWindowBounds.getMinY());
                        } else {
                            LOG.debug(" Room on left to display popup. Show on left.");
                            filterOptionsPopup.show(owningChapWindowBorderPane.getScene().getWindow(), chapterWindowBounds.getMinX() - popupWidth, chapterWindowBounds.getMinY());
                        }
                    }
                }
            }
        });
        filterOptionsPopup.showingProperty().subscribe(showing ->
                coordinatesMenuButton.pseudoClassStateChanged(FILTER_SHOWING, showing));

        filterOptionsPopup.defaultOptionsSetProperty().subscribe(isDefault ->
                coordinatesMenuButton.pseudoClassStateChanged(FILTER_SET, !isDefault));

        return filterOptionsPopup;
    }
}
