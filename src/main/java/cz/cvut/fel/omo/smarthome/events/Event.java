package cz.cvut.fel.omo.smarthome.events;

import cz.cvut.fel.omo.smarthome.house.SmartHomeContext;

import java.time.LocalDateTime;

public class Event {
    private final EventType type;
    private final Object source;
    private final Object target; // Optional: person involved or related object
    private final LocalDateTime createdAt;
    private String handledBy;

    public Event(EventType type, Object source, Object target) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.createdAt = SmartHomeContext.getInstance().getCurrentTime();
    }

    public EventType getType() { return type; }
    public Object getSource() { return source; }
    public Object getTarget() { return target; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }
}