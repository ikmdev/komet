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
package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLWorkspaceSkin;
import dev.ikm.komet.kview.klwindows.ChapterKlWindow;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A custom JavaFX {@link Control} that represents an "infinite" workspace.
 * This control can host multiple concept windows, each arranged with configurable
 * horizontal and vertical gaps. The workspace enforces minimum and maximum size constraints
 * and uses a custom skin {@link KLWorkspaceSkin} to manage its layout.
 *
 * <p>Usage example:
 * <pre><code>
 * KLWorkspace workspace = new KLWorkspace();
 * workspace.setHorizontalGap(20.0);
 * workspace.setVerticalGap(20.0);
 *
 * // Add window to the workspace
 * workspace.getWindows().add(klWindow);
 * </code></pre>
 *
 * @see KLWorkspaceSkin
 */
public class KLWorkspace extends Control {

    /**
     * Default style class for the workspace.
     */
    public static final String DEFAULT_STYLE_CLASS = "workspace";

    /**
     * Style class used to style the "desktop pane" region of the workspace.
     */
    public static final String DESKTOP_PANE_STYLE_CLASS = "desktop-pane";

    public static final double DEFAULT_HORIZONTAL_GAP = 24.0;
    public static final double DEFAULT_VERTICAL_GAP = 24.0;
    private final ObservableList<ChapterKlWindow<Pane>> DEFAULT_WINDOWS = FXCollections.observableArrayList();

    /**
     * Number of rows for internal usage if grid-like layout is desired.
     */
    public static final int ROWS = 3;

    /**
     * Number of columns for internal usage if grid-like layout is desired.
     */
    public static final int COLUMNS = 3;

    // -- Workspace width and height constraints
    /**
     * Minimum width of the workspace.
     */
    public static final double MIN_WIDTH = 820.0;

    /**
     * Minimum height of the workspace.
     */
    public static final double MIN_HEIGHT = 528.0;

    /**
     * Maximum width of the workspace.
     */
    public static final double MAX_WIDTH = 2720.0;

    /**
     * Maximum height of the workspace.
     */
    public static final double MAX_HEIGHT = 1696.0;

    /**
     * Standard width of the workspace viewport.
     */
    public static final double STANDARD_WIDTH = 1728.0;

    /**
     * STANDARD height of the workspace viewport.
     */
    public static final double STANDARD_HEIGHT = 1080.0;

    /**
     * Minimum width of concept panels placed in the workspace.
     */
    public static final double MIN_WINDOW_WIDTH = 460.0;

    /**
     * Minimum height of concept panels placed in the workspace.
     */
    public static final double MIN_WINDOW_HEIGHT = 432.0;

    public static final double DEFAULT_WINDOW_WIDTH = 672.0;

    public static final double DEFAULT_WINDOW_HEIGHT = 1032.0;

    /**
     * Maximum height of concept panels placed in the workspace.
     */
    public static final double MAX_WINDOW_HEIGHT = 1032.0;

    /**
     * Constructs a new KLWorkspace with default style classes and settings.
     */
    public KLWorkspace() {
        initialize();
    }

    private void initialize() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    // -----------------------------------------------------------------------------------------
    // Horizontal gap
    // -----------------------------------------------------------------------------------------

    /**
     * A property representing the horizontal gap between windows.
     */
    private DoubleProperty horizontalGap;

    /**
     * Gets the current horizontal gap value.
     *
     * @return the horizontal gap
     */
    public double getHorizontalGap() {
        return (horizontalGap == null) ? DEFAULT_HORIZONTAL_GAP : horizontalGap.get();
    }

    /**
     * Sets a new horizontal gap value.
     *
     * @param value the new horizontal gap
     */
    public void setHorizontalGap(double value) {
        horizontalGapProperty().set(value);
    }

    /**
     * Returns the property object used for binding or observing the horizontal gap.
     *
     * @return the {@link DoubleProperty} for the horizontal gap
     */
    public DoubleProperty horizontalGapProperty() {
        if (horizontalGap == null) {
            horizontalGap = new StyleableDoubleProperty(DEFAULT_HORIZONTAL_GAP) {

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.HORIZONTAL_GAP;
                }

                @Override
                public Object getBean() {
                    return KLWorkspace.this;
                }

                @Override
                public String getName() {
                    return "horizontalGap";
                }
            };
        }
        return horizontalGap;
    }

    // -----------------------------------------------------------------------------------------
    // Vertical gap
    // -----------------------------------------------------------------------------------------

    /**
     * A property representing the vertical gap between windows.
     */
    private DoubleProperty verticalGap;

    /**
     * Gets the current vertical gap value.
     *
     * @return the vertical gap
     */
    public double getVerticalGap() {
        return (verticalGap == null) ? DEFAULT_VERTICAL_GAP : verticalGap.get();
    }

    /**
     * Sets a new vertical gap value.
     *
     * @param value the new vertical gap
     */
    public void setVerticalGap(double value) {
        verticalGapProperty().set(value);
    }

    /**
     * Returns the property object used for binding or observing the vertical gap.
     *
     * @return the {@link DoubleProperty} for the vertical gap
     */
    public DoubleProperty verticalGapProperty() {
        if (verticalGap == null) {
            verticalGap = new StyleableDoubleProperty(DEFAULT_VERTICAL_GAP) {

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.VERTICAL_GAP;
                }

                @Override
                public Object getBean() {
                    return KLWorkspace.this;
                }

                @Override
                public String getName() {
                    return "verticalGap";
                }
            };
        }
        return verticalGap;
    }

    // -----------------------------------------------------------------------------------------
    // Windows property
    // -----------------------------------------------------------------------------------------

    /**
     * A property that holds the list of windows in the workspace.
     */
    private ObjectProperty<ObservableList<ChapterKlWindow<Pane>>> windows;

    /**
     * Gets the list of windows contained in this workspace.
     *
     * @return an {@link ObservableList} of {@link ChapterKlWindow} objects
     */
    public ObservableList<ChapterKlWindow<Pane>> getWindows() {
        return (windows == null) ? DEFAULT_WINDOWS : windows.get();
    }

    /**
     * Sets the list of windows for this workspace. The provided list will replace
     * the current content of the workspace.
     *
     * @param value the new list of windows
     */
    public void setWindows(ObservableList<ChapterKlWindow<Pane>> value) {
        windowsProperty().set(value);
    }

    /**
     * Returns the property object used for binding or observing this workspace's windows.
     *
     * @return an {@link ObjectProperty} for the list of windows
     */
    public ObjectProperty<ObservableList<ChapterKlWindow<Pane>>> windowsProperty() {
        if (windows == null) {
            windows = new SimpleObjectProperty<>(this, "windows", DEFAULT_WINDOWS);
        }
        return windows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLWorkspaceSkin(this);
    }

    // -----------------------------------------------------------------------------------------
    // Stylesheet integration
    // -----------------------------------------------------------------------------------------

    /**
     * Nested class containing the styleable properties for the {@link KLWorkspace}.
     * It allows the framework to parse and apply CSS to the {@code KLWorkspace} control.
     */
    private static class StyleableProperties {
        private static final CssMetaData<KLWorkspace, Number> HORIZONTAL_GAP =
                new CssMetaData<>("-fx-horizontal-gap",
                        StyleConverter.getSizeConverter(), DEFAULT_HORIZONTAL_GAP) {

                    @Override
                    public Double getInitialValue(KLWorkspace node) {
                        return node.getHorizontalGap();
                    }

                    @Override
                    public boolean isSettable(KLWorkspace node) {
                        return node.horizontalGap == null || !node.horizontalGap.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(KLWorkspace node) {
                        return (StyleableProperty<Number>) node.horizontalGapProperty();
                    }
                };

        private static final CssMetaData<KLWorkspace, Number> VERTICAL_GAP =
                new CssMetaData<>("-fx-vertical-gap",
                        StyleConverter.getSizeConverter(), DEFAULT_VERTICAL_GAP) {

                    @Override
                    public Double getInitialValue(KLWorkspace node) {
                        return node.getVerticalGap();
                    }

                    @Override
                    public boolean isSettable(KLWorkspace node) {
                        return node.verticalGap == null || !node.verticalGap.isBound();
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public StyleableProperty<Number> getStyleableProperty(KLWorkspace node) {
                        return (StyleableProperty<Number>) node.verticalGapProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(StyleableProperties.HORIZONTAL_GAP);
            styleables.add(StyleableProperties.VERTICAL_GAP);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * Returns a list of all styleable CSS metadata for this class, so that the
     * JavaFX framework can apply CSS to instances of {@code KLWorkspace}.
     *
     * @return an unmodifiable {@link List} of {@link CssMetaData} objects
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLWorkspace.class.getResource("workspace.css").toExternalForm();
    }
}
