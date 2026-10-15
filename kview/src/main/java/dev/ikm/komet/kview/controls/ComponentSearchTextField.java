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

import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A search text field for a component: the KL component controls' inline (type-ahead) field — typing
 * suggests components by name, or by UUID — framed with a magnifier on the left and a filter
 * glyph on the right. The field keeps the suggestion the user last chose as its value;
 * {@link #searchedComponentProperty()} is that component while the field shows text, and null
 * once the text is cleared, so a caller filters on it and widens again when it goes null.
 * <p>
 * The box owns its field. What makes the field search is set on the box, as on
 * {@link KLComponentControl}: the {@link #completerProperty() completer} that finds components
 * for the typed text, the {@link #converterProperty() converter} that names them, and the
 * {@link #suggestionsCellFactoryProperty() cell factory} that draws them in the popup.
 * {@link KLComponentControlFactory#createComponentSearchTextField} fills in the component controls' own.
 * A {@link #suggestionFilterProperty() suggestion filter} then keeps only the suggestions worth
 * picking in the place the box is used.
 * <p>
 * Styled by kview.css under {@code .component-search-text-field}, its suggestions popup under
 * {@code .component-search-popup}. The box lays out its field and glyphs itself — the field over
 * its whole content area, the glyphs inside the field's edges — and keeps its minimum height at
 * its preferred height, so a parent short of room never squashes it.
 */
public class ComponentSearchTextField extends Region {

    /** The glyphs' distance from the field's left and right edges. */
    private static final double GLYPH_INSET_LEFT = 10;
    private static final double GLYPH_INSET_RIGHT = 9;

    /** The popup's row height, as the component controls set it. */
    private static final double SUGGESTION_ROW_HEIGHT = 41;

    private final AutoCompleteTextField<EntityProxy> searchField = new AutoCompleteTextField<>();
    private final Region searchIcon = new Region();
    private final Region filterIcon = new Region();

    private final ReadOnlyObjectWrapper<EntityProxy> searchedComponent =
            new ReadOnlyObjectWrapper<>(this, "searchedComponent");

    public ComponentSearchTextField() {
        searchField.getPopupStyleClasses().addAll("component-popup", "component-search-popup");
        searchField.setSuggestionsNodeHeight(SUGGESTION_ROW_HEIGHT);
        searchField.converterProperty().bind(converter);
        searchField.suggestionsCellFactoryProperty().bind(suggestionsCellFactory);
        // The field searches with the completer, narrowed by the filter when there is one.
        completer.subscribe(() -> updateFieldCompleter());
        suggestionFilter.subscribe(() -> updateFieldCompleter());
        updateFieldCompleter();

        searchIcon.getStyleClass().add("search-icon");
        filterIcon.getStyleClass().add("filter-icon");
        getChildren().addAll(searchField, searchIcon, filterIcon);
        getStyleClass().add("component-search-text-field");

        searchField.valueProperty().subscribe(() -> updateSearchedComponent());
        searchField.textProperty().subscribe(() -> updateSearchedComponent());
    }

    private void updateFieldCompleter() {
        Function<String, List<EntityProxy>> completer = getCompleter();
        Predicate<EntityProxy> filter = getSuggestionFilter();
        if (completer == null) {
            searchField.setCompleter(null);
        } else if (filter == null) {
            searchField.setCompleter(completer);
        } else {
            searchField.setCompleter(text -> completer.apply(text).stream().filter(filter).toList());
        }
    }

    private void updateSearchedComponent() {
        searchedComponent.set(searchField.getText().isEmpty() ? null : searchField.getValue());
    }

    /** The prompt shown while the field is empty. */
    public void setPromptText(String promptText) {
        searchField.setPromptText(promptText);
    }

    /***************************************************************************
     *                                                                         *
     * Layout                                                                  *
     *                                                                         *
     **************************************************************************/

    @Override
    protected void layoutChildren() {
        double x = snappedLeftInset();
        double y = snappedTopInset();
        double width = getWidth() - x - snappedRightInset();
        double height = getHeight() - y - snappedBottomInset();
        searchField.resizeRelocate(x, y, width, height);

        double searchIconWidth = glyphWidth(searchIcon);
        double searchIconHeight = glyphHeight(searchIcon);
        searchIcon.resizeRelocate(x + GLYPH_INSET_LEFT, y + (height - searchIconHeight) / 2,
                searchIconWidth, searchIconHeight);

        double filterIconWidth = glyphWidth(filterIcon);
        double filterIconHeight = glyphHeight(filterIcon);
        filterIcon.resizeRelocate(x + width - GLYPH_INSET_RIGHT - filterIconWidth,
                y + (height - filterIconHeight) / 2, filterIconWidth, filterIconHeight);
    }

    /*
     * A glyph is a bare Region shaped and sized by the stylesheet's min and max sizes alone, so its
     * preferred size is nothing: its size is its preferred size held within those bounds, as a
     * layout pane would hold it.
     */
    private static double glyphWidth(Region glyph) {
        return Math.min(Math.max(glyph.prefWidth(-1), glyph.minWidth(-1)), glyph.maxWidth(-1));
    }

    private static double glyphHeight(Region glyph) {
        return Math.min(Math.max(glyph.prefHeight(-1), glyph.minHeight(-1)), glyph.maxHeight(-1));
    }

    @Override
    protected double computePrefWidth(double height) {
        return snappedLeftInset() + searchField.prefWidth(height) + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double width) {
        return snappedTopInset() + searchField.prefHeight(width) + snappedBottomInset();
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + searchField.minWidth(height) + snappedRightInset();
    }

    /** The preferred height: the box never gives up height to a sibling. */
    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected double computeMaxHeight(double width) {
        return computePrefHeight(width);
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- completer
    /**
     * Finds the components to suggest for the typed text. Runs on the field's own search thread,
     * not the FX thread.
     */
    private final ObjectProperty<Function<String, List<EntityProxy>>> completer = new SimpleObjectProperty<>(this, "completer");
    public final void setCompleter(Function<String, List<EntityProxy>> completer) { this.completer.set(completer); }
    public final Function<String, List<EntityProxy>> getCompleter() { return completer.get(); }
    public final ObjectProperty<Function<String, List<EntityProxy>>> completerProperty() { return completer; }

    // -- suggestion filter
    /**
     * Keeps only the suggestions worth picking, out of those the completer finds; null keeps them
     * all. Runs where the completer runs: on the field's own search thread.
     */
    private final ObjectProperty<Predicate<EntityProxy>> suggestionFilter = new SimpleObjectProperty<>(this, "suggestionFilter");
    public final void setSuggestionFilter(Predicate<EntityProxy> filter) { suggestionFilter.set(filter); }
    public final Predicate<EntityProxy> getSuggestionFilter() { return suggestionFilter.get(); }
    public final ObjectProperty<Predicate<EntityProxy>> suggestionFilterProperty() { return suggestionFilter; }

    // -- converter
    /** Names a component, in the field once chosen and in the popup's default cells. */
    private final ObjectProperty<StringConverter<EntityProxy>> converter = new SimpleObjectProperty<>(this, "converter");
    public final void setConverter(StringConverter<EntityProxy> converter) { this.converter.set(converter); }
    public final StringConverter<EntityProxy> getConverter() { return converter.get(); }
    public final ObjectProperty<StringConverter<EntityProxy>> converterProperty() { return converter; }

    // -- suggestions cell factory
    /** Draws a suggestion in the popup; null leaves the field's default cell, the converter's text. */
    private final ObjectProperty<Callback<ListView<EntityProxy>, ListCell<EntityProxy>>> suggestionsCellFactory =
            new SimpleObjectProperty<>(this, "suggestionsCellFactory");
    public final void setSuggestionsCellFactory(Callback<ListView<EntityProxy>, ListCell<EntityProxy>> factory) { suggestionsCellFactory.set(factory); }
    public final Callback<ListView<EntityProxy>, ListCell<EntityProxy>> getSuggestionsCellFactory() { return suggestionsCellFactory.get(); }
    public final ObjectProperty<Callback<ListView<EntityProxy>, ListCell<EntityProxy>>> suggestionsCellFactoryProperty() { return suggestionsCellFactory; }

    // -- searched component
    /**
     * The component to search for: the suggestion last chosen, while the field shows text; null
     * while the field is empty.
     */
    public ReadOnlyObjectProperty<EntityProxy> searchedComponentProperty() { return searchedComponent.getReadOnlyProperty(); }
    public EntityProxy getSearchedComponent() { return searchedComponent.get(); }
}
