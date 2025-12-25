package cz.cvut.fel.omo.smarthome.logs;

import java.time.LocalDateTime;

/**
 * Immutable record describing a single activity performed in the smart home.
 *
 * <p>Typical examples: TURN_ON, TURN_OFF, DENIED_TURN_ON, BUY_DEVICE, etc.</p>
 *
 * <p>The entry stores who performed the action, what action was performed,
 * on which target, and when it happened.</p>
 */
public class ActivityEntry {

    /** Identifier of the person (or "SYSTEM"). */
    private final String personId;

    /** Display name of the person (or "SYSTEM"). */
    private final String personName;

    /** Action name (e.g. TURN_ON, DENIED_TURN_OFF, BUY_DEVICE). */
    private final String action;

    /** Human-readable name of the target device/object. */
    private final String targetName;

    /** Timestamp of the activity. */
    private final LocalDateTime time;

    /**
     * Creates a new activity entry.
     *
     * @param personId   id of the acting person (or system identifier)
     * @param personName name of the acting person (or "SYSTEM")
     * @param action     action code/name
     * @param targetName name of the target object/device
     * @param time       time of the activity
     */
    public ActivityEntry(String personId, String personName, String action, String targetName, LocalDateTime time) {
        this.personId = personId;
        this.personName = personName;
        this.action = action;
        this.targetName = targetName;
        this.time = time;
    }

    /** @return acting person id (or system id) */
    public String getPersonId() { return personId; }

    /** @return acting person name (or "SYSTEM") */
    public String getPersonName() { return personName; }

    /** @return action code/name */
    public String getAction() { return action; }

    /** @return target name */
    public String getTargetName() { return targetName; }

    /** @return timestamp of the activity */
    public LocalDateTime getTime() { return time; }
}
