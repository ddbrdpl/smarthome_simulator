package cz.cvut.fel.omo.smarthome.events;

public interface EventHandler {
    boolean handle(Event e);
    void setNext(EventHandler next);
}
