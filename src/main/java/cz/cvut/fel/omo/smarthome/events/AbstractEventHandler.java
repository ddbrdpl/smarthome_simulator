package cz.cvut.fel.omo.smarthome.events;

public abstract class AbstractEventHandler implements EventHandler {
    protected EventHandler next;

    @Override
    public void setNext(EventHandler next) {
        this.next = next;
    }

    protected boolean next(Event e) {
        return next != null && next.handle(e);
    }

    // Helper to log handling
    protected void markHandled(Event e, String handlerName) {
        System.out.println("[" + handlerName + "] handled " + e.getType());
        e.setHandledBy(handlerName);
    }
}