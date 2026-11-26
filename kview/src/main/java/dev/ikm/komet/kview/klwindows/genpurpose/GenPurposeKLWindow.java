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
package dev.ikm.komet.kview.klwindows.genpurpose;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.klwindows.AbstractEntityChapterKlWindow;
import dev.ikm.komet.kview.klwindows.EntityKlWindowState;
import dev.ikm.komet.kview.klwindows.EntityKlWindowType;
import dev.ikm.komet.kview.klwindows.EntityKlWindowTypes;
import dev.ikm.komet.kview.mvvm.view.genpurpose.GenPurposeDetailsController;
import dev.ikm.komet.kview.mvvm.view.genpurpose.GenPurposeWindowView;
import dev.ikm.komet.kview.mvvm.view.genpurpose.WindowEditSession;
import dev.ikm.komet.layout.editor.StandardEditorWindows;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.BiFunction;

import static dev.ikm.komet.preferences.KLEditorPreferences.KL_STANDARD_WINDOWS_DIR;

/**
 * A general-purpose KL window: a chapter window on the journal workspace built from a KL-editor
 * window definition, viewing or editing one component.
 */
public class GenPurposeKLWindow extends AbstractEntityChapterKlWindow {

    /** Preference key holding the title of the KL-editor window definition this window is built from. */
    public static final String KL_EDITOR_WINDOW_TITLE = "KL_EDITOR_WINDOW_TITLE";

    /**
     * Preference key holding the kl-editor-app folder the definition lives in:
     * {@code user-windows} or {@code standard-windows}.
     */
    public static final String KL_EDITOR_WINDOW_DIR = "KL_EDITOR_WINDOW_DIR";

    /**
     * Preference key holding a UUID of the pattern a standard Semantic window shows semantics of.
     * The window's definition is generated from that pattern rather than read from a stored one,
     * and a window still creating its semantic has no semantic to read the pattern from.
     */
    public static final String SEMANTIC_PATTERN_UUID = "SEMANTIC_PATTERN_UUID";

    /**
     * The {@code kl-editor-app/{user,standard}-windows/<title>} node of the KL-editor window
     * definition this window is built from. Persisted by {@link #captureAdditionalState} so the
     * definition can be re-resolved when the journal is reopened (komet-desktop#20). Null for the
     * standard Semantic window, whose definition is generated (see {@link #semanticPattern}).
     */
    private final KometPreferences klEditorWindowPreferences;

    /**
     * The pattern a standard Semantic window shows a semantic of, which its definition is
     * generated from; null for every window built from a stored definition.
     */
    private final PatternFacade semanticPattern;

    /**
     * Constructs a general-purpose KL window built from a KL-editor window definition.
     *
     * @param journalTopic              the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param entityFacade              entity facade when not null usually this will load and display the current details.
     * @param viewProperties            view properties is access to view calculators to query data.
     * @param preferences               komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     * @param klEditorWindowPreferences the {@code kl-editor-app/{user,standard}-windows/<title>} node of the
     *                                  KL-editor window definition (sections, patterns, fields) to build the window from
     */
    public GenPurposeKLWindow(UUID journalTopic, EntityFacade entityFacade,
                              ViewProperties viewProperties, KometPreferences preferences,
                              KometPreferences klEditorWindowPreferences) {
        super(journalTopic, entityFacade, viewProperties, preferences);
        this.klEditorWindowPreferences = klEditorWindowPreferences;
        this.semanticPattern = null;

        build(journalTopic, entityFacade, (view, session) ->
                new GenPurposeDetailsController(view, session, journalTopic, klEditorWindowPreferences));
    }

    /**
     * Constructs the standard Semantic window: a general-purpose KL window framing one semantic,
     * built from the definition generated for the semantic's pattern
     * ({@link StandardEditorWindows#createSemanticWindow}).
     *
     * @param journalTopic   the UUID representing the journal topic the owning Journal Window uses to communicate events.
     * @param semantic       the semantic to show; null to create a new semantic of the pattern
     * @param pattern        the pattern of the semantic
     * @param viewProperties view properties is access to view calculators to query data.
     * @param preferences    komet preferences assists on reading and writing data to preferences user.home/Solor/database_folder/preferences
     */
    public GenPurposeKLWindow(UUID journalTopic, EntityFacade semantic, PatternFacade pattern,
                              ViewProperties viewProperties, KometPreferences preferences) {
        super(journalTopic, semantic, viewProperties, preferences);
        this.klEditorWindowPreferences = null;
        this.semanticPattern = pattern;

        // Generated through the window's derived coordinate (#660), like everything the window reads.
        build(journalTopic, semantic, (view, session) -> new GenPurposeDetailsController(view, session, journalTopic,
                StandardEditorWindows.createSemanticWindow(getViewProperties().calculator(), pattern.nid())));
    }

    /**
     * Builds the window: its edit session, its view as the root pane, and the controller wiring
     * the window's behavior onto the view for its definition.
     */
    private void build(UUID journalTopic, EntityFacade entityFacade,
                       BiFunction<GenPurposeWindowView, WindowEditSession, GenPurposeDetailsController> controllerFactory) {
        // No component to open means the window was launched from the Journal's "+" button to create a
        // new one; a supplied component means it was opened ("Open as ...") to edit that component,
        // or to go on creating it when it was never published (see WindowEditSession).
        // Edits go through the window's derived coordinate (#660), not the raw journal one.
        WindowEditSession session = new WindowEditSession(getViewProperties(), entityFacade);

        // The view is the window's root pane. The window's KL ViewContext goes on it before the
        // controller builds the definition's sections, whose KL areas resolve that context (#660).
        GenPurposeWindowView view = new GenPurposeWindowView();
        paneWindow = view;
        establishViewContext();

        // Wires the window's behavior onto the view for the definition.
        GenPurposeDetailsController controller = controllerFactory.apply(view, session);
        this.detailsController = controller;
        controller.setOnCloseWindow(_ -> getOnClose().ifPresent(Runnable::run));

        // The component the window frames: in create mode, the one created once it exists. Saved
        // to preferences so a reopened journal restores the window on it.
        session.componentProperty().subscribe(component -> {
            setEntityFacade(component);
            save();
        });
    }

    @Override
    public EntityKlWindowType getWindowType() {
        return EntityKlWindowTypes.GEN_PURPOSE_KL;
    }

    @Override
    protected void captureAdditionalState(EntityKlWindowState state) {
        super.captureAdditionalState(state);
        if (semanticPattern != null) {
            // The standard Semantic window has no stored definition to name: it is told apart by
            // its title and regenerated from its pattern on restore.
            state.addProperty(KL_EDITOR_WINDOW_TITLE, StandardEditorWindows.SEMANTIC_WINDOW);
            state.addProperty(KL_EDITOR_WINDOW_DIR, KL_STANDARD_WINDOWS_DIR);
            state.addProperty(SEMANTIC_PATTERN_UUID, semanticPattern.publicId().leastUuid().toString());
            return;
        }
        // Persist which KL-editor window definition this window is built from (title + folder), so the
        // factory can re-resolve it on restore (komet-desktop#20).
        Path path = Paths.get(klEditorWindowPreferences.absolutePath());
        state.addProperty(KL_EDITOR_WINDOW_TITLE, path.getFileName().toString());
        state.addProperty(KL_EDITOR_WINDOW_DIR, path.getParent().getFileName().toString());
    }

    // The properties panel's open state and selected tab are not persisted for this window type.

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
    protected void setSelectedPropertyPanel(String selectedPanel) {
    }

    private GenPurposeDetailsController detailsController;

    /** The Window also follows the KL editor's shared window model. */
    @Override
    public void releaseSubscriptions() {
        super.releaseSubscriptions();
        if (detailsController != null) {
            detailsController.releaseSubscriptions();
        }
    }
}
