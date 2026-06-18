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
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

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
     * Sets up the View Options {@link FilterOptionsPopup} for a window's coordinates menu button — the single
     * shared implementation used by every host (chapter windows, the journal, KL-engine windows). The popup
     * reads its inherited baseline from {@code viewProperties.parentView()} (a NoOverride) and commits edits to
     * {@code viewProperties.nodeView()} (a WithOverride) only on Apply. Hosts vary only in the navigator they
     * build and where the popup is positioned, supplied as {@code navigatorFactory} and {@code showStrategy}.
     *
     * @param viewProperties   the window's view properties (parentView = inherited baseline, nodeView = the
     *                         editable override the popup writes)
     * @param filterType       the popup filter type
     * @param coordinatesMenuButton the menu button that toggles the popup
     * @param navigatorFactory builds the navigator for the popup from the view properties
     * @param showStrategy     positions and shows the popup (e.g. {@link #flankWindowShowStrategy})
     * @param updateViewBlock  the host's view-refresh routine, run on each coordinate change (may be null)
     * @return the configured {@link FilterOptionsPopup}
     */
    public static FilterOptionsPopup setupViewCoordinateOptionsPopup(ViewProperties viewProperties,
                                                                     FilterOptionsPopup.FILTER_TYPE filterType,
                                                                     ButtonBase coordinatesMenuButton,
                                                                     Function<ViewProperties, FilterOptionsNavigator> navigatorFactory,
                                                                     Consumer<FilterOptionsPopup> showStrategy,
                                                                     Runnable updateViewBlock) {
        // Filter Options Popup for the coordinates menu button.
        FilterOptionsPopup filterOptionsPopup = new FilterOptionsPopup(filterType, viewProperties.parentView());

        // Bind the popup's filter options to the view model's filter options. Update details if options change.
        viewProperties.nodeView().subscribe((_, _) -> {
            filterOptionsPopup.setNavigator(navigatorFactory.apply(viewProperties));
            if (updateViewBlock != null) {
                updateViewBlock.run();
            }
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

        // Default popup height; the show strategy may override it before showing (e.g. grow with the owning
        // window). A STABLE pref, never a live content binding, to avoid a JavaFX/macOS Metal render-target
        // crash on popup resize (ike-issues#681).
        filterOptionsPopup.setStyle("-popup-pref-height: " + 760);
        coordinatesMenuButton.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (filterOptionsPopup.getNavigator() == null) {
                filterOptionsPopup.setNavigator(navigatorFactory.apply(viewProperties));
            }
            if (e.getButton() == MouseButton.PRIMARY) {
                if (filterOptionsPopup.isShowing()) {
                    e.consume();
                    filterOptionsPopup.hide();
                } else {
                    showStrategy.accept(filterOptionsPopup);
                }
            }
        });
        filterOptionsPopup.showingProperty().subscribe(showing ->
                coordinatesMenuButton.pseudoClassStateChanged(FILTER_SHOWING, showing));

        filterOptionsPopup.defaultOptionsSetProperty().subscribe(isDefault ->
                coordinatesMenuButton.pseudoClassStateChanged(FILTER_SET, !isDefault));

        return filterOptionsPopup;
    }

    /**
     * Backward-compatible setup for the common case: a Calculator navigator and the popup flanking the owning
     * window. Used by chapter windows and KL-engine windows; the journal supplies its own navigator + positioning.
     */
    public static FilterOptionsPopup setupViewCoordinateOptionsPopup(ViewProperties viewProperties,
                                                                     FilterOptionsPopup.FILTER_TYPE filterType,
                                                                     Pane owningChapWindowBorderPane,
                                                                     ButtonBase coordinatesMenuButton,
                                                                     Runnable updateViewBlock) {
        return setupViewCoordinateOptionsPopup(viewProperties, filterType, coordinatesMenuButton,
                calculatorNavigator(), flankWindowShowStrategy(owningChapWindowBorderPane), updateViewBlock);
    }

    /** The default navigator factory: a {@link CalculatorFilterOptionsNavigator} over the view's calculator. */
    public static Function<ViewProperties, FilterOptionsNavigator> calculatorNavigator() {
        return vp -> new CalculatorFilterOptionsNavigator(vp.nodeView().calculator());
    }

    /**
     * A show strategy that flanks the popup on whichever side of the owning pane has room on its screen, sizing it
     * at least tall enough for the collapsed panes and growing with the owning window. Set as a stable pref before
     * showing — never a live binding — to avoid a JavaFX/macOS Metal render-target crash on popup resize (#681).
     */
    public static Consumer<FilterOptionsPopup> flankWindowShowStrategy(Pane owningPane) {
        return filterOptionsPopup -> {
            filterOptionsPopup.setStyle("-popup-pref-height: " + Math.max(owningPane.getHeight(), 760));
            Bounds windowBounds = owningPane.localToScreen(owningPane.getLayoutBounds());
            Screen screen = Screen.getScreens().stream().filter(s -> {
                double minX = s.getBounds().getMinX();
                double maxX = s.getBounds().getMaxX();
                double windowX = windowBounds.getMinX();
                return windowX >= minX && windowX <= maxX;
            }).findFirst().orElse(Screen.getPrimary());
            double popupWidth = filterOptionsPopup.getWidth() == 0.0d ? 326.0 : filterOptionsPopup.getWidth();
            double distanceToScreenLeft = windowBounds.getMinX() - screen.getBounds().getMinX();
            filterOptionsPopup.setAutoFix(false);
            if (distanceToScreenLeft < popupWidth) {
                filterOptionsPopup.show(owningPane.getScene().getWindow(), windowBounds.getMaxX(), windowBounds.getMinY());
            } else {
                filterOptionsPopup.show(owningPane.getScene().getWindow(), windowBounds.getMinX() - popupWidth, windowBounds.getMinY());
            }
        };
    }
}
