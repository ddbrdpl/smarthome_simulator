package cz.cvut.fel.omo.smarthome.events;

public class MotherHandler extends AbstractEventHandler {
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
