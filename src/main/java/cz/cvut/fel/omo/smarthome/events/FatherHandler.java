package cz.cvut.fel.omo.smarthome.events;

import cz.cvut.fel.omo.smarthome.devices.Device;

public class FatherHandler extends AbstractEventHandler {

    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.WATER_LEAK) {
            markHandled(e, "FATHER");
            // Critical: Father deals with leaks immediately (optional logic)
            return true;
        }

        if (e.getType() == EventType.DEVICE_BROKEN) {
            // Father acknowledges the breakdown, but repairs will happen
            // during his active simulation step (Person.performStep), not instantly here.
            markHandled(e, "FATHER (Notified)");
            return true;
        }

        return next(e);
    }
}