package cz.cvut.fel.omo.smarthome.events;

public class FallbackHandler extends AbstractEventHandler {
    @Override
    public boolean handle(Event e) {
        System.out.println("[SYSTEM] unhandled event: " + e.getType());
        return true;
    }
}
