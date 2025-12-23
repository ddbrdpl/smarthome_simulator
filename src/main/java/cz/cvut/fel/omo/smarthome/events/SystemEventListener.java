package cz.cvut.fel.omo.smarthome.events;

import cz.cvut.fel.omo.smarthome.devices.Device;
import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;
import cz.cvut.fel.omo.smarthome.logs.EventEntry;
import cz.cvut.fel.omo.smarthome.people.Person;

public class SystemEventListener implements EventListener {

    private final EventHandler chain;
    private final SmartHomeContext ctx;

    public SystemEventListener(EventHandler chain, SmartHomeContext ctx) {
        this.chain = chain;
        this.ctx = ctx;
    }

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

        // NEW: who caused it (from target)
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
