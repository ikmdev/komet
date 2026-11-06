package dev.ikm.komet.kview.events;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.scene.Node;

public class MakeKLWindowEvent extends Evt {

    public static final EvtType<MakeConceptWindowEvent> OPEN_ENTITY_COMPONENT = new EvtType<>(Evt.ANY, "OPEN_ENTITY_COMPONENT");

    public static final EvtType<MakeConceptWindowEvent> OPEN_ENTITY_FROM_ENTITY = new EvtType<>(Evt.ANY, "OPEN_ENTITY_FROM_ENTITY");

    /**
     * Opens the entity in the named standard (application-provided) KL window, e.g.
     * {@code StandardEditorWindows.CONCEPT_WINDOW}, rather than a user-defined window.
     */
    public static final EvtType<MakeKLWindowEvent> OPEN_STANDARD_WINDOW = new EvtType<>(Evt.ANY, "OPEN_STANDARD_WINDOW");

    /**
     * Shows the entity's named standard KL window as a peek, a transient popover at the
     * {@linkplain #getNear() near} node, closed by a click outside it or Esc, rather than a window
     * added to the journal; pinning it places it as {@link #OPEN_STANDARD_WINDOW} would, beside
     * the window the near node is in.
     */
    public static final EvtType<MakeKLWindowEvent> PEEK_STANDARD_WINDOW = new EvtType<>(Evt.ANY, "PEEK_STANDARD_WINDOW");

    /**
     * Opens the entity in the named standard KL window under the event's
     * {@linkplain #getViewProperties() view}, writing nothing to the store. Unlike
     * {@link #OPEN_STANDARD_WINDOW}, it does not seed the standard windows first: seeding projects
     * any pattern changed since the journal opened and commits the projection, which a request
     * only to look at a component must not do. The journal seeded the definitions when it opened,
     * so they are there; the one difference is that a pattern changed since then shows its
     * definition as the journal last projected it.
     *
     * @see #viewStandardWindow(Object, EntityFacade, String, ViewProperties)
     */
    public static final EvtType<MakeKLWindowEvent> VIEW_STANDARD_WINDOW = new EvtType<>(Evt.ANY, "VIEW_STANDARD_WINDOW");

    private final EntityFacade entityFacade;

    private final String windowTitle;

    private final Node near;

    private final ViewProperties viewProperties;

    /**
     * Constructs a prototypical Event.
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType
     * @param entityFacade
     * @throws IllegalArgumentException if source is null
     */
    public MakeKLWindowEvent(Object source, EvtType eventType, EntityFacade entityFacade, String windowTitle) {
        this(source, eventType, entityFacade, windowTitle, null);
    }

    /**
     * An event placing the window beside the one a node is in, or a peek anchored at the node.
     *
     * @param source       the object on which the Event initially occurred
     * @param eventType    the event type
     * @param entityFacade the entity the window is about
     * @param windowTitle  the standard window's title
     * @param near         the node the window opens beside (the chip or row that asked for it); null places it as usual
     */
    public MakeKLWindowEvent(Object source, EvtType eventType, EntityFacade entityFacade, String windowTitle, Node near) {
        this(source, eventType, entityFacade, windowTitle, near, null);
    }

    private MakeKLWindowEvent(Object source, EvtType eventType, EntityFacade entityFacade, String windowTitle, Node near,
                              ViewProperties viewProperties) {
        super(source, eventType);
        this.entityFacade = entityFacade;
        this.windowTitle = windowTitle;
        this.near = near;
        this.viewProperties = viewProperties;
    }

    /**
     * An event opening a standard window on an entity under a view, writing nothing to the store:
     * see {@link #VIEW_STANDARD_WINDOW}.
     *
     * @param source         the object on which the Event initially occurred
     * @param entityFacade   the entity the window is about
     * @param windowTitle    the standard window's title
     * @param viewProperties the view the window reads; the window derives its own coordinate from
     *                       it, so its coordinates menu does not change the view given
     * @return the event
     */
    public static MakeKLWindowEvent viewStandardWindow(Object source, EntityFacade entityFacade, String windowTitle,
                                                       ViewProperties viewProperties) {
        return new MakeKLWindowEvent(source, VIEW_STANDARD_WINDOW, entityFacade, windowTitle, null, viewProperties);
    }

    public EntityFacade getEntityFacade() { return entityFacade; }

    public String getWindowTitle() { return windowTitle; }

    /** The node the window opens beside, or a peek is anchored at; null when none was given. */
    public Node getNear() { return near; }

    /** The view a {@link #VIEW_STANDARD_WINDOW} window reads; null otherwise. */
    public ViewProperties getViewProperties() { return viewProperties; }
}