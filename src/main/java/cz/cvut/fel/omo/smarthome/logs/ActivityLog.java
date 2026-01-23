package cz.cvut.fel.omo.smarthome.logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityLog {
    private final List<ActivityEntry> entries = new ArrayList<>();

    public void add(ActivityEntry e) {
        entries.add(e);
    }

    public List<ActivityEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }
}