package cz.cvut.fel.omo.smarthome.events;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.EventEntry;
import cz.cvut.fel.omo.smarthome.people.Person;

/**
 * Central system-level event listener.
 *
 * <p>This listener receives all events from {@link EventBus},
 * processes them through a chain of {@link EventHandler}s,
 * and records the result into the system {@link cz.cvut.fel.omo.smarthome.logs.EventLog}.</p>
 *
 * <p>Acts as a bridge between the event system and persistent event logging.</p>
 */
public class SystemEventListener implements EventListener {

    /** Root handler of the event handling chain */
    private final EventHandler chain;

    /** Smart home context for logging and system access */
    private final SmartHomeContext ctx;

    /**
     * Creates a new system event listener.
     *
     * @param chain root of the event handler chain
     * @param ctx   smart home context
     */
    public SystemEventListener(EventHandler chain, SmartHomeContext ctx) {
        this.chain = chain;
        this.ctx = ctx;
    }

    /**
     * Processes an incoming event, delegates handling,
     * and stores the event into the event log.
     *
     * @param e received event
     */
    @Override
    public void onEvent(Event e) {
        boolean handled = chain.handle(e);

        String handledBy = e.getHandledBy();
        if (handledBy == null) {
            handledBy = handled ? "SYSTEM" : "SYSTEM (unhandled)";
        }

        String deviceName = null;
        if (e.getSource() instanceof Device d) {
            deviceName = d.getName();
        }

        String causedBy = null;
        if (e.getTarget() instanceof Person p) {
            causedBy = p.getRole() + " (" + p.getName() + ")";
        }

        ctx.getEventLog().add(new EventEntry(
                e.getCreatedAt(),
                e.getType(),
                handledBy,
                deviceName,
                causedBy
        ));
    }
}
