package cz.cvut.fel.omo.smarthome.logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventLog {
    private final List<EventEntry> entries = new ArrayList<>();

    public void add(EventEntry e) {
        entries.add(e);
    }

    public List<EventEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }
}