/**
 * <b>Expansion</b> — the capability for a figure to fill a larger surface, and the ladder of surfaces
 * it can fill.
 * <p>
 * A <em>figure</em> is anything worth reading larger than its place allows: a document, a concept tree,
 * a table. Any figure — a {@link dev.ikm.komet.layout.KlArea}, a plain control, or a document block —
 * that implements {@link dev.ikm.komet.layout.expand.KlExpandable} carries a small expand icon in a
 * configurable {@link dev.ikm.komet.layout.expand.ExpansionCorner} (default bottom-right), hidden until
 * the cursor nears it so it never competes with the content. Activating it does an in-place modal
 * takeover via {@link dev.ikm.komet.layout.controls.KlExpansionOverlay}, dismissed with {@code Escape},
 * returning the figure to its place and its scroll position — "the grammar of fullscreen video / map
 * expand".
 * <p>
 * Expansion is <b>stepped, not absolute</b>. A figure fills the nearest enclosing
 * {@link dev.ikm.komet.layout.expand.KlExpansionHost} rung, and from there may climb: a table fills its
 * document, then its card, then the journal window, and — if the figure opts in — the screen. Each step
 * names its destination, so the reader always knows where "further" leads. Only the innermost figure
 * under the cursor offers its icon, so nested figures never produce competing affordances.
 * <p>
 * A surface becomes a rung by <em>opting in</em>, stamping itself via
 * {@link dev.ikm.komet.layout.expand.KlExpansionHost#mark}. Most ancestors cannot honestly host an
 * expanded figure: a scroll pane's content is taller than its viewport, a split pane's items are
 * skin-managed, a workspace's desktop is a canvas many times the size of the window. Opting in is what
 * separates "an ancestor" from "a place a reader would recognise as somewhere to be". It also inverts
 * the dependency, so a surface defined in a higher tier can be a rung without this tier knowing that
 * tier exists.
 * <p>
 * Where a figure also implements {@link dev.ikm.komet.layout.expand.KlPreferenceEditable}, the expanded
 * chrome offers a standard <b>preferences toggle</b> over an editor — by default a generic
 * {@link dev.ikm.komet.layout.settings.ConceptSettingsForm} generated from the figure's
 * concept-identity-enum settings record. So reading and adjusting a figure's stored preferences is one
 * uniform gesture rather than a bespoke control per card. Like
 * {@link dev.ikm.komet.layout.KlPeerToRegion}, the behaviour lives in {@code default} methods that
 * manipulate the figure's node and stash state in its node properties — a thin convention, not a
 * framework.
 */
package dev.ikm.komet.layout.expand;
