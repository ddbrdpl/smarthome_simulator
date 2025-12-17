package cz.cvut.fel.omo.smarthome.events;

public class DaughterHandler extends AbstractEventHandler {
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.PET_OUTSIDE_LONG) {
            System.out.println("[DAUGHTER] handled " + e.getType());
            e.setHandledBy("DAUGHTER");
            return true;
        }
        return next(e);
    }
}
