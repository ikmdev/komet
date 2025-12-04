package dev.ikm.komet.kview.fxutils;

import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

import static javafx.scene.shape.FillRule.EVEN_ODD;
import static javafx.scene.shape.FillRule.NON_ZERO;

/**
 * Utility class providing factory methods for creating SVG icons as JavaFX SVGPath objects.
 */
public interface IconsHelper {

    /**
     * Enum representing available SVG icons with their path data and default fill rules.
     */
    enum IconType {
        /**
         * Attachment icon (paperclip)
         */
        ATTACHMENT("M2.32011 11.5752C0.906855 10.162 0.906855 7.87464 2.32011 6.46139L7.55018 1.23131C8.58738 0.194111 10.2676 0.194111 11.3048 1.23131C12.342 2.26851 12.342 3.94873 11.3048 4.98593L6.98088 9.30986C6.31974 9.971 5.24664 9.971 4.58549 9.30986C3.92434 8.64871 3.92434 7.57561 4.58549 6.91446L8.52134 2.97861L9.33097 3.78824L5.39512 7.72409C5.18183 7.93738 5.18183 8.28694 5.39512 8.50023C5.60841 8.71352 5.95797 8.71352 6.17126 8.50023L10.4952 4.17631C11.0845 3.58696 11.0845 2.63028 10.4952 2.04094C9.90583 1.4516 8.94915 1.4516 8.35981 2.04094L3.12974 7.27101C2.16434 8.23641 2.16434 9.80021 3.12974 10.7656C4.09513 11.731 5.65894 11.731 6.62433 10.7656L11.4663 5.92361L12.276 6.73323L7.43396 11.5752C6.02071 12.9885 3.73336 12.9885 2.32011 11.5752Z",
                EVEN_ODD),

        /**
         * Comments/chat bubble icon
         */
        COMMENTS("M5.59212 12.8334C5.43741 12.8334 5.28904 12.772 5.17964 12.6626C5.07025 12.5532 5.00879 12.4048 5.00879 12.2501V10.5001H2.67546C2.36604 10.5001 2.06929 10.3772 1.8505 10.1584C1.63171 9.93958 1.50879 9.64283 1.50879 9.33341V2.33341C1.50879 1.68591 2.03379 1.16675 2.67546 1.16675H12.0088C12.3182 1.16675 12.615 1.28966 12.8337 1.50846C13.0525 1.72725 13.1755 2.024 13.1755 2.33341V9.33341C13.1755 9.64283 13.0525 9.93958 12.8337 10.1584C12.615 10.3772 12.3182 10.5001 12.0088 10.5001H8.45046L6.29212 12.6642C6.17546 12.7751 6.02962 12.8334 5.88379 12.8334H5.59212ZM6.17546 9.33341V11.1301L7.97212 9.33341H12.0088V2.33341H2.67546V9.33341H6.17546Z",
                NON_ZERO);

        /**
         * The SVG path data string for the icon
         */
        private final String pathData;

        /**
         * The fill rule to apply when rendering the icon
         */
        private final FillRule fillRule;

        /**
         * Constructs an IconType with specific SVG path data and fill rule.
         *
         * @param pathData The SVG path data string for the icon
         * @param fillRule The fill rule to apply when rendering the icon
         */
        IconType(String pathData, FillRule fillRule) {
            this.pathData = pathData;
            this.fillRule = fillRule;
        }

        /**
         * Gets the SVG path data string for this icon type.
         *
         * @return The SVG path data string
         */
        public String getPathData() {
            return pathData;
        }

        /**
         * Gets the fill rule to be applied when rendering this icon type.
         *
         * @return The fill rule (EVEN_ODD or NON_ZERO)
         */
        public FillRule getFillRule() {
            return fillRule;
        }
    }

    /**
     * Creates an SVGPath with the specified path data, fill rule, and color.
     *
     * @param pathData  The SVG path data string defining the icon shape
     * @param fillRule  The fill rule to apply (NON_ZERO or EVEN_ODD); if null, NON_ZERO will be used
     * @param fillColor The color to fill the path with; if null, BLACK will be used
     * @return A new SVGPath instance configured with the specified parameters
     * @throws IllegalArgumentException if pathData is null or empty
     */
    static SVGPath createIcon(String pathData, FillRule fillRule, Color fillColor) {
        if (pathData == null || pathData.isEmpty()) {
            throw new IllegalArgumentException("SVG path data cannot be null or empty");
        }

        SVGPath svgPath = new SVGPath();
        svgPath.setContent(pathData);
        svgPath.setFillRule(fillRule != null ? fillRule : NON_ZERO);
        svgPath.setFill(fillColor != null ? fillColor : Color.BLACK);
        return svgPath;
    }

    /**
     * Creates an SVGPath with the specified path data and color, using NON_ZERO fill rule.
     *
     * @param pathData  The SVG path data string defining the icon shape
     * @param fillColor The color to fill the path with; if null, BLACK will be used as default
     * @return Configured SVGPath object ready to be added to a JavaFX scene
     * @throws IllegalArgumentException if pathData is null or empty (checked in the delegated method)
     * @see #createIcon(String, FillRule, Color) The delegated method that performs the actual creation
     */
    static SVGPath createIcon(String pathData, Color fillColor) {
        return createIcon(pathData, NON_ZERO, fillColor);
    }

    /**
     * Creates an SVGPath from a predefined icon type with the specified color.
     *
     * @param iconType  The type of icon to create from the predefined set
     * @param fillColor The color to fill the path with; if null, BLACK will be used as default
     * @return Configured SVGPath object ready to be added to a JavaFX scene
     * @throws IllegalArgumentException if iconType is null
     * @see #createIcon(String, FillRule, Color) The delegated method that performs the actual creation
     */
    static SVGPath createIcon(IconType iconType, Color fillColor) {
        if (iconType == null) {
            throw new IllegalArgumentException("Icon type cannot be null");
        }
        return createIcon(iconType.getPathData(), iconType.getFillRule(), fillColor);
    }

    /**
     * Creates an SVGPath from a predefined icon type with the default black color.
     *
     * @param iconType The type of icon to create from the predefined set
     * @return Configured SVGPath object with BLACK fill color and the icon type's default fill rule
     * @throws IllegalArgumentException if iconType is null
     * @see #createIcon(IconType, Color) The delegated method that performs the actual creation
     */
    static SVGPath createIcon(IconType iconType) {
        return createIcon(iconType, Color.BLACK);
    }
}
