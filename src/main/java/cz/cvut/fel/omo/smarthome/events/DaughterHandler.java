package cz.cvut.fel.omo.smarthome.events;

/**
 * Event handler representing the daughter role.
 *
 * <p>The daughter is responsible for handling
 * pet-related events.</p>
 */
public class DaughterHandler extends AbstractEventHandler {

    /**
     * Handles pet-related alert events.
     *
     * @param e event to handle
     * @return {@code true} if handled, otherwise delegated further
     */
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.PET_OUTSIDE_LONG) {
            System.out.println("[DAUGHTER] handled " + e.getType());
            e.setHandledBy("DAUGHTER");
            return true;
        }
        return next(e);
    }
}
