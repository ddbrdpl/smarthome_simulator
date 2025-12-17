package cz.cvut.fel.omo.smarthome.logs;

import cz.cvut.fel.omo.smarthome.events.EventType;

import java.time.LocalDateTime;

public class EventEntry {
    private final EventType type;
    private final String handledBy;
    private final LocalDateTime time;

    public EventEntry(EventType type, String handledBy, LocalDateTime time) {
        this.type = type;
        this.handledBy = handledBy;
        this.time = time;
    }

    public EventType getType() { return type; }
    public String getHandledBy() { return handledBy; }
    public LocalDateTime getTime() { return time; }
}
