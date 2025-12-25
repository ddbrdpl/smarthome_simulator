package cz.cvut.fel.omo.smarthome.events;

/**
 * Listener interface for receiving {@link Event}s.
 *
 * <p>Classes implementing this interface can subscribe to
 * {@link EventBus} to react to events occurring in the system.</p>
 */
public interface EventListener {

    /**
     * Called when an event is published to the {@link EventBus}.
     *
     * @param e received event
     */
    void onEvent(Event e);
}
