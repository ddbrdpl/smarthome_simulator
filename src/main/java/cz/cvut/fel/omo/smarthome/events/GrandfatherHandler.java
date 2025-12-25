package cz.cvut.fel.omo.smarthome.events;

/**
 * Event handler representing the grandfather role.
 *
 * <p>The grandfather handles motion-related events
 * detected by sensors.</p>
 */
public class GrandfatherHandler extends AbstractEventHandler {

    /**
     * Handles motion detection events.
     *
     * @param e event to handle
     * @return {@code true} if handled, otherwise delegated further
     */
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.MOTION_DETECTED) {
            System.out.println("[GRANDFATHER] handled " + e.getType());
            e.setHandledBy("GRANDFATHER");
            return true;
        }
        return next(e);
    }
}
