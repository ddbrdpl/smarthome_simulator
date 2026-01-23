package cz.cvut.fel.omo.smarthome.events;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.EventEntry;
import cz.cvut.fel.omo.smarthome.people.Person;

// Bridges the EventBus and the Logging system.
public class SystemEventListener implements EventListener {

    private final EventHandler chain;
    private final SmartHomeContext ctx;

    public SystemEventListener(EventHandler chain, SmartHomeContext ctx) {
        this.chain = chain;
        this.ctx = ctx;
    }

    @Override
    public void onEvent(Event e) {
        // 1. Process via Chain of Responsibility
        boolean handled = chain.handle(e);

        // 2. Log result
        String handledBy = (e.getHandledBy() != null) ? e.getHandledBy()
                : (handled ? "SYSTEM" : "UNHANDLED");

        String deviceName = (e.getSource() instanceof Device d) ? d.getName() : null;

        String causedBy = null;
        if (e.getTarget() instanceof Person p) {
            causedBy = p.getRole() + " (" + p.getName() + ")";
        }

        ctx.getEventLog().add(new EventEntry(
                e.getCreatedAt(), e.getType(), handledBy, deviceName, causedBy
        ));
    }
}