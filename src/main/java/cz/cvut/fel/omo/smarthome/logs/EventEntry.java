package cz.cvut.fel.omo.smarthome.logs;

import cz.cvut.fel.omo.smarthome.events.EventType;

import java.time.LocalDateTime;

public class EventEntry {

    private final LocalDateTime time;
    private final EventType type;
    private final String handledBy;
    private final String deviceName;

    public EventEntry(LocalDateTime time, EventType type, String handledBy, String deviceName) {
        this.time = time;
        this.type = type;
        this.handledBy = handledBy;
        this.deviceName = deviceName;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public EventType getType() {
        return type;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
