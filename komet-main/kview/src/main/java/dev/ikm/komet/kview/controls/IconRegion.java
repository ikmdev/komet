package dev.ikm.komet.kview.controls;

import javafx.scene.layout.Region;

/**
 * <p>IconRegion is just a convenience {@link Region} class with zero, one or more style classes set via
 * its constructor, intended mainly for nodes decorated with graphic icons set via CSS.
 * </p>
 * <p>For instance, this can be used to create a 20x20 pane decorated with a 'search' icon:
 * <pre>  IconRegion icon = new IconRegion("icon", "search");
 * StackPane pane = new StackPane(icon);
 * pane.getStyleClass().add("region");
 * </pre>
 * </p>
 * <p>with the following css:
 * <pre>  .region {
 *     -fx-pref-width: 20;
 *     -fx-pref-height: 20;
 *     -fx-background-color: transparent;
 * }
 * .region > .icon.search {
 *    -fx-scale-shape: false;
 *    -fx-background-color: gray;
 *    -fx-shape:"M10.3 9.05833L15.075 ... 5.91667 2.16667Z";
 *  }
 * </pre>
 * </p>
 */
public class IconRegion extends Region {

    /**
     * <p>Creates a new {@link Region} node, and sets its style classes from the list of styleClasses passed
     * </p>
     * @param styleClasses a list of zero, one or more strings that define the style classes to be set
     */
    public IconRegion(String... styleClasses) {
        getStyleClass().addAll(styleClasses);
    }
}
