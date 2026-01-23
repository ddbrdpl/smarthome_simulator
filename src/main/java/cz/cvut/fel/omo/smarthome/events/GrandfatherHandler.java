package cz.cvut.fel.omo.smarthome.events;

// Grandfather watches for motion.
public class GrandfatherHandler extends AbstractEventHandler {
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.MOTION_DETECTED) {
            markHandled(e, "GRANDFATHER");
            return true;
        }
        return next(e);
    }
}