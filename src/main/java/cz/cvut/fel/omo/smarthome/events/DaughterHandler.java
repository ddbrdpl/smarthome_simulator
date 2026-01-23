package cz.cvut.fel.omo.smarthome.events;

// Daughter takes care of the pet.
public class DaughterHandler extends AbstractEventHandler {
    @Override
    public boolean handle(Event e) {
        if (e.getType() == EventType.PET_OUTSIDE_LONG) {
            markHandled(e, "DAUGHTER");
            return true;
        }
        return next(e);
    }
}