package cz.cvut.fel.omo.smarthome.events;

public class GrandfatherHandler extends AbstractEventHandler {
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.MOTION_DETECTED) {
            System.out.println("[GRANDFATHER] handled " + e.getType());
            return true;
        }
        return next(e);
    }
}
