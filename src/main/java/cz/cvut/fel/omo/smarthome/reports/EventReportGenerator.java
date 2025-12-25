package cz.cvut.fel.omo.smarthome.reports;

import cz.cvut.fel.omo.smarthome.logs.EventEntry;
import cz.cvut.fel.omo.smarthome.logs.EventLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates a textual event report from {@link EventLog}.
 *
 * <p>The report contains a chronological list of events, formatted as:</p>
 * <pre>
 * time | EVENT_TYPE | device: X | caused by: Y | handled by: Z
 * </pre>
 *
 * <p>Fields such as device name or caused-by information are optional
 * and are included only when available.</p>
 */
public class EventReportGenerator {

    /** Source event log. */
    private final EventLog log;

    /**
     * Creates a new event report generator.
     *
     * @param log event log to read from
     */
    public EventReportGenerator(EventLog log) {
        this.log = log;
    }

    /**
     * Writes the event report into a text file.
     *
     * @param path target output path (directories are created automatically)
     * @throws IllegalStateException if the report cannot be written
     */
    public void generate(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== EVENT REPORT ===\n\n");

        for (EventEntry e : log.getEntries()) {
            sb.append(e.getTime()).append(" | ")
                    .append(e.getType()).append(" | ");

            if (e.getDeviceName() != null) {
                sb.append("device: ").append(e.getDeviceName()).append(" | ");
            }

            if (e.getCausedBy() != null) {
                sb.append("caused by: ").append(e.getCausedBy()).append(" | ");
            }

            sb.append("handled by: ").append(e.getHandledBy())
                    .append("\n");
        }

        try {
            Path p = Path.of(path);
            Files.createDirectories(p.getParent());
            Files.writeString(p, sb.toString());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write event report", ex);
        }
    }
}
