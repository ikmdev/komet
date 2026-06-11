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
package dev.ikm.komet.layout_engine.blueprint;

import dev.ikm.komet.layout.check.CheckResult;
import dev.ikm.komet.layout.check.Status;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.KlArea;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.service.TinkExecutor;
import dev.ikm.tinkar.coordinate.view.ViewCoordinateRecord;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base for a <em>check area</em> — a supplemental area that runs an action against a
 * focused item and reports a pass / fail / error / unknown verdict as a coloured status
 * indicator.
 *
 * <p>This base supplies everything common to every check, regardless of backend:
 * <ul>
 *   <li>A compact header (status dot, title, and a <b>Run</b> button) mounted in the top of the
 *       supplemental area's {@link javafx.scene.layout.BorderPane}; the inherited
 *       {@code gridPaneForChildren()} centre is left available for subclasses.</li>
 *   <li>A {@link #focusProperty() focus} property naming the {@link EntityFacade} to evaluate.</li>
 *   <li>Background execution of the check on the {@link TinkExecutor} pool, with the verdict
 *       applied back on the JavaFX application thread, and a coloured {@link Circle} / spinner
 *       swap while a run is in flight.</li>
 * </ul>
 *
 * <p>Subclasses implement a single hook, {@link #evaluate(EntityFacade, ViewCalculator)}, which
 * runs off the FX thread and returns a {@link CheckResult}. Distinct backends (Claude, the
 * rules engine, …) are distinct subclasses, each contributing its own
 * {@link dev.ikm.komet.layout.area.KlSupplementalArea.Factory}.
 *
 * <p><b>Focus resolution (v1):</b> the {@code focus} property is set by the host — the layout
 * editor's properties pane (a fixed target concept) or, at runtime, whatever drives the area.
 * When {@code focus} is {@code null} the area reports {@link Status#UNKNOWN}. Automatically
 * binding the focus to the concept the surrounding layout is rendering is a planned follow-up
 * (see the layout-editor integration issues); it is intentionally not wired here so this base
 * stays backend- and host-agnostic.
 */
public abstract class AbstractCheckArea extends SupplementalAreaBlueprint {

    private final StringProperty title = new SimpleStringProperty(this, "title", "Check");
    private final ObjectProperty<EntityFacade> focus = new SimpleObjectProperty<>(this, "focus");
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(this, "status", Status.UNKNOWN);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ViewProperties injectedViewProperties;

    private final Circle statusDot = new Circle(7);
    private final ProgressIndicator progress = new ProgressIndicator();
    private final Label titleLabel = new Label();
    private final Label detailLabel = new Label();
    private final Button runButton = new Button("Run");

    {
        buildCheckUi();
    }

    /**
     * Restore constructor.
     *
     * @param preferences the preferences node backing this area
     */
    protected AbstractCheckArea(KometPreferences preferences) {
        super(preferences);
    }

    /**
     * Create constructor.
     *
     * @param preferencesFactory factory for this area's preferences node
     * @param areaFactory        the factory creating this area
     */
    protected AbstractCheckArea(KlPreferencesFactory preferencesFactory, KlArea.Factory areaFactory) {
        super(preferencesFactory, areaFactory);
    }

    /**
     * Evaluates the focused item and returns a verdict. Invoked off the JavaFX application
     * thread; implementations may block (network, rules engine, datastore queries).
     *
     * @param item           the item in focus; never {@code null} (the base reports
     *                       {@link Status#UNKNOWN} without calling this method when there is no
     *                       focus)
     * @param viewProperties the view (calculator, navigation, and edit coordinates) for the
     *                       current context, resolved on the FX thread before this call; may be
     *                       {@code null} if no view could be resolved
     * @return the verdict; if {@code null} is returned it is treated as {@link Status#UNKNOWN}
     */
    protected abstract CheckResult evaluate(EntityFacade item, ViewProperties viewProperties);

    /**
     * Resolves the {@link ViewProperties} the check runs against. Returns the injected view when
     * one was supplied via {@link #setCheckViewProperties(ViewProperties)}; otherwise derives an
     * overridable view from this area's layout context. Called on the JavaFX application thread.
     *
     * @return the view properties for the current context
     */
    protected ViewProperties viewProperties() {
        if (injectedViewProperties != null) {
            return injectedViewProperties;
        }
        ViewCoordinateRecord viewRecord = context().viewCoordinate().toViewCoordinateRecord();
        String name = this.getClass().getSimpleName();
        return new ObservableViewNoOverride(viewRecord, name).makeOverridableViewProperties(name);
    }

    /**
     * Injects the view this area should query, overriding the context-derived default. A host
     * (for example a journal tool window) may call this before the area is shown.
     *
     * @param viewProperties the view to use, or {@code null} to fall back to the context view
     */
    public final void setCheckViewProperties(ViewProperties viewProperties) {
        this.injectedViewProperties = viewProperties;
    }

    private void buildCheckUi() {
        statusDot.setStroke(Color.web("#00000022"));
        progress.setPrefSize(16, 16);
        progress.setMaxSize(16, 16);
        progress.setVisible(false);
        progress.setManaged(false);

        StackPane indicator = new StackPane(statusDot, progress);

        titleLabel.textProperty().bind(title);
        titleLabel.getStyleClass().add("kl-check-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        runButton.setOnAction(event -> requestRun());

        HBox header = new HBox(8, indicator, titleLabel, spacer, runButton);
        header.setAlignment(Pos.CENTER_LEFT);

        detailLabel.setWrapText(true);
        detailLabel.getStyleClass().add("kl-check-detail");

        VBox content = new VBox(4, header, detailLabel);
        content.setPadding(new Insets(8));

        // The supplemental BorderPane already centres gridPaneForChildren(); mount the check
        // chrome in the top so subclasses keep the centre for any additional content.
        fxObject().setTop(content);

        status.subscribe(this::updateStatusVisual);
        updateStatusVisual();
    }

    private void updateStatusVisual() {
        Status current = status.get();
        Color color = switch (current) {
            case PASS -> Color.web("#2e7d32");
            case FAIL -> Color.web("#c62828");
            case ERROR -> Color.web("#ef6c00");
            case UNKNOWN -> Color.web("#9e9e9e");
        };
        statusDot.setFill(color);
        Tooltip.install(statusDot, new Tooltip(current.name()));
    }

    /**
     * Runs the check now: snapshots the focus and view calculator on the FX thread, then
     * evaluates off-thread and applies the verdict back on the FX thread. Re-entrant calls while
     * a run is already in flight are ignored.
     */
    public final void requestRun() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        final EntityFacade item = focus.get();
        ViewProperties resolved;
        try {
            resolved = viewProperties();
        } catch (RuntimeException e) {
            resolved = null;
        }
        final ViewProperties resolvedViewProperties = resolved;
        setBusy(true);

        TinkExecutor.threadPool().execute(() -> {
            CheckResult result;
            try {
                if (item == null) {
                    result = CheckResult.unknown("No item in focus.");
                } else {
                    CheckResult evaluated = evaluate(item, resolvedViewProperties);
                    result = (evaluated == null) ? CheckResult.unknown("No result.") : evaluated;
                }
            } catch (Throwable t) {
                AreaBlueprint.LOG.error("Check failed in {}", this.getClass().getSimpleName(), t);
                String message = (t.getMessage() == null) ? t.toString() : t.getMessage();
                result = CheckResult.error(message);
            }
            final CheckResult applied = result;
            Platform.runLater(() -> applyResult(applied));
        });
    }

    private void applyResult(CheckResult result) {
        status.set(result.status());
        detailLabel.setText(result.detail());
        setBusy(false);
        running.set(false);
    }

    private void setBusy(boolean busy) {
        progress.setVisible(busy);
        progress.setManaged(busy);
        statusDot.setVisible(!busy);
        statusDot.setManaged(!busy);
        runButton.setDisable(busy);
    }

    /**
     * The item this area evaluates. Set by the host (the layout editor's properties pane, or
     * whatever drives the area at runtime). When {@code null} the area reports
     * {@link Status#UNKNOWN}.
     *
     * @return the focus property
     */
    public final ObjectProperty<EntityFacade> focusProperty() {
        return focus;
    }

    /**
     * Sets the item to evaluate.
     *
     * @param item the item, or {@code null} to clear the focus
     */
    public final void setFocus(EntityFacade item) {
        focus.set(item);
    }

    /**
     * Returns the item currently in focus.
     *
     * @return the focused item, or {@code null} if none
     */
    public final EntityFacade getFocus() {
        return focus.get();
    }

    /**
     * The most recent verdict (defaults to {@link Status#UNKNOWN} before the first run).
     *
     * @return the status property
     */
    public final ObjectProperty<Status> statusProperty() {
        return status;
    }

    /**
     * Returns the most recent verdict.
     *
     * @return the current status
     */
    public final Status getStatus() {
        return status.get();
    }

    /**
     * The title shown in the area header.
     *
     * @return the title property
     */
    public final StringProperty titleProperty() {
        return title;
    }

    /**
     * Sets the title shown in the area header.
     *
     * @param checkTitle the title text
     */
    public final void setCheckTitle(String checkTitle) {
        title.set(checkTitle);
    }

    @Override
    protected void subAreaRestoreFromPreferencesOrDefault() {
        // Focus is supplied by the host; nothing persisted by this base in v1.
    }

    @Override
    protected void subAreaRevert() {
        // No persisted check-specific state to revert in this base.
    }

    @Override
    protected void subAreaSave() {
        // No persisted check-specific state to save in this base.
    }

    @Override
    public void knowledgeLayoutBind() {
        Platform.runLater(() -> this.lifecycleState.set(LifecycleState.BOUND));
    }

    @Override
    public void knowledgeLayoutUnbind() {
        // Nothing to unbind.
    }
}
