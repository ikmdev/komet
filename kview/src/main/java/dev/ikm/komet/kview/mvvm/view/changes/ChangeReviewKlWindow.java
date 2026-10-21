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
package dev.ikm.komet.kview.mvvm.view.changes;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractChapterKlWindow;
import dev.ikm.komet.kview.klwindows.ChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.UUID;

/**
 * The journal workspace window that reviews the unpublished changes, one component of the tray
 * at a time: its changes as cards of Before / After field rows, Previous / Next through the
 * tray's components, and a footer that opens, discards or publishes just this component. The
 * window builds and owns its content ({@link ChangeReviewPane}); the tray fills it with
 * {@link #show(Page)}, answers its {@link Actions} and binds {@link #busyProperty()} to whatever
 * should hold the footer's buttons.
 * <p>
 * It lives only while open: it has no preferences node, nothing of it is saved, and the
 * journal does not list it among the windows it restores ({@link #isTransient}).
 */
public final class ChangeReviewKlWindow extends AbstractChapterKlWindow<Pane> {

    /** What the window's controls ask of the tray. */
    public interface Actions {
        void previous();

        void next();

        void open(UnpublishedGroup group);

        void publish(UnpublishedGroup group);

        void discard(UnpublishedGroup group);

        void close();
    }

    /**
     * One page of the review: a component and the diffs of its changes.
     *
     * @param group       the component and its changes
     * @param diffs       one diff per change, in the group's order
     * @param index       the component's position among the tray's components, from 0
     * @param total       how many components the tray lists
     * @param changeTotal how many changes the tray lists in all
     * @param author      who saved the changes, as described on the view
     */
    public record Page(UnpublishedGroup group, List<ChangeDiff> diffs, int index, int total, int changeTotal,
                       String author) {
    }

    /** The window's type; its prefix never reaches a journal's saved window names. */
    public static final EntityKlWindowType TYPE = new EntityKlWindowType() {
        @Override
        public String getPrefix() {
            return "journal_change_review_";
        }

        @Override
        public String toString() {
            return "JOURNAL_CHANGE_REVIEW";
        }
    };

    private final UUID windowTopic = UUID.randomUUID();

    /** True while the tray publishes or discards; the footer's buttons wait for it. */
    private final BooleanProperty busy = new SimpleBooleanProperty(this, "busy", false);

    private final ChangeReviewPane pane;

    /**
     * @param viewProperties the journal's view
     * @param actions        what the window's controls do
     */
    public ChangeReviewKlWindow(ViewProperties viewProperties, Actions actions) {
        super(viewProperties, null);
        this.pane = new ChangeReviewPane(actions);
        this.pane.busyProperty().bind(busy);
        this.paneWindow = pane;
    }

    /** Whether a window is one the journal never saves or restores. */
    public static boolean isTransient(ChapterKlWindow<?> window) {
        return window instanceof ChangeReviewKlWindow;
    }

    /** Shows a component and its changes; the previous page is replaced. */
    public void show(Page page) {
        pane.show(page);
    }

    /** Whether the window is still on the workspace (it may have been removed without its close button). */
    public boolean isShowing() {
        return pane.getScene() != null;
    }

    /** Brings the window in front of the workspace's other windows. */
    public void toFront() {
        pane.toFront();
    }

    /** Closes the window: runs the close action the journal gave it when it was added. */
    public void close() {
        getOnClose().ifPresent(Runnable::run);
    }

    @Override
    public UUID getWindowTopic() {
        return windowTopic;
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return TYPE;
    }

    @Override
    public void onShown() {
    }

    /** Nothing is saved: the window is rebuilt from the tray each time it is opened. */
    @Override
    public void save() {
    }

    @Override
    public void delete() {
    }

    @Override
    protected boolean isPropertyPanelOpen() {
        return false;
    }

    @Override
    protected void setPropertyPanelOpen(boolean isOpen) {
    }

    @Override
    protected String selectedPropertyPanel() {
        return null;
    }

    @Override
    protected void setSelectedPropertyPanel(String propertyPanel) {
    }

    @Override
    protected void captureAdditionalState(EntityKlWindowState state) {
    }

    @Override
    protected void applyAdditionalState(EntityKlWindowState state) {
    }

    /*=========================================================================*
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     *=========================================================================*/

    // -- busy
    public final BooleanProperty busyProperty() { return busy; }
    public final boolean isBusy() { return busy.get(); }
    public final void setBusy(boolean busy) { this.busy.set(busy); }
}
