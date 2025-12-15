package cz.cvut.fel.omo.smarthome.events;

public abstract class AbstractEventHandler implements EventHandler {
    private EventHandler next;

    @Override
    public void setNext(EventHandler next) {
        this.next = next;
    }

    protected boolean next(Event e) {
        return next != null && next.handle(e);
    }
}
