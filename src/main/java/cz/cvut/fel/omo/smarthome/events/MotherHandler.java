package cz.cvut.fel.omo.smarthome.events;

/**
 * Event handler representing the mother role.
 *
 * <p>The mother handles safety-related alerts,
 * such as smoke or gas detection.</p>
 */
public class MotherHandler extends AbstractEventHandler {

    /**
     * Handles smoke or gas alert events.
     *
     * @param e event to handle
     * @return {@code true} if handled, otherwise delegated further
     */
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.SMOKE_ALERT) {
            System.out.println("[MOTHER] handled " + e.getType());
            e.setHandledBy("MOTHER");
            return true;
        }
        return next(e);
    }
}
