package dev.ikm.komet.kview.events;

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

    private final EntityFacade entityFacade;

    private final String windowTitle;

    private final Node near;

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
        super(source, eventType);
        this.entityFacade = entityFacade;
        this.windowTitle = windowTitle;
        this.near = near;
    }

    public EntityFacade getEntityFacade() { return entityFacade; }

    public String getWindowTitle() { return windowTitle; }

    /** The node the window opens beside, or a peek is anchored at; null when none was given. */
    public Node getNear() { return near; }
}