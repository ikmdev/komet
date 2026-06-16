/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.layout.controls;

import dev.ikm.komet.framework.view.ViewProperties;
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
 * Wires a window's coordinates menu button to the relocated {@link FilterOptionsPopup} for editing the
 * view coordinate (ike-issues#661/#684). It lives in {@code knowledge-layout} so BOTH kview chapter
 * windows and the KL-engine windows ({@code ViewContextMenuButtonArea}) share one implementation.
 *
 * <p>The popup's navigation source is derived directly from the window view's {@link
 * dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator} via {@link CalculatorFilterOptionsNavigator},
 * so this helper carries no dependency on the {@code navigator} module.
 */
public final class ViewOptionsPopupHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ViewOptionsPopupHelper.class);

    /** Pseudo-class set on the menu button when the popup's options differ from the inherited defaults. */
    public static final PseudoClass FILTER_SET = PseudoClass.getPseudoClass("filter-set");
    /** Pseudo-class set on the menu button while the popup is showing. */
    public static final PseudoClass FILTER_SHOWING = PseudoClass.getPseudoClass("filter-showing");

    private ViewOptionsPopupHelper() {
    }

    /**
     * Sets up the View Options {@link FilterOptionsPopup} for a window's coordinates menu button.
     *
     * <p>The popup reads its inherited baseline from {@code viewProperties.parentView()} (a NoOverride)
     * and writes edits to {@code viewProperties.nodeView()} (a WithOverride); the menu button press
     * toggles the popup, flanking the owning pane on whichever side has room.
     *
     * @param viewProperties            the window's view properties (parentView = inherited baseline,
     *                                  nodeView = the editable override the popup writes)
     * @param filterType                the popup filter type
     * @param owningChapWindowBorderPane the pane the popup is positioned against
     * @param coordinatesMenuButton     the menu button that toggles the popup
     * @param updateViewBlock           the host's view-refresh routine, run on each coordinate change
     * @return the configured {@link FilterOptionsPopup}
     */
    public static FilterOptionsPopup setupViewCoordinateOptionsPopup(ViewProperties viewProperties,
                                                                     FilterOptionsPopup.FILTER_TYPE filterType,
                                                                     Pane owningChapWindowBorderPane,
                                                                     MenuButton coordinatesMenuButton,
                                                                     Runnable updateViewBlock) {
        // Filter Options Popup for the coordinates menu button.
        FilterOptionsPopup filterOptionsPopup = new FilterOptionsPopup(filterType, viewProperties.parentView());

        // Bind the popup's filter options to the view model's filter options. Update details if options change.
        viewProperties.nodeView().subscribe((_, _) -> {
            filterOptionsPopup.setNavigator(new CalculatorFilterOptionsNavigator(viewProperties.nodeView().calculator()));
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
                filterOptionsPopup.setNavigator(new CalculatorFilterOptionsNavigator(viewProperties.nodeView().calculator()));
            }
            if (e.getButton() == MouseButton.PRIMARY) {
                if (filterOptionsPopup.isShowing()) {
                    e.consume();
                    filterOptionsPopup.hide();
                } else {
                    // Flank the popup on whichever side of the owning window has room on its screen.
                    Bounds chapterWindowBounds = owningChapWindowBorderPane.localToScreen(owningChapWindowBorderPane.getLayoutBounds());
                    Screen screenChapWindowIsIn = Screen.getScreens().stream().filter(screen -> {
                        double screenMinX = screen.getBounds().getMinX();
                        double screenMaxX = screen.getBounds().getMaxX();
                        double conceptWindowX = chapterWindowBounds.getMinX();
                        return conceptWindowX >= screenMinX && conceptWindowX <= screenMaxX;
                    }).findFirst().orElse(Screen.getPrimary());
                    if (screenChapWindowIsIn != null) {
                        LOG.debug(" Chapter window is on screen with bounds: {}", screenChapWindowIsIn.getBounds());
                        final double popupWidth = filterOptionsPopup.getWidth() == 0.0d ? 326.0 : filterOptionsPopup.getWidth();
                        double distanceFromWindowLeftToScreenLeft = chapterWindowBounds.getMinX() - screenChapWindowIsIn.getBounds().getMinX();
                        filterOptionsPopup.setAutoFix(false);
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
