package dev.ikm.komet.kview.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A keyboard shortcut drawn as a row of keycaps, one per key: {@code Ctrl} {@code Shift} {@code I}
 * on Windows, {@code ⇧} {@code ⌘} {@code I} on macOS. It sits beside whatever the shortcut
 * triggers (a setting, a button, a menu row) so the user learns the keys where they would
 * otherwise reach for the mouse.
 * <p>
 * The keys are spelled the way the platform spells them, taken from
 * {@link KeyCombination#getDisplayText()}, which is also what a menu shows for an accelerator.
 * The row only presents: it installs no key handler, so the shortcut it shows must be the one
 * its owner has installed.
 */
public class ShortcutKeys extends HBox {

    /** The modifier symbols macOS spells a shortcut with, run together with no separator. */
    private static final String MAC_MODIFIER_SYMBOLS = "⌃⌥⇧⌘";

    /*=*************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     ************************************************************************=*/

    public ShortcutKeys() {
        getStyleClass().add("shortcut-keys");
        shortcut.subscribe(this::showKeys);
    }

    /*=*************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     ************************************************************************=*/

    /**
     * Splits a shortcut's display text into its keys, one per keycap, in the order they are
     * written: {@code "Ctrl+Shift+I"} is Ctrl, Shift and I; {@code "⇧⌘I"} is ⇧, ⌘ and I.
     *
     * @param displayText a shortcut as {@link KeyCombination#getDisplayText()} spells it
     * @return the keys, modifiers first
     */
    public static List<String> keysOf(String displayText) {
        List<String> keys = new ArrayList<>();
        int index = 0;
        while (index < displayText.length() && MAC_MODIFIER_SYMBOLS.indexOf(displayText.charAt(index)) >= 0) {
            keys.add(displayText.substring(index, index + 1));
            index++;
        }
        if (index < displayText.length()) {
            // A plus separates only when a key follows it: "Ctrl++" is Ctrl and the plus key.
            keys.addAll(Arrays.asList(displayText.substring(index).split("\\+(?=.)")));
        }
        return keys;
    }

    /*=*************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     ************************************************************************=*/

    private void showKeys() {
        getChildren().clear();
        KeyCombination combination = getShortcut();
        if (combination == null) {
            return;
        }
        for (String key : keysOf(combination.getDisplayText())) {
            Label keycap = new Label(key);
            keycap.getStyleClass().add("key");
            getChildren().add(keycap);
        }
    }

    /*=*************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     ************************************************************************=*/

    // -- shortcut
    /**
     * The shortcut whose keys are shown. {@code null}, the default, shows none.
     */
    private final ObjectProperty<KeyCombination> shortcut = new SimpleObjectProperty<>(this, "shortcut");
    public ObjectProperty<KeyCombination> shortcutProperty() { return shortcut; }
    public KeyCombination getShortcut() { return shortcut.get(); }
    public void setShortcut(KeyCombination value) { shortcut.set(value); }
}
