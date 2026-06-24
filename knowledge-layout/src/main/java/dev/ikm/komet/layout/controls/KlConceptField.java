/*
 * Copyright © 2024 Integrated Knowledge Management (support@ikm.dev)
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * An editable, single-concept field: it shows a concept as a large identicon
 * {@link dev.ikm.komet.framework.controls.KonceptBadge}, and lets the user <em>clear</em> it
 * (the X) and <em>refill</em> it by typing — a type-ahead search whose results come from an
 * injected {@link Completer}, so the control is backend-agnostic. Clearing swaps the badge for a
 * search text field <em>in place</em> (the field node is stable), so a half-typed query is never
 * lost to a re-render. The field is also a concept drop target and the badge a drag source.
 *
 * <p>Callbacks fire only on <strong>user</strong> actions — {@link #onSelectedProperty()} when the
 * user commits a concept (from the popup or a drop), {@link #onClearedProperty()} when the user
 * clicks the X, {@link #onDetailProperty()} on a click that is not a drag. The host updates its own
 * model in those callbacks and then normalizes the displayed value back via {@link #setValue} —
 * which does <em>not</em> re-fire the callbacks, so host-normalization cannot loop.
 */
public class KlConceptField extends Control {

    /** The control's CSS style class. */
    public static final String DEFAULT_STYLE_CLASS = "kl-concept-field";

    /** Default identicon edge length (px) — deliberately large, for a legible concept. */
    public static final double DEFAULT_ICON_SIZE = 28;

    /**
     * The displayable value of the field: a grounded concept or empty. Sealed so the skin switches
     * exhaustively and a new state is a compile error.
     */
    public sealed interface Value permits Value.Concept, Value.Empty {

        /**
         * A resolved concept rendered as a big badge.
         *
         * @param nid   the concept nid
         * @param label the presentation label (the badge resolves its own name from the view; this
         *              is carried for the host); never null
         */
        record Concept(int nid, String label) implements Value {
            /**
             * Validates the value.
             *
             * @throws NullPointerException if {@code label} is null
             */
            public Concept {
                Objects.requireNonNull(label, "label");
            }
        }

        /** No concept — the field shows its search text field in place. */
        record Empty() implements Value {
        }
    }

    /**
     * A backend-agnostic concept search. The host injects one (for example over a Lucene searcher);
     * it must run the query <em>off</em> the FX thread and deliver results on the FX thread.
     */
    @FunctionalInterface
    public interface Completer {

        /**
         * One result row.
         *
         * @param nid   the concept nid
         * @param label the presentation label
         */
        record Result(int nid, String label) {
        }

        /**
         * Runs {@code query} and delivers rows to {@code onResults} on the FX thread.
         *
         * @param query      the user's current text (never null or blank when called)
         * @param maxResults a soft cap on the number of rows
         * @param onResults  an FX-thread callback receiving the rows (possibly empty)
         */
        void complete(String query, int maxResults, Consumer<List<Result>> onResults);
    }

    private final ObjectProperty<Value> value = new SimpleObjectProperty<>(this, "value", new Value.Empty());
    private final ObjectProperty<Completer> completer = new SimpleObjectProperty<>(this, "completer");
    private final ObjectProperty<ViewProperties> viewProperties = new SimpleObjectProperty<>(this, "viewProperties");
    private final DoubleProperty iconSize = new SimpleDoubleProperty(this, "iconSize", DEFAULT_ICON_SIZE);
    private final BooleanProperty clearable = new SimpleBooleanProperty(this, "clearable", true);
    private final ObjectProperty<Consumer<Value.Concept>> onSelected = new SimpleObjectProperty<>(this, "onSelected");
    private final ObjectProperty<Runnable> onCleared = new SimpleObjectProperty<>(this, "onCleared");
    private final ObjectProperty<Consumer<Value>> onDetail = new SimpleObjectProperty<>(this, "onDetail");

    /** Creates an empty field. */
    public KlConceptField() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    // ── value (host sets this quietly; it never re-fires user callbacks) ─────────

    /**
     * The displayed value; never null, defaults to {@link Value.Empty}. Setting it updates the
     * display only — it does not fire {@link #onSelectedProperty()} / {@link #onClearedProperty()},
     * so a host normalizing the value (for example after grounding a dropped concept) cannot loop.
     *
     * @return the value property
     */
    public final ObjectProperty<Value> valueProperty() {
        return value;
    }

    /**
     * Returns the current value.
     *
     * @return the value (never null)
     */
    public final Value getValue() {
        return value.get();
    }

    /**
     * Sets the value (host normalization; fires no user callback).
     *
     * @param newValue the value; null is treated as {@link Value.Empty}
     */
    public final void setValue(Value newValue) {
        value.set(newValue == null ? new Value.Empty() : newValue);
    }

    /**
     * The concept nid currently shown, or empty.
     *
     * @return the nid, or {@link OptionalInt#empty()} when empty
     */
    public final OptionalInt conceptNid() {
        return (value.get() instanceof Value.Concept concept) ? OptionalInt.of(concept.nid()) : OptionalInt.empty();
    }

    // ── injected backend + presentation ──────────────────────────────────────────

    /**
     * The injected search backend.
     *
     * @return the completer property
     */
    public final ObjectProperty<Completer> completerProperty() {
        return completer;
    }

    /**
     * The view used to build the live, draggable badge and result rows; null renders a
     * presentation-only badge with no drag or status.
     *
     * @return the view-properties property
     */
    public final ObjectProperty<ViewProperties> viewPropertiesProperty() {
        return viewProperties;
    }

    /**
     * The identicon edge length (px) for the badge.
     *
     * @return the icon-size property
     */
    public final DoubleProperty iconSizeProperty() {
        return iconSize;
    }

    /**
     * Whether the clear (X) is offered. {@code false} makes the field substitute-only (drop or
     * search-replace still work, but the concept cannot be deleted to empty) — used for a typed
     * statement's topic and for a measure's unit, where an empty value is not meaningful.
     *
     * @return the clearable property
     */
    public final BooleanProperty clearableProperty() {
        return clearable;
    }

    /**
     * Whether the clear (X) is offered.
     *
     * @return true if clearable
     */
    public final boolean isClearable() {
        return clearable.get();
    }

    /**
     * Sets whether the clear (X) is offered.
     *
     * @param value true to offer clearing
     */
    public final void setClearable(boolean value) {
        clearable.set(value);
    }

    // ── user-action callbacks (host-supplied) ─────────────────────────────────────

    /**
     * Called when the user commits a concept (popup selection or drop).
     *
     * @return the selection-callback property
     */
    public final ObjectProperty<Consumer<Value.Concept>> onSelectedProperty() {
        return onSelected;
    }

    /**
     * Called when the user clears the field (the X); the field stays, showing its search box.
     *
     * @return the clear-callback property
     */
    public final ObjectProperty<Runnable> onClearedProperty() {
        return onCleared;
    }

    /**
     * Called on a click that is not a drag — to open the concept's detail.
     *
     * @return the detail-callback property
     */
    public final ObjectProperty<Consumer<Value>> onDetailProperty() {
        return onDetail;
    }

    // ── skin-facing user-commit helpers (set value AND fire the user callback) ────

    /**
     * Commits a user-chosen concept: updates the value and fires {@link #onSelectedProperty()}.
     *
     * @param nid   the chosen concept nid
     * @param label the presentation label
     */
    public final void commitConcept(int nid, String label) {
        Value.Concept concept = new Value.Concept(nid, label == null ? "" : label);
        value.set(concept);
        Consumer<Value.Concept> callback = onSelected.get();
        if (callback != null) {
            callback.accept(concept);
        }
    }

    /**
     * Clears the field by user action: sets {@link Value.Empty} and fires {@link #onClearedProperty()}.
     */
    public final void commitClear() {
        value.set(new Value.Empty());
        Runnable callback = onCleared.get();
        if (callback != null) {
            callback.run();
        }
    }

    /** Fires the detail callback for the current concept, if any (called by the skin). */
    public final void requestDetail() {
        Consumer<Value> callback = onDetail.get();
        if (callback != null && value.get() instanceof Value.Concept) {
            callback.accept(value.get());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new dev.ikm.komet.layout.controls.skin.KlConceptFieldSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KlConceptField.class.getResource("kl-concept-field.css").toExternalForm();
    }
}
