package cz.cvut.fel.omo.smarthome.logs;

import cz.cvut.fel.omo.smarthome.events.EventType;
import java.time.LocalDateTime;

/**
 * Immutable record describing one event that occurred in the smart home.
 *
 * <p>Each entry contains the event type, timestamp, who handled the event,
 * optional device name, and optional "causedBy" (who triggered the event).</p>
 *
 * <p>The "causedBy" field is typically filled for events like {@code DEVICE_BROKEN},
 * where the target can carry information about the person who last used the device.</p>
 */
public class EventEntry {

    /** Timestamp of when the event was created. */
    private final LocalDateTime time;

    /** Type of the event (e.g. DEVICE_BROKEN, SMOKE_ALERT). */
    private final EventType type;

    /** Who handled the event in the handler chain (e.g. FATHER, MOTHER). */
    private final String handledBy;

    /** Optional device name if the event source is a device. */
    private final String deviceName;

    /** Optional information about who caused the event (role + person name). */
    private final String causedBy;

    /**
     * Creates a new event entry.
     *
     * @param time      event timestamp
     * @param type      event type
     * @param handledBy who handled the event
     * @param deviceName optional device name (may be null)
     * @param causedBy  optional "caused by" info (may be null)
     */
    public EventEntry(LocalDateTime time, EventType type, String handledBy, String deviceName, String causedBy) {
        this.time = time;
        this.type = type;
        this.handledBy = handledBy;
        this.deviceName = deviceName;
        this.causedBy = causedBy;
    }

    /** @return event timestamp */
    public LocalDateTime getTime() { return time; }

    /** @return event type */
    public EventType getType() { return type; }

    /** @return who handled the event */
    public String getHandledBy() { return handledBy; }

    /** @return device name or null */
    public String getDeviceName() { return deviceName; }

    /** @return caused by info or null */
    public String getCausedBy() { return causedBy; }
}
