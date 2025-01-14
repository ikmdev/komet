package dev.ikm.komet.layout;

/**
 * Highest level Knowledge Layout Component. Some components, such as {@code Window} do
 * not descend from {@code Node}, and this interface enables inclusion of those components
 * in the Knowledge Layout paradigm (i.e. consistent use of factories and preferences, and
 * an ability to serialize, share, and restore a layout.)
 *
 */
public interface KlGadget<T> {
    T klGadget();
}
