package dev.ikm.komet.layout;

import javafx.scene.layout.Region;

/**
 * A {@code KlCard} is a self-contained, movable panel placed on a workspace
 * <em>Surface</em> (for example the Journal's workspace desktop). It is tier four of the
 * Knowledge Layout containment ladder:
 *
 * <pre>
 *   Window (Stage)  &rarr;  Frame ({@link dev.ikm.komet.layout.window.KlRenderView})
 *                   &rarr;  Surface (the workspace)
 *                   &rarr;  Card ({@code KlCard})
 *                   &rarr;  Area ({@link KlArea})
 * </pre>
 *
 * <p><b>Why a distinct term.</b> The word "window" has been overloaded to mean three different
 * things at once: a top-level OS {@code Stage} ("Window"), the Journal that hosts a workspace
 * ("Journal Window"), and the movable panels that float on that workspace ("a window in the
 * Journal"). A {@code KlCard} is the third of these — <em>not</em> an OS window. It is a card:
 * a discrete, titled, draggable, resizable, possibly-overlapping surface that the user arranges
 * on the workspace, composed of {@link KlArea}s. Reserving "Window" for the {@code Stage} and
 * naming this tier "Card" removes that overload.
 *
 * <p>A card is also a {@link KlParent}: it hosts child {@link KlArea}s in its
 * {@link KlParent#gridPaneForChildren() children grid}. Like {@link dev.ikm.komet.layout.area.KlSupplementalArea},
 * {@code KlCard} is a non-sealed member of the sealed {@link KlArea} family so that cards can be
 * recognized as a first-class area kind throughout the framework.
 *
 * @param <FX> the type of JavaFX {@code Region} that serves as this card's root node
 * @see dev.ikm.komet.layout.area.KlSupplementalArea
 * @see KlParent
 */
public non-sealed interface KlCard<FX extends Region> extends KlArea<FX>, KlParent<FX> {

    /**
     * The factory contract for producing and restoring {@link KlCard} instances. It is a
     * non-sealed member of the sealed {@link KlArea.Factory} family, mirroring
     * {@link dev.ikm.komet.layout.area.KlSupplementalArea.Factory}.
     *
     * @param <FX> the type of JavaFX {@code Region} that serves as the produced card's root node
     * @param <KL> the concrete {@link KlCard} type this factory produces
     */
    non-sealed interface Factory<FX extends Region, KL extends KlCard<FX>>
            extends KlArea.Factory<FX, KL> {
    }
}
