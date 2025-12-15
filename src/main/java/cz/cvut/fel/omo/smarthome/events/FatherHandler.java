package cz.cvut.fel.omo.smarthome.events;

public class FatherHandler extends AbstractEventHandler {
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.WATER_LEAK || e.getType() == EventType.DEVICE_BROKEN) {
            System.out.println("[FATHER] handled " + e.getType());
            return true;
        }
        return next(e);
    }
}
