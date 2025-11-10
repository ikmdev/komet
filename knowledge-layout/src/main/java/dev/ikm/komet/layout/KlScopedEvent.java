package dev.ikm.komet.layout;

import javafx.event.Event;
import javafx.scene.input.*;

import java.util.concurrent.atomic.AtomicBoolean;

public interface KlScopedEvent {
    AtomicBoolean isAltDown = new AtomicBoolean(false);
    AtomicBoolean isShiftDown = new AtomicBoolean(false);
    AtomicBoolean isCtrlDown = new AtomicBoolean(false);

    ScopedValue<Event> EVENT = ScopedValue.newInstance();

    static boolean isAltDown() {
        if (KlScopedEvent.EVENT.isBound()) {
            return isAltDown(KlScopedEvent.EVENT.get());
        }
        return false;
    }

    static boolean isAltDown(Event event) {
        if (KlScopedEvent.EVENT.isBound() && KlScopedEvent.EVENT.get() instanceof InputEvent inputEvent) {
            return switch (inputEvent) {
                case KeyEvent keyEvent -> keyEvent.isAltDown();
                case MouseEvent mouseEvent -> mouseEvent.isAltDown();
                case ScrollEvent scrollEvent -> scrollEvent.isAltDown();
                case ZoomEvent zoomEvent -> zoomEvent.isAltDown();
                case RotateEvent rotateEvent -> rotateEvent.isAltDown();
                case SwipeEvent swipeEvent -> swipeEvent.isAltDown();
                default -> false;
            };
        }
        return false;
    }
}
