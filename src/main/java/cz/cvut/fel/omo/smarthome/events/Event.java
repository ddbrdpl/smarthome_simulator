package cz.cvut.fel.omo.smarthome.events;

import java.time.LocalDateTime;

/**
 * Represents an event occurring in the smart home system.
 *
 * <p>An event is created when a significant situation happens,
 * such as a device breaking, a sensor alert, or a repair.</p>
 *
 * <p>Events are published through {@link EventBus} and processed by
 * registered {@link EventListener}s and {@link EventHandler}s.</p>
 */
public class Event {

    /** Type of the event */
    private final EventType type;

    /** Source that caused the event (usually a device or sensor) */
    private final Object source;

    /** Optional target related to the event (e.g. person who caused it) */
    private final Object target;

    /** Timestamp when the event was created */
    private final LocalDateTime createdAt;

    /** Identifier of the handler that processed the event */
    private String handledBy;

    /**
     * Creates a new event with current timestamp.
     *
     * @param type   type of the event
     * @param source originator of the event
     * @param target optional related object
     */
    public Event(EventType type, Object source, Object target) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.createdAt = LocalDateTime.now();
    }

    /** @return event type */
    public EventType getType() {
        return type;
    }

    /** @return event source */
    public Object getSource() {
        return source;
    }

    /** @return optional event target */
    public Object getTarget() {
        return target;
    }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return identifier of the handler that processed the event */
    public String getHandledBy() {
        return handledBy;
    }

    /**
     * Sets the identifier of the handler that processed the event.
     *
     * @param handledBy handler identifier
     */
    public void setHandledBy(String handledBy) {
        this.handledBy = handledBy;
    }
}
