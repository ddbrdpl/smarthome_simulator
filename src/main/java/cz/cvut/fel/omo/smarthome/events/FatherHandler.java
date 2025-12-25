package cz.cvut.fel.omo.smarthome.events;

import cz.cvut.fel.omo.smarthome.devices.Device;

/**
 * Event handler representing the father role.
 *
 * <p>The father has the highest authority and is responsible for
 * handling critical system events such as water leaks and device failures.</p>
 *
 * <p>This handler can also repair broken devices.</p>
 */
public class FatherHandler implements EventHandler {

    /** Next handler in the chain */
    private EventHandler next;

    /**
     * Handles water leak and device failure events.
     *
     * @param event event to handle
     * @return {@code true} if handled, otherwise delegated to next handler
     */
    @Override
    public boolean handle(Event event) {

        if (event.getType() == EventType.WATER_LEAK) {
            System.out.println("[FATHER] handled WATER_LEAK");
            event.setHandledBy("FATHER");
            return true;
        }

        if (event.getType() == EventType.DEVICE_BROKEN) {
            System.out.println("[FATHER] handled " + event.getType());

            if (event.getSource() instanceof Device d) {
                d.repair();
                System.out.println("[FATHER] repaired device: " + d.getName());

                Event repaired = new Event(EventType.DEVICE_REPAIRED, d, null);
                repaired.setHandledBy("FATHER");
                d.publishEvent(repaired);
            }

            event.setHandledBy("FATHER");
            return true;
        }

        return next != null && next.handle(event);
    }

    /**
     * Sets the next handler in the chain.
     *
     * @param next next event handler
     */
    @Override
    public void setNext(EventHandler next) {
        this.next = next;
    }
}
