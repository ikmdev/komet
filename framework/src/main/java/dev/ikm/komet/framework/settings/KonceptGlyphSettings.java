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
package dev.ikm.komet.framework.settings;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.util.Subscription;

/**
 * Puts the glyph settings of {@link KometSettings} into effect: which of the marks that lead a
 * koncept's identicon are drawn — the component-kind sigil, the definition-status glyph, and the
 * multiple-parents fork (ikmdev/komet-desktop#153). Every renderer of those marks (the
 * {@code KonceptBadge}, kview's component chip and concept navigator, the drag glyph) reads the
 * values here rather than the settings store, so a control needs no preferences service to
 * render, and a test can drive the marks without touching the real settings of whoever runs it.
 *
 * <p>Until {@link #install(KometSettings)} is called every mark is shown — the default, and what
 * a host with no settings (Scene Builder, a control test) gets.
 *
 * <p>Controls follow these values live. A control that comes and goes (a chip, a cell) gates its
 * subscription on being on a scene ({@code ObservableValue.when}), so the application-wide values
 * here hold no reference to it once it is discarded.
 */
public final class KonceptGlyphSettings {

    private static final ReadOnlyBooleanWrapper showKindSigil =
            new ReadOnlyBooleanWrapper(KonceptGlyphSettings.class, "showKindSigil", true);
    private static final ReadOnlyBooleanWrapper showDefinitionStatus =
            new ReadOnlyBooleanWrapper(KonceptGlyphSettings.class, "showDefinitionStatus", true);
    private static final ReadOnlyBooleanWrapper showMultipleParents =
            new ReadOnlyBooleanWrapper(KonceptGlyphSettings.class, "showMultipleParents", true);

    private KonceptGlyphSettings() {
    }

    /**
     * Starts following {@code settings}. Call once, before the first window is shown; every mark
     * rendered from then on reflects the settings as they change.
     *
     * @param settings the settings to put into effect
     * @return a subscription that stops following the settings and shows every mark again
     */
    public static Subscription install(KometSettings settings) {
        showKindSigil.bind(settings.showKindSigilProperty());
        showDefinitionStatus.bind(settings.showDefinitionStatusProperty());
        showMultipleParents.bind(settings.showDefinitionStatusProperty().and(settings.showMultipleParentsProperty()));
        return () -> {
            for (ReadOnlyBooleanWrapper shown : new ReadOnlyBooleanWrapper[] {
                    showKindSigil, showDefinitionStatus, showMultipleParents}) {
                shown.unbind();
                shown.set(true);
            }
        };
    }

    /**
     * Whether the component-kind sigil (D / S / P letter, stamp pentagon) is drawn beside names.
     *
     * @return the live value, {@code true} until settings are installed
     */
    public static ReadOnlyBooleanProperty showKindSigil() {
        return showKindSigil.getReadOnlyProperty();
    }

    /**
     * Whether the definition-status glyph (defined, primitive, root) is drawn beside concept names.
     *
     * @return the live value, {@code true} until settings are installed
     */
    public static ReadOnlyBooleanProperty showDefinitionStatus() {
        return showDefinitionStatus.getReadOnlyProperty();
    }

    /**
     * Whether the multiple-parents mark — the fork appended to the status glyph, the navigator's
     * alternate-parents tree — is drawn. The fork only ever appears on top of the definition
     * status, and the Settings dialog disables this option while that status is hidden, so the
     * value here is {@code false} whenever {@link #showDefinitionStatus()} is: a mark the user can
     * no longer reach in the dialog is not left showing.
     *
     * @return the live value, {@code true} until settings are installed
     */
    public static ReadOnlyBooleanProperty showMultipleParents() {
        return showMultipleParents.getReadOnlyProperty();
    }

    /** @see #showKindSigil() */
    public static boolean isShowKindSigil() {
        return showKindSigil.get();
    }

    /** @see #showDefinitionStatus() */
    public static boolean isShowDefinitionStatus() {
        return showDefinitionStatus.get();
    }

    /** @see #showMultipleParents() */
    public static boolean isShowMultipleParents() {
        return showMultipleParents.get();
    }
}
