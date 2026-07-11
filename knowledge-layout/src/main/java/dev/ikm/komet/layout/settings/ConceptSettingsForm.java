package dev.ikm.komet.layout.settings;

import dev.ikm.tinkar.common.bind.ClassConceptBinding;
import dev.ikm.tinkar.common.bind.EnumConceptBinding;
import dev.ikm.tinkar.common.binary.Encodable;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A generic settings editor that reflects over an {@link Encodable} settings record whose components
 * are concept-identity value enums, rendering one {@link ChoiceBox} per such field — items from the
 * enum's constants, labels from its {@link EnumConceptBinding#regularNames() regular name}. Picking a
 * value rebuilds the record (via its canonical constructor, with that one component replaced) and
 * pushes it to the caller, which persists it through {@link ControlSettings}. This is the default,
 * zero-boilerplate editor that the {@code KlPreferenceEditable} preferences toggle shows.
 *
 * <p>An area can tune the generated form with {@link #hide(String)} (drop a field) and
 * {@link #augment(Node)} (add extra controls); an override is simply {@code hide} + {@code augment}.
 * Only concept-identity-enum components auto-render in this version; other component types are
 * skipped (an area supplies its own control for them via {@code augment}). Configure the hooks before
 * the form is shown (they take effect at {@link #build()}, which runs on scene attach).
 *
 * <p>Reflection reads the record's public accessors and canonical constructor, so the record's
 * package must be open (as {@code PrintSettings}' package already is for {@code Encodable}). Must be
 * used on the JavaFX application thread; the pure helpers are thread-free and unit-tested.
 *
 * @param <T> the settings record type (an {@link Encodable} {@link Record})
 */
public final class ConceptSettingsForm<T extends Record & Encodable> extends VBox {

    private final Supplier<T> current;
    private final Consumer<T> onChange;
    private final Set<String> hidden = new HashSet<>();
    private final List<Node> augmentations = new ArrayList<>();
    private final List<Runnable> synchronizers = new ArrayList<>();
    private boolean populating;
    private boolean built;

    /**
     * @param current  supplies the current settings record (read live so edits compose)
     * @param onChange receives the rebuilt record after each change
     */
    public ConceptSettingsForm(Supplier<T> current, Consumer<T> onChange) {
        this.current = Objects.requireNonNull(current, "current is null");
        this.onChange = Objects.requireNonNull(onChange, "onChange is null");
        setSpacing(10);
        setPadding(new Insets(12));
        setFillWidth(true);
        getStyleClass().add("kl-settings-form");
        sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null && !built) {
                build();
            }
        });
    }

    /**
     * Convenience factory.
     *
     * @param current  supplies the current settings record
     * @param onChange receives the rebuilt record after each change
     * @param <T>      the settings record type
     * @return a new form
     */
    public static <T extends Record & Encodable> ConceptSettingsForm<T> of(
            Supplier<T> current, Consumer<T> onChange) {
        return new ConceptSettingsForm<>(current, onChange);
    }

    /**
     * Drops a field from the generated form.
     *
     * @param componentName the record component name to hide
     * @return this form, for chaining
     */
    public ConceptSettingsForm<T> hide(String componentName) {
        hidden.add(componentName);
        return this;
    }

    /**
     * Adds an extra control below the generated fields (for custom or non-enum settings).
     *
     * @param node the control to append
     * @return this form, for chaining
     */
    public ConceptSettingsForm<T> augment(Node node) {
        augmentations.add(node);
        return this;
    }

    /** Re-reads the current record into the choice boxes without emitting changes. */
    public void sync() {
        populating = true;
        try {
            synchronizers.forEach(Runnable::run);
        } finally {
            populating = false;
        }
    }

    /** Builds the form's rows from the record's concept-enum components. Idempotent. */
    public void build() {
        if (built) {
            return;
        }
        built = true;
        populating = true;

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        ColumnConstraints captions = new ColumnConstraints();
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);
        fields.setFillWidth(true);
        grid.getColumnConstraints().addAll(captions, fields);

        RecordComponent[] components = current.get().getClass().getRecordComponents();
        int row = 0;
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            if (hidden.contains(component.getName()) || !isConceptEnum(component.getType())) {
                continue;
            }
            Label caption = new Label(prettyName(component.getName()));
            caption.getStyleClass().add("kl-settings-caption");
            grid.add(caption, 0, row);
            grid.add(choiceFor(i, component), 1, row);
            row++;
        }
        populating = false;

        getChildren().add(grid);
        getChildren().addAll(augmentations);
    }

    private ChoiceBox<Object> choiceFor(int index, RecordComponent component) {
        ChoiceBox<Object> box = new ChoiceBox<>(
                FXCollections.observableArrayList(component.getType().getEnumConstants()));
        box.setMaxWidth(Double.MAX_VALUE);
        box.setConverter(new StringConverter<>() {
            @Override
            public String toString(Object value) {
                return value == null ? "" : ((EnumConceptBinding) value).regularNames().getAny();
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });
        box.setValue(readComponent(current.get(), component));
        box.valueProperty().addListener((obs, old, value) -> {
            if (!populating && value != null) {
                onChange.accept(rebuildWith(current.get(), index, value));
            }
        });
        synchronizers.add(() -> box.setValue(readComponent(current.get(), component)));
        return box;
    }

    // ---- Pure, unit-testable reflection helpers --------------------------------------------------

    /**
     * Whether a record component type is a concept-identity value enum (auto-renderable).
     *
     * @param type the component type
     * @return {@code true} if it is an enum implementing {@link EnumConceptBinding}
     */
    public static boolean isConceptEnum(Class<?> type) {
        return type.isEnum() && EnumConceptBinding.class.isAssignableFrom(type);
    }

    /**
     * Reads a record component's current value via its accessor.
     *
     * @param record    the record instance
     * @param component the component to read
     * @return the component value
     */
    public static Object readComponent(Record record, RecordComponent component) {
        try {
            Method accessor = component.getAccessor();
            accessor.setAccessible(true);
            return accessor.invoke(record);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read record component " + component.getName(), e);
        }
    }

    /**
     * Rebuilds a record with the component at {@code index} replaced by {@code newValue}, via the
     * record's canonical constructor.
     *
     * @param record   the source record
     * @param index    the component index to replace
     * @param newValue the replacement value
     * @param <T>      the record type
     * @return a new record with the one component changed
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T rebuildWith(T record, int index, Object newValue) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        Class<?>[] types = new Class<?>[components.length];
        Object[] values = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
            values[i] = readComponent(record, components[i]);
        }
        values[index] = newValue;
        try {
            Constructor<?> canonical = record.getClass().getDeclaredConstructor(types);
            canonical.setAccessible(true);
            return (T) canonical.newInstance(values);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot rebuild record " + record.getClass().getName(), e);
        }
    }

    private static String prettyName(String componentName) {
        String words = ClassConceptBinding.camelCaseToWords(componentName);
        return words.isEmpty() ? words : Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }
}
