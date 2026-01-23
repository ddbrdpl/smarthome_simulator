package cz.cvut.fel.omo.smarthome.events;

// Catch-all handler for unprocessed events.
public class FallbackHandler extends AbstractEventHandler {
    @Override
    public boolean handle(Event e) {
        System.out.println("[SYSTEM] Unhandled event: " + e.getType());
        return true;
    }
}