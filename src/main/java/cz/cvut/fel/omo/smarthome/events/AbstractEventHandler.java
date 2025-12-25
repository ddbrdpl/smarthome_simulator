package cz.cvut.fel.omo.smarthome.events;

/**
 * Base class for event handlers in a Chain of Responsibility.
 *
 * <p>This abstract class provides default implementation
 * for chaining handlers and forwarding events to the next handler.</p>
 *
 * <p>Concrete handlers should extend this class and implement
 * their own handling logic.</p>
 */
public abstract class AbstractEventHandler implements EventHandler {

    /** Next handler in the chain */
    private EventHandler next;

    /**
     * Sets the next handler in the chain.
     *
     * @param next next event handler
     */
    @Override
    public void setNext(EventHandler next) {
        this.next = next;
    }

    /**
     * Forwards the event to the next handler if present.
     *
     * @param e event to forward
     * @return {@code true} if the event was handled by a subsequent handler
     */
    protected boolean next(Event e) {
        return next != null && next.handle(e);
    }
}
