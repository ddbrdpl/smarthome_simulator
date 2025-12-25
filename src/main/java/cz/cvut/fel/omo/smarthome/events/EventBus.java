package cz.cvut.fel.omo.smarthome.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Central event dispatcher implementing the Observer pattern.
 *
 * <p>The {@code EventBus} allows {@link EventListener}s to subscribe
 * and receive {@link Event}s published by the system.</p>
 *
 * <p>All published events are delivered synchronously
 * to registered listeners.</p>
 */
public class EventBus {

    /** Registered event listeners */
    private final List<EventListener> listeners = new ArrayList<>();

    /**
     * Registers a new event listener.
     *
     * @param l listener to subscribe
     */
    public void subscribe(EventListener l) {
        listeners.add(l);
    }

    /**
     * Publishes an event to all registered listeners.
     *
     * @param e event to publish
     */
    public void publish(Event e) {
        for (EventListener l : listeners) {
            l.onEvent(e);
        }
    }
}
