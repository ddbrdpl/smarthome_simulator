package cz.cvut.fel.omo.smarthome.logs;

import java.time.LocalDateTime;

public class ActivityEntry {
    private final String personId;
    private final String personName;
    private final String action;
    private final String targetName;
    private final LocalDateTime time;

    public ActivityEntry(String personId, String personName, String action, String targetName, LocalDateTime time) {
        this.personId = personId;
        this.personName = personName;
        this.action = action;
        this.targetName = targetName;
        this.time = time;
    }

    public String getPersonId() { return personId; }
    public String getPersonName() { return personName; }
    public String getAction() { return action; }
    public String getTargetName() { return targetName; }
    public LocalDateTime getTime() { return time; }
}
