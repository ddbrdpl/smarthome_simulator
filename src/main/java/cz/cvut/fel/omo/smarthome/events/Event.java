package cz.cvut.fel.omo.smarthome.events;

import java.time.LocalDateTime;

public class Event {
    private final EventType type;
    private final Object source;
    private final Object target;
    private final LocalDateTime createdAt;

    private String handledBy;
    public void setHandledBy(String h) { this.handledBy = h; }
    public String getHandledBy() { return handledBy; }


    public Event(EventType type, Object source, Object target) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.createdAt = LocalDateTime.now();
    }

    public EventType getType() { return type; }
    public Object getSource() { return source; }
    public Object getTarget() { return target; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
