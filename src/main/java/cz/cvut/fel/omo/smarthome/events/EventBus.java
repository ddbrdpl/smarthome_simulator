package cz.cvut.fel.omo.smarthome.events;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private final List<EventListener> listeners = new ArrayList<>();

    public void subscribe(EventListener l) {
        listeners.add(l);
    }

    public void publish(Event e) {
        for (EventListener l : listeners) {
            l.onEvent(e);
        }
    }
}
