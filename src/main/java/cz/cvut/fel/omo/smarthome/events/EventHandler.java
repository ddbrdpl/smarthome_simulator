package cz.cvut.fel.omo.smarthome.events;

/**
 * Handler interface for processing events using the Chain of Responsibility pattern.
 *
 * <p>Each handler decides whether it can process a given {@link Event}.
 * If it cannot handle the event, it forwards it to the next handler
 * in the chain.</p>
 */
public interface EventHandler {

    /**
     * Attempts to handle the given event.
     *
     * @param e event to handle
     * @return {@code true} if the event was handled, {@code false} otherwise
     */
    boolean handle(Event e);

    /**
     * Sets the next handler in the chain.
     *
     * @param next next event handler
     */
    void setNext(EventHandler next);
}
