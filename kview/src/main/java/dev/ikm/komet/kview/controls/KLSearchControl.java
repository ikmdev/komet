package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLSearchControlSkin;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.function.Consumer;

/**
 * <p>The Search control provides a search field for the user to type any term. It can be used for any purpose, like
 * searching through a dataset of concepts (the actual search implementation should be done outside this control), and
 * includes a filter button that can be used to open a menu with filter options.
 * </p>
 * <p>The {@link SearchResult search results} that are passed to the control via {@link #resultsProperty()}, if any,
 * are shown below the search field in a {@link javafx.scene.control.ListView} with limited height. When there are no
 * results, a placeholder text is shown instead.
 * </p>
 * <p>This control can interact with a {@link KLConceptNavigatorControl}, for instance, in order to show the results
 * of a search through the dataset in the treeView.
 * </p>
 * <p>A simple implementation of this control, for a given {@link dev.ikm.komet.navigator.graph.Navigator} holding
 * a valid dataset, can be as follows:
 * <pre><code>
 *     KLSearchControl searchControl = new KLSearchControl();
 *     searchControl.setOnAction(_ -&gt; {
 *          searchControl.setResultsPlaceholder("Searching...");
 *          List&lt;ConceptFacade&gt; results = navigator.getViewCalculator().search(searchControl.getText(), 100);
 *          List&lt;KLSearchControl.SearchResult&gt; searchResults = results.stream()
 *              .map(entity -&gt;
 *                  new KLSearchControl.SearchResult(Entity.getFast(navigator.getParentNids(entity.nid())[0]),
 *                      entity, searchControl.getText())
*               .toList());
 *          searchControl.setResultsPlaceholder(null);
 *          searchControl.resultsProperty().addAll(searchResults);
 *     });
 *     searchControl.setOnLongHover(result -&gt; System.out.println("Result " + result + " was hovered"));
 *     searchControl.setOnSearchResultClick(result -&gt; System.out.println("Result " + result + " was selected"));
 *     searchControl.setOnClearSearch(_ -&gt; System.out.println("Results have been cleared"));
 * </code></pre>
 * </p>
 */
public class KLSearchControl extends Control {

    /**
     * <p>Creates a {@link KLSearchControl} instance.
     * </p>
     */
    public KLSearchControl() {

        getStyleClass().add("search-control");
        getStylesheets().add(getUserAgentStylesheet());
    }

    /**
     * The prompt text to display in the search field. If set to null (by default), the
     * default prompt text is displayed. If set to an empty string, no prompt text is displayed.
     */
    private final StringProperty promptTextProperty = new SimpleStringProperty(this, "promptText");
    public final StringProperty promptTextProperty() {
        return promptTextProperty;
    }
    public final String getPromptText() {
        return promptTextProperty.get();
    }
    public final void setPromptText(String value) {
        promptTextProperty.set(value);
    }

    /**
     * The text displayed in the search field, typed by the user, or set programmatically.
     */
    private final StringProperty textProperty = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() {
        return textProperty;
    }
    public final String getText() {
        return textProperty.get();
    }
    public final void setText(String value) {
        textProperty.set(value);
    }

    /**
     * <p>The action handler associated with this search field, or null if no action handler is assigned.
     * Typically, this can be used to call the implementation that searches the dataset with the
     * {@link #textProperty()}.
     * </p>
     * <p>When the search field is not empty, after the user presses the ENTER key, the event handler
     * is called invoking the search action, and the results area becomes visible, waiting for
     * {@link SearchResult search results} that are passed to the control via {@link #resultsProperty()}.
     * </p>
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onActionProperty = new SimpleObjectProperty<>(this, "onAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onActionProperty;
    }
    public final EventHandler<ActionEvent> getOnAction() {
        return onActionProperty.get();
    }
    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty.set(value);
    }

    /**
     * <p>The search field includes a button that when pressed by the user clears the field and closes the results area.
     * Therefore, {@link #textProperty()} and {@link #resultsProperty()} are cleared.
     * This property can be used to include an external action to be invoked right before that happens.
     * </p>
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onClearSearchProperty = new SimpleObjectProperty<>(this, "onClearSearch");
    public final ObjectProperty<EventHandler<ActionEvent>> onClearSearchProperty() {
       return onClearSearchProperty;
    }
    public final EventHandler<ActionEvent> getOnClearSearch() {
       return onClearSearchProperty.get();
    }
    public final void setOnClearSearch(EventHandler<ActionEvent> value) {
        onClearSearchProperty.set(value);
    }

    /**
     * Action to be invoked when the filter button is pressed.
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onFilterActionProperty = new SimpleObjectProperty<>(this, "onFilterAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onFilterActionProperty() {
        return onFilterActionProperty;
    }
    public final EventHandler<ActionEvent> getOnFilterAction() {
        return onFilterActionProperty.get();
    }
    public final void setOnFilterAction(EventHandler<ActionEvent> value) {
        onFilterActionProperty.set(value);
    }

    /**
     * This boolean property tracks if the filter options are set or not.
     */
    private final BooleanProperty filterSetProperty = new SimpleBooleanProperty(this, "filterSet");
    public final BooleanProperty filterSetProperty() {
        return filterSetProperty;
    }
    public final boolean isFilterSet() {
        return filterSetProperty.get();
    }
    public final void setFilterSet(boolean value) {
        filterSetProperty.set(value);
    }

    /**
     * <p>The results area becomes visible when the search action defined by {@link #onActionProperty()} starts, and
     * until the {@link SearchResult} are not available, or if {@link #resultsProperty()} is empty, this property can
     * be used to set a message in the results area.
     * </p>
     * <p>When set to null (by default), the default placeholder text is displayed. When set to an empty string,
     * no placeholder text is displayed.</p>
     */
    private final StringProperty resultsPlaceholderProperty = new SimpleStringProperty(this, "resultsPlaceholder");
    public final StringProperty resultsPlaceholderProperty() {
        return resultsPlaceholderProperty;
    }
    public final String getResultsPlaceholder() {
        return resultsPlaceholderProperty.get();
    }
    public final void setResultsPlaceholder(String value) {
        resultsPlaceholderProperty.set(value);
    }

    /**
     * <p>Double property that sets the milliseconds of activation or delay for performing an action, after
     * a "long" hovering over a search result.</p>
     * <p>The 'long-hover` event is defined as the sustained hovering over a search result that lasts at least the
     * activation milliseconds.</p>
     * <p>The default value is set to 500 ms.</p>
     */
    private final DoubleProperty activationProperty = new SimpleDoubleProperty(this, "activation", 500);
    public final DoubleProperty activationProperty() {
        return activationProperty;
    }
    public final double getActivation() {
        return activationProperty.get();
    }
    public final void setActivation(double value) {
        activationProperty.set(value);
    }

    /**
     * <p>This property sets a consumer that accepts a {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem},
     * so when a long-hover event occurs over a {@link SearchResult}, the action defined for such consumer can be
     * performed for it.
     * </p>
     */
    private final ObjectProperty<Consumer<InvertedTree.ConceptItem>> onLongHoverProperty = new SimpleObjectProperty<>(this, "onLongHover");
    public final ObjectProperty<Consumer<InvertedTree.ConceptItem>> onLongHoverProperty() {
       return onLongHoverProperty;
    }
    public final Consumer<InvertedTree.ConceptItem> getOnLongHover() {
       return onLongHoverProperty.get();
    }
    public final void setOnLongHover(Consumer<InvertedTree.ConceptItem> value) {
        onLongHoverProperty.set(value);
    }

    /**
     * <p>This property sets a consumer that accepts a {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem},
     * so when a click event occurs over a {@link SearchResult}, the action defined for such consumer can be
     * performed for it.
     * </p>
     */
    private final ObjectProperty<Consumer<InvertedTree.ConceptItem>> onSearchResultClickProperty = new SimpleObjectProperty<>(this, "onSearchResultClick");
    public final ObjectProperty<Consumer<InvertedTree.ConceptItem>> onSearchResultClickProperty() {
       return onSearchResultClickProperty;
    }
    public final Consumer<InvertedTree.ConceptItem> getOnSearchResultClick() {
       return onSearchResultClickProperty.get();
    }
    public final void setOnSearchResultClick(Consumer<InvertedTree.ConceptItem> value) {
        onSearchResultClickProperty.set(value);
    }

    /**
     * <p>The {@link SearchResult} record holds a result after searching a dataset with a given term. This
     * result includes the {@link ConceptFacade} found, the {@link ConceptFacade} of one of its possible
     * parents, and the string that was searched for.
     * </p>
     * @param parentConcept the {@link ConceptFacade} of one of the possible parents
     * @param concept the {@link ConceptFacade} found as a result of a search in a dataset of {@link #textProperty()}
     * @param highlight the term searched, typically defined by {@link #textProperty()}.
     */
    public record SearchResult(ConceptFacade parentConcept, ConceptFacade concept, String highlight) {}

    /**
     * <p>An {@link ObservableList<SearchResult>} that holds the {@link SearchResult} of a search performed
     * through a dataset.
     * </p>
     * <p>This list of items defines the underlying data model a {@link javafx.scene.control.ListView} that
     * is shown in the results area, with limited height.</p>
     */
    private final ObservableList<SearchResult> resultsProperty = FXCollections.observableArrayList();
    public final ObservableList<SearchResult> resultsProperty() {
        return resultsProperty;
    }

    /** {@inheritDoc} **/
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLSearchControlSkin(this);
    }

    /** {@inheritDoc} **/
    @Override
    public String getUserAgentStylesheet() {
        return KLSearchControl.class.getResource("search-control.css").toExternalForm();
    }
}