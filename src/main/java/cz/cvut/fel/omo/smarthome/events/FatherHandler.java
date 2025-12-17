package cz.cvut.fel.omo.smarthome.events;

public class FatherHandler implements EventHandler {

    private EventHandler next;

    @Override
    public boolean handle(Event event) {
        if (event.getType() == EventType.WATER_LEAK) {
            System.out.println("[FATHER] handled WATER_LEAK");

            event.setHandledBy("FATHER");

            return true;
        }
        return next != null && next.handle(event);
    }

    @Override
    public void setNext(EventHandler next) {
        this.next = next;
    }
}
