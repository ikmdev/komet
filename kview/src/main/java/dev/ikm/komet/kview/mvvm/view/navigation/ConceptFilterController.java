package dev.ikm.komet.kview.mvvm.view.navigation;

import dev.ikm.komet.kview.controls.ConceptMatchList;
import dev.ikm.komet.kview.controls.ConceptFilterResult;
import dev.ikm.komet.kview.controls.ConceptFilterBar;
import dev.ikm.komet.kview.controls.ConceptFilterPicker;
import dev.ikm.komet.kview.controls.ConceptFilterPicker.Choice;
import dev.ikm.komet.kview.controls.FilteredNavigator;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.id.LongIdCollection;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import org.eclipse.collections.api.LongIterable;
import org.eclipse.collections.api.factory.primitive.LongSets;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * The concept navigator's "Show" filter: it narrows the tree to the concepts that have a semantic
 * of a chosen pattern, or only those whose semantic has a chosen value in a chosen field, keeping
 * their ancestors so each can be found at its place in the hierarchy.
 * <p>
 * It owns the row over the tree ({@link ConceptFilterBar}), the picker the row opens
 * ({@link ConceptFilterPicker}), the list of the matching concepts that can stand in for the tree
 * ({@link ConceptMatchList}) and the legend under the tree, and does the reading those leave to
 * their owner: the patterns, a pattern's fields, the values a field has, the concepts that carry
 * a value, and the names and paths of the matches. The filter narrows this navigator's tree only;
 * it is not part of the view, so windows opened from the navigator are not filtered.
 * <p>
 * Give it the navigator to narrow through {@link #baseNavigatorProperty()} and show the navigator
 * it answers with, {@link #navigatorProperty()}, in the tree. Show {@link #getMatchList()} in
 * place of the tree while {@link #listShownProperty()} is true.
 */
public class ConceptFilterController {

    /** A field with more different values than this is not listed; its value is typed instead. */
    static final int MAX_LISTED_VALUES = 500;

    /** The transparent margin around the picker in its popup, which leaves room for the picker's shadow. */
    private static final Insets POPUP_SHADOW_MARGIN = new Insets(8, 16, 22, 16);

    /**
     * A click on the pill while the picker is open closes the picker twice over: the popup hides itself on
     * the press, and the click then asks to toggle it. A toggle this soon after the popup hid itself is that click.
     */
    private static final long REOPEN_GUARD_MILLIS = 250;

    /** A path is written in full up to this many concepts; a longer one keeps its first and its last two. */
    private static final int MAX_PATH_NAMES = 3;
    private static final String PATH_SEPARATOR = " › ";
    private static final String PATH_GAP = "…";
    /** How far up a path is followed, which also ends a walk around parents that lead back to each other. */
    private static final int MAX_PATH_DEPTH = 64;
    /** The paths kept for the rows of the list that were in view lately. */
    private static final int MAX_KEPT_PATHS = 2_000;

    /** The field index of a filter that asks for the pattern alone, whatever its semantics hold. */
    private static final int ANY_FIELD = -1;

    private final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-filter");
    private final NumberFormat countFormat = NumberFormat.getIntegerInstance();

    private final ConceptFilterBar bar = new ConceptFilterBar();
    private final ConceptFilterPicker picker = new ConceptFilterPicker();
    private final Region caret = new Region();
    private final Popup pickerPopup = new Popup();
    private final ConceptMatchList matchList = new ConceptMatchList();
    private final Label legend = new Label(resources.getString("bar.legend"));

    /** The filter narrowing the tree, or null. */
    private Spec appliedSpec;
    /** What the tree shows while it is filtered: the filter result and the navigator it narrows. Null while it is not. */
    private ConceptFilterResult shownResult;
    private Navigator shownBase;
    /** Whether the list holds, or is being given, the matches of the shown result. */
    private boolean matchesRead;
    private final Map<Long, String> keptPaths = new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
            return size() > MAX_KEPT_PATHS;
        }
    };
    /** What the picker's current choices would show, worked out ahead of Apply. */
    private Spec pendingSpec;
    private ConceptFilterResult pendingResult;
    private Navigator pendingBase;
    /** The values of the field chosen in the picker, or null while they are unread or out of date. */
    private ValueIndex valueIndex;
    /** The components that have a semantic of the pattern chosen in the picker, or null while they are unread. */
    private MutableLongSet patternComponents;
    private boolean choicesOutOfDate;
    /** Set while the fields of a newly chosen pattern are put in the picker, which moves the chosen field about. */
    private boolean showingFields;
    private long autoHiddenAt;

    /** Each background read carries the number of its kind at the time; a result that is no longer the latest is dropped. */
    private int valuesRun;
    private int patternRun;
    private int pendingRun;
    private int applyRun;
    private int matchesRun;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public ConceptFilterController() {
        bar.setOnPillAction(this::togglePicker);
        bar.setOnClearAction(this::clear);
        bar.listShownProperty().subscribe(() -> showListOrTree());

        matchList.setPathProvider(match -> pathOf(match.nid()));
        matchList.setOnShowInTree(match -> {
            showTree();
            if (getOnShowInTree() != null) {
                getOnShowInTree().accept(match.nid());
            }
        });

        caret.getStyleClass().add("picker-caret");
        StackPane popupRoot = new StackPane(picker, caret);
        StackPane.setAlignment(caret, Pos.TOP_LEFT);
        popupRoot.getStyleClass().add("concept-filter-popup");
        popupRoot.setPadding(POPUP_SHADOW_MARGIN);
        popupRoot.getStylesheets().add(ConceptFilterBar.class.getResource("concept-filter.css").toExternalForm());
        pickerPopup.getContent().add(popupRoot);
        pickerPopup.setAutoHide(true);
        pickerPopup.setHideOnEscape(true);
        pickerPopup.setOnAutoHide(_ -> autoHiddenAt = System.currentTimeMillis());
        pickerPopup.showingProperty().subscribe(bar::setPickerShowing);

        picker.setOnClose(pickerPopup::hide);
        picker.setOnApply(this::apply);
        picker.setOnClear(() -> {
            clear();
            pickerPopup.hide();
        });
        picker.selectedPatternProperty().subscribe(this::showFieldsOf);
        picker.selectedFieldIndexProperty().subscribe(() -> {
            if (!showingFields) {
                readValues();
            }
        });
        picker.narrowedProperty().subscribe(() -> readValues());
        picker.selectedValueProperty().subscribe(() -> updatePending());
        picker.valueTextProperty().subscribe(() -> updatePending());
        picker.setValuesMessage(resources.getString("picker.values.choose"));

        Region swatch = new Region();
        swatch.getStyleClass().add("filter-legend-swatch");
        legend.setGraphic(swatch);
        legend.getStyleClass().add("concept-filter-legend");
        legend.getStylesheets().add(ConceptFilterBar.class.getResource("concept-filter.css").toExternalForm());
        legend.setMaxWidth(Double.MAX_VALUE);
        legend.visibleProperty().bind(filtered.and(listShown.not()));
        legend.managedProperty().bind(legend.visibleProperty());
        filtered.subscribe(set -> searchPrompt.set(set ? resources.getString("search.prompt") : null));

        baseNavigator.subscribe(() -> baseNavigatorChanged());
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * The row that names the filter and opens the picker, to place between the search box and the tree.
     */
    public ConceptFilterBar getBar() {
        return bar;
    }

    /**
     * The line that explains the dashed rows, to place under the tree. It shows only while the
     * tree is filtered.
     */
    public Label getLegend() {
        return legend;
    }

    /**
     * The matching concepts as a flat list, to show in place of the tree while
     * {@link #listShownProperty()} is true.
     */
    public ConceptMatchList getMatchList() {
        return matchList;
    }

    /**
     * Goes back to the tree when the list of matching concepts is shown, for something that is
     * about to point at a concept in the tree.
     */
    public void showTree() {
        bar.setListShown(false);
    }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    /** Opens the picker under the pill, its pointer under the pill's middle, or closes it. */
    private void togglePicker() {
        if (pickerPopup.isShowing()) {
            pickerPopup.hide();
            return;
        }
        if (getBaseNavigator() == null || System.currentTimeMillis() - autoHiddenAt < REOPEN_GUARD_MILLIS) {
            return;
        }
        if (picker.getPatterns().isEmpty()) {
            readPatterns();
        }
        Region pill = bar.getPill();
        Bounds pillOnScreen = pill.localToScreen(pill.getBoundsInLocal());
        caret.setTranslateX(Math.round(pillOnScreen.getWidth() / 2 - 5.5));
        caret.setTranslateY(-5.5);
        pickerPopup.show(bar.getScene().getWindow(),
                pillOnScreen.getMinX() - POPUP_SHADOW_MARGIN.getLeft(),
                pillOnScreen.getMaxY() + 8 - POPUP_SHADOW_MARGIN.getTop());
        if (choicesOutOfDate) {
            readValues();
        }
        picker.focusNextChoice();
    }

    private void readPatterns() {
        ViewCalculator calculator = getBaseNavigator().getViewCalculator();
        TinkExecutor.threadPool().execute(() -> {
            List<Choice> choices = new ArrayList<>();
            EntityService.get().forEachPatternEntity(pattern ->
                    choices.add(new Choice(pattern.nid(), calculator.getDescriptionTextOrNid(pattern.nid()), -1)));
            choices.sort(Comparator.comparing(Choice::label, String.CASE_INSENSITIVE_ORDER));
            Platform.runLater(() -> picker.getPatterns().setAll(choices));
        });
    }

    /** The chosen pattern's fields, named by their meanings, with the first one chosen. */
    private void showFieldsOf(Choice pattern) {
        List<String> names = new ArrayList<>();
        if (pattern != null && getBaseNavigator() != null) {
            ViewCalculator calculator = getBaseNavigator().getViewCalculator();
            calculator.latestPatternEntityVersion((Long) pattern.key()).ifPresent(version ->
                    version.fieldDefinitions().forEach(field ->
                            names.add(calculator.getDescriptionTextOrNid(field.meaningNid()))));
        }
        showingFields = true;
        picker.getFields().setAll(names);
        picker.setSelectedFieldIndex(names.isEmpty() ? -1 : 0);
        showingFields = false;
        patternComponents = null;
        patternRun++;
        readValues();
    }

    /** Whether the picker asks for a field value, as opposed to the pattern alone. */
    private boolean isNarrowing() {
        return picker.isNarrowed() && picker.getSelectedFieldIndex() >= 0;
    }

    /**
     * Reads, off the JavaFX thread, the values the chosen field has, and lists them by how many
     * components carry each. While the picker asks for the pattern alone there are no values to
     * read, and only what the pattern would show is worked out.
     */
    private void readValues() {
        int run = ++valuesRun;
        valueIndex = null;
        choicesOutOfDate = false;
        picker.setSelectedValue(null);
        picker.setValueText("");
        picker.getValues().clear();
        Choice pattern = picker.getSelectedPattern();
        int fieldIndex = picker.getSelectedFieldIndex();
        if (pattern == null || !isNarrowing() || getBaseNavigator() == null) {
            picker.setValuesMessage(resources.getString("picker.values.choose"));
            updatePending();
            return;
        }
        picker.setValuesMessage(resources.getString("picker.values.reading"));
        updatePending();
        ViewCalculator calculator = getBaseNavigator().getViewCalculator();
        long patternNid = (Long) pattern.key();
        TinkExecutor.threadPool().execute(() -> {
            ValueIndex index = readValueIndex(calculator, patternNid, fieldIndex);
            List<Choice> choices = new ArrayList<>();
            index.componentsByValue().forEach((key, components) ->
                    choices.add(new Choice(key, labelOf(key, calculator), components.size())));
            choices.sort(Comparator.<Choice>comparingLong(Choice::count).reversed()
                    .thenComparing(Choice::label, String.CASE_INSENSITIVE_ORDER));
            Platform.runLater(() -> {
                if (run == valuesRun) {
                    showValues(index, choices);
                }
            });
        });
    }

    private void showValues(ValueIndex index, List<Choice> choices) {
        valueIndex = index;
        if (index.tooMany()) {
            picker.setValuesMessage(MessageFormat.format(resources.getString("picker.values.toomany"), MAX_LISTED_VALUES));
        } else if (choices.isEmpty()) {
            picker.setValuesMessage(resources.getString("picker.values.none"));
        } else {
            picker.getValues().setAll(choices);
            picker.setValuesMessage(null);
        }
        updatePending();
    }

    /**
     * Works out, off the JavaFX thread, what the picker's choices would show, so the footer can
     * say how many concepts match and Apply has nothing left to read. A field whose values are
     * typed is read on Apply instead: reading it on every keystroke would scan the pattern each time.
     */
    private void updatePending() {
        int run = ++pendingRun;
        pendingSpec = null;
        pendingResult = null;
        pendingBase = null;
        picker.setSummary(null);
        picker.setApplyDisabled(true);
        if (picker.getSelectedPattern() == null || getBaseNavigator() == null) {
            return;
        }
        if (!isNarrowing()) {
            updatePendingForPattern(run);
            return;
        }
        Choice value = picker.getSelectedValue();
        String text = picker.getValueText();
        if (valueIndex == null) {
            return;
        }
        if (valueIndex.tooMany()) {
            picker.setApplyDisabled(text.isBlank());
            return;
        }
        if (value == null) {
            return;
        }
        Spec spec = chosenSpec(value.key(), value.label());
        MutableLongSet candidates = valueIndex.componentsByValue().get(value.key());
        Navigator base = getBaseNavigator();
        picker.setSummary(resources.getString("picker.summary.counting"));
        TinkExecutor.threadPool().execute(() -> {
            ConceptFilterResult result = resolveResult(base, candidates);
            Platform.runLater(() -> {
                if (run == pendingRun) {
                    pendingSpec = spec;
                    pendingResult = result;
                    pendingBase = base;
                    picker.setSummary(summaryOf(result.getMatchCount()));
                    picker.setApplyDisabled(result.getMatchCount() == 0);
                }
            });
        });
    }

    /**
     * The pattern alone as the filter: every component that has a semantic of it. The components
     * are read once for a pattern and kept while it stays chosen, so opening and closing the field
     * part does not read them again. Nothing is read while the picker is closed.
     */
    private void updatePendingForPattern(int run) {
        if (!pickerPopup.isShowing()) {
            choicesOutOfDate = true;
            return;
        }
        Choice pattern = picker.getSelectedPattern();
        Spec spec = new Spec((Long) pattern.key(), ANY_FIELD, null,
                MessageFormat.format(resources.getString("bar.filter.pattern"), pattern.label()));
        Navigator base = getBaseNavigator();
        MutableLongSet known = patternComponents;
        int patternRead = patternRun;
        picker.setSummary(resources.getString("picker.summary.counting"));
        TinkExecutor.threadPool().execute(() -> {
            MutableLongSet components = known != null ? known : readComponents(base.getViewCalculator(), spec);
            ConceptFilterResult result = resolveResult(base, components);
            Platform.runLater(() -> {
                if (patternRead == patternRun) {
                    patternComponents = components;
                }
                if (run == pendingRun) {
                    pendingSpec = spec;
                    pendingResult = result;
                    pendingBase = base;
                    picker.setSummary(summaryOf(result.getMatchCount()));
                    picker.setApplyDisabled(result.getMatchCount() == 0);
                }
            });
        });
    }

    private Spec chosenSpec(Object valueKey, String valueLabel) {
        int fieldIndex = picker.getSelectedFieldIndex();
        return new Spec((Long) picker.getSelectedPattern().key(), fieldIndex, valueKey,
                MessageFormat.format(resources.getString("bar.filter"), picker.getFields().get(fieldIndex), valueLabel));
    }

    private void apply() {
        if (pendingResult != null && pendingBase == getBaseNavigator()) {
            appliedSpec = pendingSpec;
            applyRun++;
            showFiltered(pendingBase, pendingResult);
        } else {
            String text = picker.getValueText().strip();
            appliedSpec = pendingSpec != null ? pendingSpec : chosenSpec(text, text);
            refilter();
        }
        pickerPopup.hide();
    }

    /** Reads the applied filter again, off the JavaFX thread, against the base navigator of the moment. */
    private void refilter() {
        int run = ++applyRun;
        Spec spec = appliedSpec;
        Navigator base = getBaseNavigator();
        filtered.set(true);
        bar.setFilterText(spec.label());
        bar.setCountText(resources.getString("bar.filtering"));
        forgetMatches();
        showListOrTree();
        TinkExecutor.threadPool().execute(() -> {
            ConceptFilterResult result = resolveResult(base, readComponents(base.getViewCalculator(), spec));
            Platform.runLater(() -> {
                if (run == applyRun) {
                    showFiltered(base, result);
                }
            });
        });
    }

    private void showFiltered(Navigator base, ConceptFilterResult result) {
        filtered.set(true);
        bar.setFilterText(appliedSpec.label());
        bar.setCountText(result.getMatchCount() == 1
                ? resources.getString("bar.count.one")
                : MessageFormat.format(resources.getString("bar.count"), countFormat.format(result.getMatchCount())));
        navigator.set(new FilteredNavigator(base, result));
        forgetMatches();
        shownResult = result;
        shownBase = base;
        showListOrTree();
    }

    private void clear() {
        appliedSpec = null;
        applyRun++;
        filtered.set(false);
        bar.setFilterText(null);
        bar.setCountText(null);
        navigator.set(getBaseNavigator());
        forgetMatches();
        showListOrTree();
    }

    /** The list's matches belong to the result that was shown; another filter, view or none leaves them behind. */
    private void forgetMatches() {
        matchesRun++;
        matchesRead = false;
        shownResult = null;
        shownBase = null;
        keptPaths.clear();
        matchList.getMatches().clear();
        matchList.setMessage(resources.getString("list.reading"));
    }

    /**
     * Shows the list in place of the tree while a filter is set and the row's switch is on List.
     * The matches are read the first time the list shows them, not when the filter is applied:
     * naming and sorting a large set is work the tree does not need.
     */
    private void showListOrTree() {
        listShown.set(isFiltered() && bar.isListShown());
        if (isListShown() && !matchesRead && shownResult != null) {
            readMatches();
        }
    }

    /** Names the matches and sorts them by name, off the JavaFX thread. */
    private void readMatches() {
        int run = ++matchesRun;
        matchesRead = true;
        long[] matchNids = shownResult.getMatchNids();
        ViewCalculator calculator = shownBase.getViewCalculator();
        TinkExecutor.threadPool().execute(() -> {
            List<ConceptMatchList.Match> matches = Arrays.stream(matchNids).parallel()
                    .mapToObj(nid -> new ConceptMatchList.Match(nid, calculator.getDescriptionTextOrNid(nid)))
                    .sorted(Comparator.comparing(ConceptMatchList.Match::name, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            Platform.runLater(() -> {
                if (run == matchesRun) {
                    matchList.getMatches().setAll(matches);
                    matchList.setMessage(null);
                }
            });
        });
    }

    /**
     * The path that leads to a concept in the filtered tree, from the top of the tree down to the
     * concept's parent. A concept with several parents is given the path through the first one the
     * tree shows. Asked for the rows of the list in view, so the paths are kept for a while.
     */
    private String pathOf(long nid) {
        String kept = keptPaths.get(nid);
        if (kept == null && shownResult != null) {
            kept = readPath(nid);
            keptPaths.put(nid, kept);
        }
        return kept;
    }

    private String readPath(long nid) {
        ViewCalculator calculator = shownBase.getViewCalculator();
        long[] rootNids = shownBase.getRootNids();
        Deque<String> names = new ArrayDeque<>();
        long current = nid;
        for (int depth = 0; depth < MAX_PATH_DEPTH; depth++) {
            OptionalLong parent = Arrays.stream(calculator.unsortedParentsOf(current).toArray())
                    .filter(shownResult::isVisible)
                    .findFirst();
            if (parent.isEmpty() || Arrays.stream(rootNids).anyMatch(root -> root == parent.getAsLong())) {
                break;
            }
            current = parent.getAsLong();
            names.addFirst(calculator.getDescriptionTextOrNid(current));
        }
        if (names.isEmpty()) {
            return resources.getString("list.path.top");
        }
        if (names.size() > MAX_PATH_NAMES) {
            String first = names.removeFirst();
            while (names.size() > MAX_PATH_NAMES - 1) {
                names.removeFirst();
            }
            names.addFirst(PATH_GAP);
            names.addFirst(first);
        }
        return String.join(PATH_SEPARATOR, names);
    }

    /**
     * A new base navigator is a new view: the tree follows it at once when there is no filter, and
     * once the filter has been read again under the new view when there is one. What the picker
     * had read is out of date, and is read again the next time it opens.
     */
    private void baseNavigatorChanged() {
        picker.getPatterns().clear();
        valueIndex = null;
        patternComponents = null;
        patternRun++;
        choicesOutOfDate = picker.getSelectedPattern() != null;
        updatePending();
        if (appliedSpec == null || getBaseNavigator() == null) {
            navigator.set(getBaseNavigator());
        } else {
            refilter();
        }
    }

    private String summaryOf(int matchCount) {
        return switch (matchCount) {
            case 0 -> resources.getString("picker.summary.none");
            case 1 -> resources.getString("picker.summary.count.one");
            default -> MessageFormat.format(resources.getString("picker.summary.count"), countFormat.format(matchCount));
        };
    }

    private static String labelOf(Object valueKey, ViewCalculator calculator) {
        return valueKey instanceof ComponentValue component
                ? calculator.getDescriptionTextOrNid(component.nid())
                : String.valueOf(valueKey);
    }

    /** The matches among the candidates that have a place in the base navigator's hierarchy, with their ancestors. */
    private static ConceptFilterResult resolveResult(Navigator base, LongIterable candidates) {
        ViewCalculator calculator = base.getViewCalculator();
        return ConceptFilterResult.resolve(candidates, nid -> calculator.unsortedParentsOf(nid).toArray(), base.getRootNids());
    }

    /**
     * Reads every semantic of the pattern once and groups the components they are about by the
     * value of the field. Past {@link #MAX_LISTED_VALUES} different values the field is given up
     * as one whose values cannot be listed.
     */
    private static ValueIndex readValueIndex(ViewCalculator calculator, long patternNid, int fieldIndex) {
        Map<Object, MutableLongSet> componentsByValue = new ConcurrentHashMap<>();
        AtomicBoolean tooMany = new AtomicBoolean();
        calculator.forEachSemanticVersionOfPatternParallel(patternNid, (version, _) -> {
            if (tooMany.get() || fieldIndex >= version.fieldValues().size()) {
                return;
            }
            forEachValueKey(version.fieldValues().get(fieldIndex), key ->
                    componentsByValue.computeIfAbsent(key, _ -> LongSets.mutable.empty().asSynchronized())
                            .add(version.referencedComponentNid()));
            if (componentsByValue.size() > MAX_LISTED_VALUES) {
                tooMany.set(true);
            }
        });
        return new ValueIndex(tooMany.get() ? Map.of() : componentsByValue, tooMany.get());
    }

    /**
     * The components that have a semantic of the filter's pattern: all of them for a filter by the
     * pattern alone, otherwise those whose field has the filter's value.
     */
    private static MutableLongSet readComponents(ViewCalculator calculator, Spec spec) {
        MutableLongSet components = LongSets.mutable.empty().asSynchronized();
        calculator.forEachSemanticVersionOfPatternParallel(spec.patternNid(), (version, _) -> {
            if (spec.fieldIndex() == ANY_FIELD) {
                components.add(version.referencedComponentNid());
            } else if (spec.fieldIndex() < version.fieldValues().size()) {
                forEachValueKey(version.fieldValues().get(spec.fieldIndex()), key -> {
                    if (sameValue(key, spec.valueKey())) {
                        components.add(version.referencedComponentNid());
                    }
                });
            }
        });
        return components;
    }

    /** Typed text matches any value that is written the same, whatever its type, except a component. */
    private static boolean sameValue(Object key, Object wanted) {
        return key.equals(wanted)
                || (wanted instanceof String text && !(key instanceof ComponentValue) && String.valueOf(key).equals(text));
    }

    /**
     * The values a field value stands for: a component for itself, a set or list of components
     * for each of them, text and numbers for themselves. Other kinds of value (a tree, a graph,
     * an image) are not something a filter can ask for, and give nothing.
     */
    private static void forEachValueKey(Object fieldValue, Consumer<Object> consumer) {
        switch (fieldValue) {
            case null -> {
            }
            case EntityFacade component -> consumer.accept(new ComponentValue(component.nid()));
            case LongIdCollection components -> components.forEach(nid -> consumer.accept(new ComponentValue(nid)));
            case String text -> {
                if (!text.isBlank()) {
                    consumer.accept(text);
                }
            }
            case Number number -> consumer.accept(number);
            case Boolean flag -> consumer.accept(flag);
            case Instant instant -> consumer.accept(instant);
            default -> {
            }
        }
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- baseNavigator
    /**
     * The navigator the filter narrows: the whole hierarchy under the navigator's view.
     */
    private final ObjectProperty<Navigator> baseNavigator = new SimpleObjectProperty<>(this, "baseNavigator");
    public ObjectProperty<Navigator> baseNavigatorProperty() { return baseNavigator; }
    public Navigator getBaseNavigator() { return baseNavigator.get(); }
    public void setBaseNavigator(Navigator value) { baseNavigator.set(value); }

    // -- navigator
    /**
     * The navigator to show in the tree: the base navigator, or a {@link FilteredNavigator} over
     * it while a filter is set.
     */
    private final ReadOnlyObjectWrapper<Navigator> navigator = new ReadOnlyObjectWrapper<>(this, "navigator");
    public ReadOnlyObjectProperty<Navigator> navigatorProperty() { return navigator.getReadOnlyProperty(); }
    public Navigator getNavigator() { return navigator.get(); }

    // -- filtered
    /**
     * Whether a filter is set.
     */
    private final ReadOnlyBooleanWrapper filtered = new ReadOnlyBooleanWrapper(this, "filtered");
    public ReadOnlyBooleanProperty filteredProperty() { return filtered.getReadOnlyProperty(); }
    public boolean isFiltered() { return filtered.get(); }

    // -- listShown
    /**
     * Whether the list of matching concepts is to be shown in place of the tree: a filter is set
     * and the row's switch is on List.
     */
    private final ReadOnlyBooleanWrapper listShown = new ReadOnlyBooleanWrapper(this, "listShown");
    public ReadOnlyBooleanProperty listShownProperty() { return listShown.getReadOnlyProperty(); }
    public boolean isListShown() { return listShown.get(); }

    // -- onShowInTree
    /**
     * What "Show in tree" on a concept of the list does, given the concept's nid. The tree is
     * already showing again when it is called.
     */
    private final ObjectProperty<LongConsumer> onShowInTree = new SimpleObjectProperty<>(this, "onShowInTree");
    public ObjectProperty<LongConsumer> onShowInTreeProperty() { return onShowInTree; }
    public LongConsumer getOnShowInTree() { return onShowInTree.get(); }
    public void setOnShowInTree(LongConsumer value) { onShowInTree.set(value); }

    // -- searchPrompt
    /**
     * The prompt for the navigator's search box while a filter is set, saying that the search
     * covers the filtered concepts only; null while none is.
     */
    private final ReadOnlyStringWrapper searchPrompt = new ReadOnlyStringWrapper(this, "searchPrompt");
    public ReadOnlyStringProperty searchPromptProperty() { return searchPrompt.getReadOnlyProperty(); }
    public String getSearchPrompt() { return searchPrompt.get(); }

    /***************************************************************************
     *                                                                         *
     * Supporting Classes                                                      *
     *                                                                         *
     **************************************************************************/

    /** A field value that is a component, as opposed to text or a number that happens to equal its nid. */
    private record ComponentValue(long nid) {
    }

    /**
     * What a filter asks for, with the words that name it in the row. A filter by the pattern
     * alone has {@link #ANY_FIELD} for field and no value.
     */
    private record Spec(long patternNid, int fieldIndex, Object valueKey, String label) {
    }

    /** The values one field of a pattern has, each with the components that carry it. */
    private record ValueIndex(Map<Object, MutableLongSet> componentsByValue, boolean tooMany) {
    }
}
