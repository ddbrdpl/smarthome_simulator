package cz.cvut.fel.omo.smarthome.events;

// Mother handles safety alerts.
public class MotherHandler extends AbstractEventHandler {
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.SMOKE_ALERT) {
            markHandled(e, "MOTHER");
            return true;
        }
        return next(e);
    }
}