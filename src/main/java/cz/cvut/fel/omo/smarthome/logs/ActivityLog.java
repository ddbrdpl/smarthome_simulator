package cz.cvut.fel.omo.smarthome.logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory log of activities performed by people (and system actions).
 *
 * <p>Stores {@link ActivityEntry} records created during simulation steps.
 * The log is later consumed by report generators (e.g. {@code ActivityReportGenerator}).</p>
 *
 * <p>This class is intentionally simple: it only appends entries and exposes
 * an unmodifiable view for safe reading.</p>
 */
public class ActivityLog {

    /** Internal list of activity records in insertion order. */
    private final List<ActivityEntry> entries = new ArrayList<>();

    /**
     * Adds a new activity entry to the log.
     *
     * @param e activity entry to add
     */
    public void add(ActivityEntry e) {
        entries.add(e);
    }

    /**
     * Returns an unmodifiable view of all logged activity entries.
     *
     * @return read-only list of entries
     */
    public List<ActivityEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Clears the log (used when re-initializing the simulation context).
     */
    public void clear() {
        entries.clear();
    }
}
